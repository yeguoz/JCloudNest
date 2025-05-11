package icu.yeguo.cloudnest.service;

import icu.yeguo.cloudnest.model.dto.UploadRequest;
import icu.yeguo.cloudnest.model.entity.Folder;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.yeguo.cloudnest.model.vo.UserVO;

import java.io.IOException;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
public interface IFolderService extends IService<Folder> {
    Long createFolder(UserVO userVO, String path, String name) throws IOException;

    int renameFolder(Long id, String name);

    Long queryFolderIdByRelativePath(Long parentId, String relativePath);

    long findFolderId(int userId, String path);

    void copyFolderRecursively(UserVO userVO, Long originFolderId, Long targetFolderId, String folderName, UploadRequest uploadRequest);

    Folder getOneByParentIdAndName(Long targetFolderId, String folderName);

    Folder createFolderEntity(Integer userId, String name, Long parentId);

    void deleteFolderRecursively(UserVO userVO, Long originFolderId);

    Boolean removePhysicallyById(Long originFolderId);
}
