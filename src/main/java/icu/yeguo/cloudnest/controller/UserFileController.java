package icu.yeguo.cloudnest.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.yeguo.cloudnest.annotation.SessionUser;
import icu.yeguo.cloudnest.common.Response;
import icu.yeguo.cloudnest.constant.UploadConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.dto.UploadRequest;
import icu.yeguo.cloudnest.model.entity.UserFile;
import icu.yeguo.cloudnest.model.vo.ChunkVO;
import icu.yeguo.cloudnest.model.vo.FileFolderVO;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.*;
import icu.yeguo.cloudnest.util.FileHandlerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "用户文件Controller")
@RestController
@RequestMapping("/userfile")
public class UserFileController {

    @Resource
    private IUserFileService userFileService;
    @Resource
    private IFileService fileService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IStorageQuotaService storageQuotaService;

    @Operation(summary = "获取目录下文件", parameters = {
            @Parameter(name = "path", description = "路径", schema = @Schema(type = "String"))
    },
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Response.class)
                    )
                    )
            })
    @SessionUser
    @GetMapping
    public Response<FileFolderVO> getUserFiles(@SessionUser UserVO userVO,
                                               @RequestParam("path") String path) {
        FileFolderVO fileVO = userFileService.getUserFiles(userVO, path);
        return Response.success(fileVO);
    }

    @Operation(summary = "创建文件", parameters = {
            @Parameter(name = "path", description = "路径", schema = @Schema(type = "String")),
            @Parameter(name = "name", description = "文件名", schema = @Schema(type = "String"))
    },
            responses = {
                    @ApiResponse(responseCode = "200", description = "成功", content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Response.class)
                    )
                    )
            })
    @SessionUser
    @PostMapping
    public Response<Long> createUserFile(@SessionUser UserVO userVO,
                                         @RequestParam("path") String path,
                                         @RequestParam("filename") String filename) throws IOException {
        Long id = userFileService.createUserFile(userVO, path, filename);
        return Response.success(id);
    }

    @GetMapping("/preview")
    public void previewFile(@RequestParam("filePath") String filePath,
                            HttpServletResponse response) throws IOException {

        Path path = Paths.get(filePath).normalize();
        String fileName = path.getFileName().toString();
        log.debug("预览文件：{}", path);
        FileHandlerUtils.previewFile(path, fileName, response);
    }

    @GetMapping("/download")
    public void downloadFile(@RequestParam("filePath") String filePath,
                             HttpServletResponse response) throws IOException {
        Path path = Paths.get(filePath).normalize();
        String fileName = path.getFileName().toString();
        log.debug("下载文件：{}", path);
        FileHandlerUtils.downloadFile(path, fileName, response);
    }

    @PostMapping("/rename")
    public Response<Integer> renameUserFile(@RequestParam("id") Long id,
                                            @RequestParam("name") String name) {
        int result = userFileService.renameUserFile(id, name);
        return Response.success(result);
    }

    @SessionUser
    @PostMapping("/upload/session")
    public Response<String> createUploadSession(@SessionUser UserVO userVO,
                                                @RequestParam("fingerprint") String fingerprint,
                                                @RequestParam("totalChunks") Integer totalChunks,
                                                @RequestParam("filename") String filename,
                                                @RequestParam("fileSize") Long fileSize,
                                                @RequestBody UploadRequest uploadRequest
    ) {
        // 判断空间是否充足
        storageQuotaService.checkAvailableSpace(userVO, fileSize, uploadRequest);
        // 检测当前文件是否有上传会话
        String redisHashKey = String.format("upload:session:info:user:%s:%s", userVO.getId(), fingerprint);
        if (stringRedisTemplate.opsForHash().hasKey(redisHashKey, UploadConstant.UPLOAD_ID)) {
            String uploadId = (String) stringRedisTemplate.opsForHash().get(redisHashKey, UploadConstant.UPLOAD_ID);
            log.debug("已存在会话：{}", uploadId);
            return Response.success(uploadId);
        }

        // 创建上传会话，redis初始化会话信息
        String uploadId = IdUtil.objectId();
        stringRedisTemplate.opsForHash().putIfAbsent(redisHashKey, UploadConstant.FINGERPRINT, fingerprint);
        stringRedisTemplate.opsForHash().putIfAbsent(redisHashKey, UploadConstant.UPLOAD_ID, uploadId);
        stringRedisTemplate.opsForHash().putIfAbsent(redisHashKey, UploadConstant.STATUS, UploadConstant.PENDING);
        stringRedisTemplate.opsForHash().putIfAbsent(redisHashKey, UploadConstant.TOTAL_CHUNKS, String.valueOf(totalChunks));
        stringRedisTemplate.opsForHash().putIfAbsent(redisHashKey, UploadConstant.CHUNK_DIR_PATH, "");
        stringRedisTemplate.opsForHash().putIfAbsent(redisHashKey, UploadConstant.FILENAME, filename);
        stringRedisTemplate.opsForHash().putIfAbsent(redisHashKey, UploadConstant.FILE_SIZE, String.valueOf(fileSize));
        stringRedisTemplate.expire(redisHashKey, 2, TimeUnit.HOURS);
        return Response.success(uploadId);
    }

    @SessionUser
    @GetMapping("/uploaded/chunks")
    public Response<Set<Integer>> getUploadedChunks(@SessionUser UserVO userVO,
                                                    @RequestParam("fingerprint") String fingerprint) {
        Set<Integer> set = Set.of();
        String redisSetKey = String.format("upload:session:parts:user:%s:%s", userVO.getId(), fingerprint);

        Set<String> members = stringRedisTemplate.opsForSet().members(redisSetKey);
        if (members == null) {
            return Response.success(set);
        }
        set = members.stream().map(Integer::parseInt).collect(Collectors.toSet());
        return Response.success(set);
    }

    @SessionUser
    @GetMapping("/uploaded/chunks/status")
    public Response<Boolean> getChunksStatus(@SessionUser UserVO userVO,
                                             @RequestParam("fingerprint") String fingerprint) {
        String redisHashKey = String.format("upload:session:info:user:%s:%s", userVO.getId(), fingerprint);
        String s = (String) stringRedisTemplate.opsForHash().get(redisHashKey, UploadConstant.STATUS);
        boolean b = UploadConstant.COMPLETED.equals(s);
        return Response.success(b);
    }

    @SessionUser
    @PostMapping("/upload/chunk/{uploadId}/{filename}/{chunkIndex}")
    public Response<ChunkVO> uploadChunk(
            @SessionUser UserVO userVO,
            @PathVariable("uploadId") String uploadId,
            @PathVariable("filename") String filename,
            @PathVariable("chunkIndex") int chunkIndex,
            @RequestParam("fingerprint") String fingerprint,
            @RequestParam("md5") String md5,
            @RequestParam("chunk") MultipartFile chunk,
            @RequestParam("totalChunks") int totalChunks,
            @RequestParam("uploadRequest") String uploadRequest,
            @RequestParam("fileSize") String fileSize
    ) throws IOException {
        UploadRequest bean = JSONUtil.toBean(uploadRequest, UploadRequest.class);
        List<String> uploadIds = bean.getUploadIds();
        long size = Long.parseLong(fileSize);
        ChunkVO result = userFileService.uploadChunk(userVO, uploadId, fingerprint, md5, chunk, filename, chunkIndex,
                totalChunks, uploadIds, size);
        return Response.success(result);
    }

    @SessionUser
    @PostMapping("/upload/merge")
    public Response<Integer> mergeChunks(@SessionUser UserVO userVO,
                                         @RequestParam("path") String path,
                                         @RequestParam("uploadId") String uploadId,
                                         @RequestParam("fingerprint") String fingerprint,
                                         @RequestParam("size") Long size,
                                         @RequestParam("filename") String filename,
                                         @RequestParam("totalChunks") Integer totalChunks,
                                         @RequestParam("webkitRelativePath") String webkitRelativePath
    ) throws IOException, NoSuchAlgorithmException, ExecutionException, InterruptedException {
        int result = userFileService.mergeChunks(userVO, path, uploadId, fingerprint, size, filename, totalChunks, webkitRelativePath);
        return Response.success(result, "上传成功");
    }

    @PutMapping("/move")
    public Response<?> moveUserFile(@RequestParam("userFileId") Long userFileId,
                                    @RequestParam("targetFolderId") Long targetFolderId) {

        UserFile userFile = userFileService.getById(userFileId);
        if (Objects.equals(userFile.getFolderId(), targetFolderId))
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "已在当前文件夹下");
        UserFile one = userFileService.getOne(new LambdaQueryWrapper<UserFile>().eq(UserFile::getFolderId, targetFolderId)
                .eq(UserFile::getFileName, userFile.getFileName()));

        if (one != null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "已存在同名文件");

        boolean b = userFileService.lambdaUpdate().eq(UserFile::getId, userFileId).set(UserFile::getFolderId, targetFolderId).update();
        if (!b)
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件移动失败");
        return Response.success(null, "文件移动成功");
    }

    @SessionUser
    @PostMapping("/copy")
    public Response<?> copyUserFile(@SessionUser UserVO userVO,
                                    @RequestParam("fileSize") Long fileSize,
                                    @RequestParam("userFileId") Long userFileId,
                                    @RequestParam("targetFolderId") Long targetFolderId,
                                    @RequestBody UploadRequest uploadRequest) {
        storageQuotaService.checkAvailableSpace(userVO, fileSize, uploadRequest);

        UserFile userFile = userFileService.getById(userFileId);
        UserFile one = userFileService.getOne(new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getFolderId, targetFolderId)
                .eq(UserFile::getFileName, userFile.getFileName()));
        if (one != null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "已存在同名文件");

        userFile.setId(null);
        userFile.setFolderId(targetFolderId);

        boolean save = userFileService.save(userFile);
        if (!save) {
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件复制失败");
        }

        fileService.increaseReferenceCount(userFile.getFileId());

        storageQuotaService.updateUserStorage(userVO, fileSize);
        return Response.success(null, "文件复制成功");
    }

    @SessionUser
    @DeleteMapping
    public Response<?> deleteUserFile(@SessionUser UserVO userVO,
                                      @RequestParam("userFileId") Long userFileId,
                                      @RequestParam("fileId") Long fileId,
                                      @RequestParam("fileSize") Long fileSize) {
        boolean b = userFileService.removePhysicallyById(userFileId);
        if (!b) {
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件删除失败");
        }

        fileService.decreaseReferenceCount(fileId);

        storageQuotaService.updateUserStorage(userVO, -fileSize);
        return Response.success(null, "文件删除成功");
    }

    @SessionUser
    @GetMapping("/search")
    public Response<FileFolderVO> searchUserFile(@SessionUser UserVO userVO, @RequestParam("keyword") String
            keyword) {
        if (keyword.isEmpty()) {
            return Response.success(null);
        }
        FileFolderVO fileFolderVO = userFileService.searchUserFile(userVO, keyword);
        return Response.success(fileFolderVO);
    }

    @SessionUser
    @GetMapping("/search/type")
    public Response<FileFolderVO> searchUserFileByType(@SessionUser UserVO userVO, @RequestParam("type") String
            type) {
        FileFolderVO fileFolderVO = userFileService.searchUserFileByType(userVO, type);
        return Response.success(fileFolderVO);
    }
}
