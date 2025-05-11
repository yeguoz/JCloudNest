package icu.yeguo.cloudnest.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.constant.UploadConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.dto.FileFolderItem;
import icu.yeguo.cloudnest.model.dto.MergedFileInfo;
import icu.yeguo.cloudnest.model.entity.*;
import icu.yeguo.cloudnest.model.vo.ChunkVO;
import icu.yeguo.cloudnest.model.vo.FileFolderVO;
import icu.yeguo.cloudnest.model.vo.AdminUserFileVO;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.*;
import icu.yeguo.cloudnest.mapper.UserFileMapper;
import icu.yeguo.cloudnest.util.FileTypeClassifier;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.ApplicationContext;

import static icu.yeguo.cloudnest.constant.CommonConstant.*;
import static icu.yeguo.cloudnest.constant.PlaceholderConstant.*;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
@Slf4j
@Service
public class UserFileServiceImpl extends ServiceImpl<UserFileMapper, UserFile>
        implements IUserFileService {
    private final int FIRST256KB = 256 * 1024;

    @Resource
    private Executor threadPoolExecutor;
    @Resource
    private UserFileMapper userFileMapper;
    @Resource
    private IFileService fileService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private IStorageQuotaService storageQuotaService;
    @Resource
    private IFolderService folderService;

    @Override
    public FileFolderVO getUserFiles(UserVO userVO, String path) {
        long folderId = folderService.findFolderId(userVO.getId(), path);
        log.debug("folderId: {}", folderId);
        if (folderId == 0)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件夹不存在");
        List<FileFolderItem> list = getUserFilesByFolderId(folderId);
        return new FileFolderVO(list);
    }

    @Override
    public List<FileFolderItem> getUserFilesByFolderId(Long folderId) {
        List<FileFolderItem> subfolderList = folderService
                .list(new LambdaQueryWrapper<Folder>().eq(Folder::getParentId, folderId))
                .stream()
                .map(f -> {
                    FileFolderItem fileFolderItem = new FileFolderItem();
                    fileFolderItem.setId(IdUtil.simpleUUID());
                    fileFolderItem.setFolderId(f.getId());
                    fileFolderItem.setUserId(f.getUserId());
                    fileFolderItem.setName(f.getName());
                    fileFolderItem.setParentId(f.getParentId());
                    fileFolderItem.setUpdatedAt(f.getUpdatedAt());
                    fileFolderItem.setType(FOLDER);
                    return fileFolderItem;
                })
                .toList();
        log.debug("subfolderList: {}", subfolderList);

        // 通过目录id查询用户文件下所有文件
        List<UserFile> userFileList = userFileMapper.selectList(new LambdaQueryWrapper<>(UserFile.class)
                .eq(UserFile::getFolderId, folderId));
        log.debug("userFileList: {}", userFileList);

        List<FileFolderItem> fileList = mergeUserFilesWithFiles(userFileList);

        return Stream
                .concat(subfolderList.stream(), fileList != null ? fileList.stream() : Stream.empty())
                .toList();
    }

    private List<FileFolderItem> mergeUserFilesWithFiles(List<UserFile> userFileList) {
        List<FileFolderItem> fileList = null;
        if (!userFileList.isEmpty()) {
            List<Long> fileIdList = userFileList.stream().map(UserFile::getFileId).toList();
            // 通过所有文件id查询所有文件
            List<File> files = fileService.listByIds(fileIdList);
            // 整合数据
            Map<Long, List<UserFile>> userfileMap = userFileList.stream()
                    .collect(Collectors.groupingBy(UserFile::getFileId));

            fileList = files.stream()
                    .map(file -> {
                        List<UserFile> userFiles = userfileMap.get(file.getId());
                        if (userFiles == null)
                            return null;
                        return userFiles.stream()
                                .map(userFile -> {
                                    FileFolderItem fileFolderItem = new FileFolderItem();
                                    fileFolderItem.setId(IdUtil.simpleUUID());
                                    fileFolderItem.setUserId(userFile.getUserId());
                                    fileFolderItem.setName(userFile.getFileName());
                                    fileFolderItem.setUserFileId(userFile.getId());
                                    fileFolderItem.setUpdatedAt(userFile.getUpdatedAt());
                                    fileFolderItem.setFileId(file.getId());
                                    fileFolderItem.setSize(file.getSize());
                                    fileFolderItem.setFileHash(file.getFileHash());
                                    fileFolderItem.setHeaderHash(file.getHeaderHash());
                                    fileFolderItem.setSourceName(file.getSourceName());
                                    fileFolderItem.setType(FILE);
                                    return fileFolderItem;
                                })
                                .toList();
                    }).filter(Objects::nonNull).flatMap(List::stream).toList();
            log.debug("fileList: {}", fileList);
        }
        return fileList;
    }

    @Transactional
    @Override
    public Long createUserFile(UserVO userVO, String path, String filename) throws IOException {
        int userId = userVO.getId();
        long folderId = folderService.findFolderId(userId, path);
        if (folderId == 0)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件夹不存在");
        UserFile userFile;
        File file = null;
        try {
            // 创建文件
            file = fileService.createFile(userVO, path, filename);
            // 该folder下不能有同名的文件
            Long count = userFileMapper.selectCount(new LambdaQueryWrapper<>(UserFile.class)
                    .eq(UserFile::getFolderId, folderId)
                    .eq(UserFile::getFileName, filename));
            if (count > 0)
                throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "该文件夹下已有同名文件");
            // 用户文件表插入数据
            userFile = new UserFile();
            userFile.setUserId(userId);
            userFile.setFileId(file.getId());
            userFile.setFileName(filename);
            userFile.setFolderId(folderId);
            String extension = FileUtil.getSuffix(filename);
            String fileType = FileTypeClassifier.classifyFile(extension);
            userFile.setFileType(fileType);

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

    @Override
    public int renameUserFile(Long id, String name) {
        UserFile userFile = userFileMapper.selectById(id);
        if (userFile == null)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件不存在");
        userFile.setFileName(name);
        return userFileMapper.updateById(userFile);
    }

    public void deleteFileAsync(Path filePath) {
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < 3; i++) { // 最多重试3次
                try {
                    if (Files.deleteIfExists(filePath)) {
                        Files.deleteIfExists(filePath.getParent());
                        log.debug("文件删除成功：{}", filePath);
                        log.debug("文件目录删除成功：{}", filePath.getParent());
                    } else {
                        log.debug("文件不存在，无需删除：{}", filePath);
                    }
                    return; // 成功删除，直接返回
                } catch (IOException e) {
                    log.warn("删除文件失败(第 {} 次)，文件：{}，错误：{}", (i + 1), filePath, e.getMessage());
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt(); // 恢复中断状态
                        log.error("删除文件线程中断：{}", filePath);
                    }
                }
            }
            log.error("删除文件失败 (重试 3 次后放弃): {}", filePath);
        }, threadPoolExecutor);
    }

    public void deleteChunksAsync(Path chunkDirPath) {
        CompletableFuture.runAsync(() -> {
            if (!Files.exists(chunkDirPath)) {
                log.debug("分片目录不存在，无需删除: {}", chunkDirPath);
                return;
            }

            // 删除文件
            try (Stream<Path> paths = Files.walk(chunkDirPath)) {
                List<Path> sortedPaths = paths.sorted(Comparator.reverseOrder()).toList();

                for (Path path : sortedPaths) {
                    for (int i = 0; i < 3; i++) {
                        try {
                            if (Files.isRegularFile(path)) {
                                Files.delete(path);
                                log.debug("分片删除成功: {}", path);
                            } else if (Files.isDirectory(path)) {
                                Files.delete(path);
                                log.debug("分片目录删除成功: {}", path);
                                Path parent = path.getParent();
                                Files.deleteIfExists(parent);
                                log.debug("父目录删除成功: {}", parent);
                            }
                            break;
                        } catch (IOException e) {
                            log.warn("分片删除失败 (第 {} 次)，路径: {}，错误: {}", (i + 1), path, e.getMessage());
                            try {
                                TimeUnit.MILLISECONDS.sleep(1000);
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                                log.error("分片删除线程中断：{}", chunkDirPath);
                                return;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error("遍历目录失败: {}", e.getMessage());
            }
        }, threadPoolExecutor);
    }

    @Override
    public ChunkVO uploadChunk(UserVO userVO, String uploadId, String fingerprint, String md5, MultipartFile chunk,
                               String filename, int chunkIndex, int totalChunks, List<String> uploadIds, Long fileSize) throws IOException {
        // 校验分片md5值
        String chunkMd5 = DigestUtils.md5DigestAsHex(chunk.getBytes());
        log.debug("请求MD5:{}", md5);
        log.debug("检验MD5:{}", chunkMd5);
        if (!md5.equals(chunkMd5)) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "MD5检验错误，文件损坏或被篡改");
        }

        String redisSetKey = String.format("upload:session:parts:user:%s:%s", userVO.getId(), fingerprint);
        String redisHashKey = String.format("upload:session:info:user:%s:%s", userVO.getId(), fingerprint);
        // 分片文件名
        String chunkDir = userVO.getPolicy().getChunkDirNameRule()
                .replace(UPLOAD_ID_PLACEHOLDER, uploadId)
                .replace(FINGERPRINT_PLACEHOLDER, fingerprint);
        String chunkFile = userVO.getPolicy().getChunkFileNameRule()
                .replace(INDEX_PLACEHOLDER, String.valueOf(chunkIndex));
        Path chunkDirPath = Paths.get(chunkDir);
        Path chunkFilePath = chunkDirPath.resolve(chunkFile);
        try {
            if (!Files.exists(chunkDirPath)) {
                Files.createDirectories(chunkDirPath);
            }
            if (!Files.exists(chunkFilePath)) {
                Files.createFile(chunkFilePath);
            }
            // 写入分片数据
            try (FileChannel channel = FileChannel.open(chunkFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 FileLock ignored1 = channel.lock()) {
                int ignored2 = channel.write(ByteBuffer.wrap(chunk.getBytes()));
            }
            log.debug("分片 {} 已写入到文件:{} ", chunkIndex, chunkFile);

            // 更新当前分片
            String chunkIndexStr = String.valueOf(chunkIndex);
            String script =
                    "redis.call('sadd', KEYS[1], ARGV[1]); " +
                            "redis.call('expire', KEYS[1], tonumber(ARGV[2])); " +
                            "redis.call('hset', KEYS[2], ARGV[3], ARGV[4]); " +
                            "redis.call('expire', KEYS[2], tonumber(ARGV[2])); " +
                            "return 1;";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            stringRedisTemplate.execute(
                    redisScript,
                    Arrays.asList(redisSetKey, redisHashKey),
                    chunkIndexStr,
                    String.valueOf(TimeUnit.HOURS.toSeconds(2)),
                    UploadConstant.CHUNK_DIR_PATH,
                    chunkDir
            );

            Set<String> members = stringRedisTemplate.opsForSet().members(redisSetKey);
            // 已上传分片
            List<Integer> chunks = new ArrayList<>();
            if (members != null) {
                chunks = members.stream().map(Integer::parseInt).toList();
            }

            if (chunks.size() == totalChunks) {
                String lockKey = "lock:uploadChunk:" + uploadId;
                RLock lock = redissonClient.getLock(lockKey);
                boolean locked = false;
                try {
                    locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
                    if (!locked) {
                        return new ChunkVO(uploadId, fingerprint, filename, chunkIndex, chunk.getSize(), totalChunks,
                                chunks.size(), false);
                    }
                    log.debug("获得锁,uploadId: {},线程：{}", uploadId, Thread.currentThread().getId());
                    String status = (String) stringRedisTemplate.opsForHash().get(redisHashKey, UploadConstant.STATUS);
                    if (UploadConstant.PROCESSING.equals(status)) {
                        return new ChunkVO(uploadId, fingerprint, filename, chunkIndex, chunk.getSize(), totalChunks,
                                chunks.size(), false);
                    }
                    stringRedisTemplate.opsForHash().put(redisHashKey, UploadConstant.STATUS, UploadConstant.PROCESSING);
                    return new ChunkVO(uploadId, fingerprint, filename, chunkIndex, chunk.getSize(), totalChunks,
                            chunks.size(), true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "处理失败");
                } finally {
                    if (locked && lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.debug("锁已释放,uploadId: {},线程：{}", uploadId, Thread.currentThread().getId());
                    }
                }
            }
            return new ChunkVO(uploadId, fingerprint, filename, chunkIndex, chunk.getSize(), totalChunks,
                    chunks.size(), false);
        } catch (Exception e) {
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public int mergeChunks(UserVO userVO, String path, String uploadId, String fingerprint, long size, String filename,
                           int totalChunks, String webkitRelativePath) {
        long currentFolderId = folderService.findFolderId(userVO.getId(), path);
        if (currentFolderId < 1) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "父文件夹不存在");
        }
        String subPath = webkitRelativePath.isEmpty()
                ? ""
                : webkitRelativePath.substring(0, webkitRelativePath.lastIndexOf(ROOT));
        Path webkitParentPath = Paths.get(subPath);

        String redisHashKey = String.format("upload:session:info:user:%s:%s", userVO.getId(), fingerprint);
        String redisSetKey = String.format("upload:session:parts:user:%s:%s", userVO.getId(), fingerprint);

        String chunkDir = userVO.getPolicy().getChunkDirNameRule()
                .replace(UPLOAD_ID_PLACEHOLDER, uploadId)
                .replace(FINGERPRINT_PLACEHOLDER, fingerprint);

        Path chunkDirPath = Paths.get(chunkDir);
        String sourceName = "";

        String lockKey = "lock:saveFolder:" + userVO.getId();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            MergedFileInfo mergedFileInfo = handleFileMerge(userVO, chunkDirPath, totalChunks, filename, webkitRelativePath);
            sourceName = mergedFileInfo.getSourceName();
            String hash = mergedFileInfo.getHash();
            String headerHash = mergedFileInfo.getFirst256KBHash();

            try {
                locked = lock.tryLock(30, 30, TimeUnit.SECONDS);
                if (!locked) {
                    throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "无法获取文件夹保存锁，请稍后重试");
                }
                UserFileServiceImpl proxy = applicationContext.getBean(UserFileServiceImpl.class);
                proxy.handleDataInsertion(userVO, size, hash, headerHash, filename, webkitParentPath, currentFolderId,
                        sourceName);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("线程中断，保存文件夹失败", e);
            } finally {
                if (locked && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
            stringRedisTemplate.opsForHash().put(redisHashKey, UploadConstant.STATUS, UploadConstant.COMPLETED);
            stringRedisTemplate.delete(redisSetKey);
            stringRedisTemplate.delete(redisHashKey);
            deleteChunksAsync(chunkDirPath);
        } catch (Exception e) {
            // 异步处理合并完成的文件
            deleteFileAsync(Paths.get(sourceName));
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        return 1;
    }

    @Override
    public List<AdminUserFileVO> getUserFilesByAdmin() {
        return userFileMapper.getUserFilesByAdmin();
    }

    @Override
    public Boolean removePhysicallyById(Long userFileId) {
        return userFileMapper.removePhysicallyById(userFileId);
    }

    @Override
    public FileFolderVO searchUserFile(UserVO userVO, String keyword) {
        List<UserFile> userFileList = userFileMapper.selectList(new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getUserId, userVO.getId())
                .like(UserFile::getFileName, keyword));
        List<FileFolderItem> fileList = mergeUserFilesWithFiles(userFileList);
        return new FileFolderVO(fileList);
    }

    @Override
    public FileFolderVO searchUserFileByType(UserVO userVO, String type) {
        List<UserFile> userFileList = userFileMapper.selectList(new LambdaQueryWrapper<UserFile>()
                .eq(UserFile::getUserId, userVO.getId())
                .like(UserFile::getFileType, type));
        List<FileFolderItem> fileList = mergeUserFilesWithFiles(userFileList);
        return new FileFolderVO(fileList);
    }

    @Transactional
    protected void handleDataInsertion(UserVO userVO, long size, String hash, String headerHash, String filename,
                                       Path webkitParentPath, long parentId, String sourceName) {
        long targetFolderId = saveFolder(webkitParentPath, parentId, userVO.getId());
        saveFileAndUserFile(userVO, size, hash, headerHash, filename, targetFolderId, sourceName);
    }

    public long saveFolder(Path webkitParentPath, long parentId, int userId) {
        if (webkitParentPath.toString().isEmpty()) return parentId;
        for (int i = 0; i < webkitParentPath.getNameCount(); i++) {
            String folderName = String.valueOf(webkitParentPath.getName(i));

            Folder one = folderService.getOne(new LambdaQueryWrapper<Folder>()
                    .eq(Folder::getParentId, parentId)
                    .eq(Folder::getName, folderName)
                    .eq(Folder::getUserId, userId));

            if (one == null) {
                Folder newFolder = new Folder();
                newFolder.setUserId(userId);
                newFolder.setName(folderName);
                newFolder.setParentId(parentId);
                folderService.save(newFolder);
                parentId = newFolder.getId();
            } else {
                parentId = one.getId();
            }
        }
        return parentId;
    }


    private MergedFileInfo handleFileMerge(UserVO userVO, Path chunkDirPath, int totalChunks, String filename,
                                           String webkitRelativePath) {
        List<Path> chunkFiles = new ArrayList<>();
        Path tempMergedFilePath = chunkDirPath.resolve("full_file");
        String extension = FileUtil.getSuffix(filename);

        log.debug("正在合并分片,路径：{}", chunkDirPath);
        for (int i = 0; i < totalChunks; i++) {
            Path chunkPartPath = chunkDirPath.resolve(i + DOT_PART);
            if (!Files.exists(chunkPartPath)) {
                throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "chunk:" + i + DOT_PART + "不存在," + webkitRelativePath);
            }
            chunkFiles.add(chunkPartPath);
        }

        String sourceName;
        String sha256Hash = "";
        String first256KBHash = "";
        try (FileChannel outChannel = FileChannel.open(tempMergedFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            for (Path chunkFile : chunkFiles) {
                try (FileChannel inChannel = FileChannel.open(chunkFile, StandardOpenOption.READ)) {
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                }
            }

            try (FileInputStream fileInputStream = new FileInputStream(String.valueOf(tempMergedFilePath))) {
                sha256Hash = DigestUtil.sha256Hex(fileInputStream);
                log.debug("文件 SHA-256 Hex:{} ", sha256Hash);
            } catch (IOException e) {
                log.error("计算文件Sha-256异常:{}", (Object) e.getStackTrace());
                log.error("分片路径:{}", chunkDirPath);
                throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "计算文件hash失败");
            }

            try (FileInputStream first256Stream = new FileInputStream(tempMergedFilePath.toFile())) {
                byte[] buffer = new byte[FIRST256KB];
                int read = first256Stream.read(buffer);
                if (read > 0) {
                    byte[] actualBytes = new byte[read];
                    System.arraycopy(buffer, 0, actualBytes, 0, read);
                    first256KBHash = DigestUtil.sha256Hex(actualBytes);
                    log.debug("前256KB SHA256: {}", first256KBHash);
                } else {
                    log.debug("读取前256KB失败:{}", chunkDirPath);
                    throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "读取前256KB失败");
                }
            }

            String fileDir = userVO.getPolicy().getFileDirNameRule()
                    .replace(HASH2_PLACEHOLDER, sha256Hash.substring(0, 2));
            String targetFilename = userVO.getPolicy().getFileNameRule()
                    .replace(HASH_PLACEHOLDER, sha256Hash) + DOT + extension;
            sourceName = fileDir + ROOT + targetFilename;
            Path fileDirPath = Paths.get(fileDir);
            Path filePath = fileDirPath.resolve(targetFilename);
            log.debug("目标文件路径：{}", filePath);

            // 确保目标目录存在
            if (!Files.exists(fileDirPath)) {
                Files.createDirectories(fileDirPath);
            }

            Files.move(tempMergedFilePath, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("文件合并完成，文件已保存到：{}", filePath);

        } catch (IOException e) {
            log.error("合并分片文件失败: ", e);
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "合并分片文件失败");
        }
        return new MergedFileInfo(sourceName, sha256Hash, first256KBHash);
    }

    private void saveFileAndUserFile(UserVO userVO, long size, String hash, String headerHash, String filename,
                                     long folderId, String sourceName) {
        try {
            Long l = userFileMapper.selectCount(new LambdaQueryWrapper<UserFile>()
                    .eq(UserFile::getFileName, filename)
                    .eq(UserFile::getFolderId, folderId)
                    .eq(UserFile::getUserId, userVO.getId()));
            if (l > 0) {
                filename = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_" + filename;
            }

            File one = fileService.getOne(new LambdaQueryWrapper<File>()
                    .eq(File::getFileHash, hash)
                    .eq(File::getHeaderHash, headerHash)
                    .eq(File::getSize, size));

            long fileId = 0;
            if (one != null) {
                fileService.increaseReferenceCount(one.getId());
                fileId = one.getId();
            } else {
                File file = new File();
                file.setSize(size);
                file.setFileHash(hash);
                file.setHeaderHash(headerHash);
                file.setSourceName(sourceName);
                file.setReferenceCount(1);
                fileService.save(file);
                fileId = file.getId();
            }

            UserFile userFile = new UserFile();
            userFile.setUserId(userVO.getId());
            userFile.setFileId(fileId);
            userFile.setFileName(filename);
            userFile.setFolderId(folderId);
            String extension = FileUtil.getSuffix(filename);
            String fileType = FileTypeClassifier.classifyFile(extension);
            userFile.setFileType(fileType);
            userFileMapper.insert(userFile);

            // 更新用户存储空间大小
            storageQuotaService.updateUserStorage(userVO, size);
        } catch (Exception e) {
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
