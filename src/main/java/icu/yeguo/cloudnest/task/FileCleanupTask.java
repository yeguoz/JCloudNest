package icu.yeguo.cloudnest.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import icu.yeguo.cloudnest.model.entity.File;
import icu.yeguo.cloudnest.service.IFileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class FileCleanupTask {

    @Resource
    private Executor threadPoolExecutor;
    @Resource
    private IFileService fileService;

    @Scheduled(cron = "0 0 4 * * ?")
    public void runAsync() {
        CompletableFuture.runAsync(() -> {
            log.info("开始执行已无引用文件清理任务");

            int currentPage = 1;
            int pageSize = 10;
            while (true) {
                Page<File> page = new Page<>(currentPage, pageSize);
                Page<File> filePage = fileService.page(page, new LambdaQueryWrapper<File>().eq(File::getReferenceCount, 0));
                List<File> fileList = filePage.getRecords();
                if (fileList.isEmpty()) {
                    break;
                }

                fileList.forEach(file -> {
                    Path path = Paths.get(file.getSourceName());
                    AtomicReference<Boolean> b = new AtomicReference<>(false);
                    // 删除物理文件
                    try {
                        b.set(Files.deleteIfExists(path));
                        if (b.get()) {
                            log.info("删除文件成功: {}", file.getSourceName());
                            boolean b1 = Files.deleteIfExists(path.getParent());
                            if (b1)
                                log.info("删除文件夹成功: {}", path.getParent());
                        }
                    } catch (IOException e) {
                        log.error("删除文件失败: {}", file.getSourceName());
                        throw new RuntimeException(e);
                    }
                    // 删除记录
                    if (b.get()) {
                        fileService.deleteByIdPhysically(file.getId());
                    }
                });
            }
            log.info("无引用文件清理任务执行完毕");
        }, threadPoolExecutor);
    }
}

