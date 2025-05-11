package icu.yeguo.cloudnest.service;

import icu.yeguo.cloudnest.model.dto.FileFolderItem;
import icu.yeguo.cloudnest.model.entity.UserFile;
import com.baomidou.mybatisplus.extension.service.IService;
import icu.yeguo.cloudnest.model.vo.ChunkVO;
import icu.yeguo.cloudnest.model.vo.FileFolderVO;
import icu.yeguo.cloudnest.model.vo.AdminUserFileVO;
import icu.yeguo.cloudnest.model.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
public interface IUserFileService extends IService<UserFile> {
    FileFolderVO getUserFiles(UserVO userVO, String path);

    Long createUserFile(UserVO userVO, String path, String filename) throws IOException;

    long saveFolder(Path webkitParentPath, long parentId, int userId);

    int renameUserFile(Long id, String name);

    List<FileFolderItem> getUserFilesByFolderId(Long folder);

    ChunkVO uploadChunk(UserVO userVO, String uploadId, String fingerprint, String md5, MultipartFile chunk, String filename,
                        int chunkIndex, int totalChunks, List<String> uploadIds, Long fileSize) throws IOException;

    int mergeChunks(UserVO userVO, String path, String uploadId, String fingerprint, long size, String filename, int totalChunks, String webkitRelativePath) throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException;

    List<AdminUserFileVO> getUserFilesByAdmin();

    Boolean removePhysicallyById(Long userFileId);

    FileFolderVO searchUserFile(UserVO userVO, String keyword);

    FileFolderVO searchUserFileByType(UserVO userVO, String type);
}
