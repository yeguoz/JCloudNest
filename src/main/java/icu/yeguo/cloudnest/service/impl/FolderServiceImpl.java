package icu.yeguo.cloudnest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.dto.UploadRequest;
import icu.yeguo.cloudnest.model.entity.Folder;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.*;
import icu.yeguo.cloudnest.mapper.FolderMapper;
import icu.yeguo.cloudnest.util.FolderHelper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static icu.yeguo.cloudnest.constant.CommonConstant.ROOT;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder>
        implements IFolderService {

    @Resource
    private FolderMapper folderMapper;
    @Resource
    private FolderHelper folderHelper;

    @Override
    public Long createFolder(UserVO userVO, String path, String name) {
        int userId = userVO.getId();
        long parentFolderId = this.findFolderId(userId, path);
        if (parentFolderId == 0)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "父文件夹不存在");
        // 不能有同名文件夹
        Long count = folderMapper.selectCount(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getName, name)
                .eq(Folder::getParentId, parentFolderId)
        );
        if (count > 0)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "存在同名文件夹");
        // 插入文件夹数据
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setName(name);
        folder.setParentId(parentFolderId);
        int insert = folderMapper.insert(folder);
        if (insert < 1)
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "创建目录失败");
        return folder.getId();
    }

    @Override
    public int renameFolder(Long id, String name) {
        Folder folder = folderMapper.selectById(id);
        if (folder == null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件夹不存在");
        folder.setName(name);
        return folderMapper.updateById(folder);
    }

    @Override
    public Long queryFolderIdByRelativePath(Long parentId, String relativePath) {
        String[] relativePaths = Arrays.copyOfRange(relativePath.split(ROOT), 1, relativePath.split(ROOT).length);
        for (String path : relativePaths) {
            if (parentId < 1)
                throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件夹不存在");
            Folder folder = folderMapper.selectOne(new LambdaQueryWrapper<Folder>()
                    .eq(Folder::getParentId, parentId)
                    .eq(Folder::getName, path));
            parentId = folder != null ? folder.getId() : 0;
        }
        return parentId;
    }

    @Override
    public long findFolderId(int userId, String path) {
        String[] paths;
        if (ROOT.equals(path)) {
            paths = new String[1];
        } else {
            paths = path.split(ROOT);
        }
        paths[0] = ROOT;

        long folderId = 0;
        for (String item : paths) {
            if (ROOT.equals(item)) {
                Folder folder = folderMapper.selectOne(new LambdaQueryWrapper<Folder>()
                        .eq(Folder::getUserId, userId)
                        .eq(Folder::getName, item)
                        .isNull(Folder::getParentId));
                folderId = folder != null ? folder.getId() : 0;
            } else {
                if (folderId == 0)
                    break;
                Folder folder = folderMapper.selectOne(new LambdaQueryWrapper<Folder>()
                        .eq(Folder::getUserId, userId)
                        .eq(Folder::getName, item)
                        .eq(Folder::getParentId, folderId));
                folderId = folder != null ? folder.getId() : 0;
            }
        }
        return folderId;
    }

    @Override
    public void copyFolderRecursively(UserVO userVO, Long currentFolderId, Long targetFolderId, String folderName,
                                      UploadRequest uploadRequest) {
        folderHelper.copyFolderRecursively(userVO, currentFolderId, targetFolderId, folderName, uploadRequest);
    }

    @Override
    public Folder getOneByParentIdAndName(Long parentId, String name) {
        return this.getOne(new LambdaQueryWrapper<Folder>()
                .eq(Folder::getParentId, parentId).eq(Folder::getName, name));
    }

    @Override
    public Folder createFolderEntity(Integer userId, String name, Long parentId) {
        Folder folder = new Folder();
        folder.setUserId(userId);
        folder.setName(name);
        folder.setParentId(parentId);
        this.save(folder);
        return folder;
    }

    @Override
    public void deleteFolderRecursively(UserVO userVO, Long originFolderId) {
        folderHelper.deleteFolderRecursively(userVO, originFolderId);
    }

    @Override
    public Boolean removePhysicallyById(Long originFolderId) {
        return folderMapper.removePhysicallyById(originFolderId);
    }
}