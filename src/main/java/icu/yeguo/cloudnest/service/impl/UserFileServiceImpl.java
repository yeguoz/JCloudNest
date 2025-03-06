package icu.yeguo.cloudnest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.constant.CommonConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.mapper.FileMapper;
import icu.yeguo.cloudnest.mapper.FolderMapper;
import icu.yeguo.cloudnest.model.dto.FileDTO;
import icu.yeguo.cloudnest.model.entity.File;
import icu.yeguo.cloudnest.model.entity.Folder;
import icu.yeguo.cloudnest.model.entity.UserFile;
import icu.yeguo.cloudnest.model.vo.FileVO;
import icu.yeguo.cloudnest.service.CommonService;
import icu.yeguo.cloudnest.service.IFileService;
import icu.yeguo.cloudnest.service.IUserFileService;
import icu.yeguo.cloudnest.mapper.UserFileMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
@Slf4j
@Service
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFile>
        implements IUserFileService {
    private final String FOLDER = "folder";
    private final String FILE = "file";
    private final String HYPHEN = "-";

    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private UserFileMapper userFileMapper;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private IFileService fileService;
    @Autowired
    private CommonService commonService;

    @Override
    public FileVO getUserFiles(Integer userId, String path) {
        long folderId = commonService.findFolderId(userId, path);
        log.debug("folderId: {}", folderId);
        if (folderId == 0)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件夹不存在");
        // 通过目录id 查询当前目录下所有子目录
        List<FileDTO> subfolderList = folderMapper
                .selectList(new LambdaQueryWrapper<Folder>().eq(Folder::getParentId, folderId))
                .stream()
                .map(f -> {
                    FileDTO fileDTO = new FileDTO();
                    fileDTO.setId(UUID.randomUUID().toString()
                            .replace(HYPHEN, CommonConstant.EMPTY).substring(0, 8));
                    fileDTO.setUserId(f.getUserId());
                    fileDTO.setName(f.getName());
                    fileDTO.setParentId(f.getParentId());
                    fileDTO.setUpdatedAt(f.getUpdatedAt());
                    fileDTO.setType(FOLDER);
                    return fileDTO;
                })
                .toList();
        log.debug("subfolderList: {}", subfolderList);
        // 通过目录id查询用户文件下所有文件
        List<UserFile> userFileList = userFileMapper.selectList(new LambdaQueryWrapper<>(UserFile.class)
                .eq(UserFile::getFolderId, folderId));
        log.debug("userFileList: {}", userFileList);
        List<FileDTO> fileList = null;
        if (!userFileList.isEmpty()) {
            List<Long> fileIdList = userFileList.stream().map(UserFile::getFileId).toList();
            // 通过所有文件id查询所有文件
            List<File> files = fileMapper.selectByIds(fileIdList);
            // 整合数据
            Map<Long, UserFile> userfileMap = userFileList.stream()
                    .collect(Collectors.toMap(UserFile::getFileId, userFile -> userFile));

            fileList = files.stream()
                    .map(file -> {
                        UserFile userFile = userfileMap.get(file.getId());
                        if (userFile == null)
                            return null;
                        FileDTO fileDTO = new FileDTO();
                        fileDTO.setId(UUID.randomUUID().toString()
                                .replace(HYPHEN, CommonConstant.EMPTY).substring(0, 8));
                        fileDTO.setUserId(userFile.getUserId());
                        fileDTO.setName(userFile.getFileName());
                        fileDTO.setSize(file.getSize());
                        fileDTO.setFileHash(file.getFileHash());
                        fileDTO.setSourceName(file.getSourceName());
                        fileDTO.setFolderId(userFile.getFolderId());
                        fileDTO.setUpdatedAt(userFile.getUpdatedAt());
                        fileDTO.setType(FILE);
                        return fileDTO;
                    }).toList();
            log.debug("fileList: {}", fileList);
        }
        List<FileDTO> combinedList = Stream
                .concat(subfolderList.stream(), fileList != null ? fileList.stream() : Stream.empty())
                .toList();
        return new FileVO(combinedList);
    }

    @Transactional
    @Override
    public Long createUserFile(Integer userId, String path, String name) throws IOException {
        long folderId = commonService.findFolderId(userId, path);
        if (folderId == 0)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件夹不存在");
        UserFile userFile;
        File file = null;
        try {
            // 创建文件
            file = fileService.createFile(userId, path, name);
            // 该folder下不能有同名的文件
            Long count = userFileMapper.selectCount(new LambdaQueryWrapper<>(UserFile.class)
                    .eq(UserFile::getFolderId, folderId)
                    .eq(UserFile::getFileName, name));
            if (count > 0)
                throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "该文件夹下已有同名文件");
            // 用户文件表插入数据
            userFile = new UserFile();
            userFile.setUserId(userId);
            userFile.setFileId(file.getId());
            userFile.setFileName(name);
            userFile.setFolderId(folderId);
            int i = userFileMapper.insert(userFile);
            if (i < 1)
                throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "创建文件失败");
            return userFile.getId();
        } catch (Exception e) {
            // 发生异常 删除创建的物理文件
            if (file != null) {
                try {
                    Path filePath = Paths.get(file.getSourceName());
                    Files.deleteIfExists(filePath);
                } catch (IOException ioException) {
                    throw new RuntimeException("删除文件失败：" + file.getSourceName(), ioException);
                }
            }
            throw e;
        }
    }
}




