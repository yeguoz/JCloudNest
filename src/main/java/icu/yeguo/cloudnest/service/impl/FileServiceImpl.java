package icu.yeguo.cloudnest.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.yeguo.cloudnest.constant.CommonConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.entity.File;
import icu.yeguo.cloudnest.model.entity.Policy;
import icu.yeguo.cloudnest.service.IFileService;
import icu.yeguo.cloudnest.mapper.FileMapper;
import icu.yeguo.cloudnest.service.IPolicyService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

/**
 * @author yeguo
 * @createDate 2025-02-15 18:13:03
 */
@Slf4j
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File>
        implements IFileService {


    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private IPolicyService policyService;

    @Override
    public File createFile(int userId, String path, String name) throws IOException {
        String UID = "{uid}";
        String PATH = "{path}";
        String TIMESTAMP = "{timestamp}";
        String RANDOM5 = "{random5}";
        String FILENAME = "{filename}";
        Policy policy = policyService.findPolicyByUserId(userId);
        String privateDirNameRule = policy.getPrivateDirNameRule();
        String privateFileNameRule = policy.getPrivateFileNameRule();
        String folderName = privateDirNameRule.replace(UID, String.valueOf(userId))
                .replace(PATH, path.replaceFirst(CommonConstant.ROOT, CommonConstant.EMPTY));
        String fileName = privateFileNameRule.replace(TIMESTAMP, String.valueOf(System.currentTimeMillis()))
                .replace(RANDOM5, String.valueOf((int) (Math.random() * 90000) + 10000))
                .replace(FILENAME, name);
        String sourceName = folderName + CommonConstant.EMPTY + fileName;

        File file = new File();
        file.setSize(0L);
        file.setSourceName(sourceName);
        // 插入数据
        int i = fileMapper.insert(file);
        if (i < 1)
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件创建失败");
        // 创建物理文件
        Path folderPath = Paths.get(folderName);
        Path sourceNamePath = Paths.get(sourceName);
        if (Files.exists(sourceNamePath))
            throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "文件已存在");
        if (!Files.exists(folderPath)) {
            Files.createDirectories(folderPath);
        }
        Files.createFile(sourceNamePath);
        return file;
    }
}




