package icu.yeguo.cloudnest.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.yeguo.cloudnest.model.dto.FileFolderItem;
import icu.yeguo.cloudnest.model.dto.UploadRequest;
import icu.yeguo.cloudnest.model.entity.Folder;
import icu.yeguo.cloudnest.model.entity.UserFile;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.*;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static icu.yeguo.cloudnest.constant.CommonConstant.FILE;
import static icu.yeguo.cloudnest.constant.CommonConstant.FOLDER;

@Component
public class FolderHelper {

    @Lazy
    @Resource
    private IUserFileService userFileService;
    @Resource
    private IFileService fileService;
    @Lazy
    @Resource
    private IFolderService folderService;
    @Lazy
    @Resource
    private IStorageQuotaService storageQuotaService;

    @Transactional
    public void copyFolderRecursively(UserVO userVO, Long currentFolderId, Long targetFolderId, String folderName,
                                      UploadRequest uploadRequest) {
        // 获取当前文件夹下所有 文件 文件夹
        List<FileFolderItem> originList = userFileService.getUserFilesByFolderId(currentFolderId);
        List<FileFolderItem> list = new ArrayList<>(originList);
        list.sort(Comparator.comparing(item -> !item.getType().equals(FILE)));

        // 目标文件夹下是否有同名文件夹
        Folder one = folderService.getOneByParentIdAndName(targetFolderId, folderName);
        if (one != null)
            throw new RuntimeException("已存在同名文件夹: " + folderName);

        // 创建新文件夹
        Folder newFolder = folderService.createFolderEntity(userVO.getId(), folderName, targetFolderId);

        for (FileFolderItem item : list) {
            if (item.getType().equals(FILE)) {
                storageQuotaService.checkAvailableSpace(userVO, item.getSize(), uploadRequest);

                UserFile userFile = userFileService.getById(item.getUserFileId());
                userFile.setId(null);
                userFile.setFolderId(newFolder.getId());
                userFileService.save(userFile);

                fileService.increaseReferenceCount(userFile.getFileId());
                storageQuotaService.updateUserStorage(userVO, item.getSize());
            } else {
                this.copyFolderRecursively(userVO, item.getFolderId(), newFolder.getId(), item.getName(), uploadRequest);
            }
        }
    }

    @Transactional
    public void deleteFolderRecursively(UserVO userVO, Long originFolderId) {
        // 获取当前文件夹下所有 文件 文件夹
        List<FileFolderItem> originList = userFileService.getUserFilesByFolderId(originFolderId);
        List<FileFolderItem> list = new ArrayList<>(originList);
        list.sort(Comparator.comparing(item -> !item.getType().equals(FOLDER)));

        if (list.isEmpty()) {
            // 删除空文件夹
            folderService.removePhysicallyById(originFolderId);
        }

        for (FileFolderItem item : list) {
            if (item.getType().equals(FILE)) {
                // 删除文件
                userFileService.removePhysicallyById(item.getUserFileId());
                // 更新文件引用计数
                fileService.decreaseReferenceCount(item.getFileId());
                // 更新用户存储
                storageQuotaService.updateUserStorage(userVO, -item.getSize());
            } else {
                this.deleteFolderRecursively(userVO, item.getFolderId());
            }

            // 删除文件夹
            Long count = userFileService.getBaseMapper()
                    .selectCount(new LambdaQueryWrapper<UserFile>()
                            .eq(UserFile::getFolderId, originFolderId));
            if (count == 0) {
                folderService.removePhysicallyById(originFolderId);
            }
        }

    }
}

