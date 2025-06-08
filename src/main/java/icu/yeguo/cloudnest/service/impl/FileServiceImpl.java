package icu.yeguo.cloudnest.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.entity.File;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.IFileService;
import icu.yeguo.cloudnest.mapper.FileMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

import static icu.yeguo.cloudnest.constant.PlaceholderConstant.*;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
        implements IFileService {

    @Resource
    private FileMapper fileMapper;

    @Override
    public File createFile(UserVO userVO, String path, String filename) throws IOException {
        String sourceName = userVO.getPolicy().getEmptyFileNameRule()
                .replace(UID_PLACEHOLDER, String.valueOf(userVO.getId()))
                .replace(UUID_PLACEHOLDER, IdUtil.simpleUUID())
                .replace(FILENAME_PLACEHOLDER, filename);

        // 创建物理文件
        Path sourceNamePath = Paths.get(sourceName);
        Path parentDir = sourceNamePath.getParent();
        if (Files.exists(sourceNamePath)) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件已存在");
        }
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        Files.createFile(sourceNamePath);


        File file = new File();
        file.setSize(0L);
        file.setSourceName(sourceName);
        file.setReferenceCount(1);
        // 插入数据
        int i = fileMapper.insert(file);
        if (i < 1) {
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件创建失败");
        }
        return file;
    }

    @Override
    public Boolean increaseReferenceCount(Long fileId) {
        return this.lambdaUpdate()
                .eq(File::getId, fileId)
                .setSql("reference_count = reference_count + 1")
                .update();
    }

    @Override
    public Boolean decreaseReferenceCount(Long fileId) {
        return this.lambdaUpdate()
                .eq(File::getId, fileId)
                .setSql("reference_count = reference_count - 1")
                .update();
    }

    @Override
    public Boolean deleteByIdPhysically(Long id) {
        return fileMapper.deleteByIdPhysically(id);
    }
}