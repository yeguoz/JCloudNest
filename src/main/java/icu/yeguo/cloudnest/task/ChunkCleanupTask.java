package icu.yeguo.cloudnest.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.yeguo.cloudnest.model.entity.Policy;
import icu.yeguo.cloudnest.service.IPolicyService;
import icu.yeguo.cloudnest.service.IRedisService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
public class ChunkCleanupTask {

    @Resource
    private IRedisService redisService;
    @Resource
    private IPolicyService policyService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void removeUncompletedChunks() {
        Map<String, String> map = redisService.getMapByPattern("upload:session:info:*");
        Policy policy = policyService.getOne(new LambdaQueryWrapper<Policy>().eq(Policy::getName, "Default"));
        String base = policy.getChunkDirNameRule().substring(0, policy.getChunkDirNameRule().indexOf("{"));

        log.debug("清理未完成的分块，根目录为:{}", base);
        Path basePath = Paths.get(base);

        try (Stream<Path> paths = Files.list(basePath)) {
            paths.forEach(path -> {
                String chunkDir = path.getFileName().toString();
                String fingerprint = map.get(chunkDir);

                if (fingerprint == null) {
                    Path p = basePath.resolve(chunkDir);

                    try (Stream<Path> walk = Files.walk(p)) {
                        walk.sorted(Comparator.reverseOrder()).forEach(sonOfP -> {
                            try {
                                Files.deleteIfExists(sonOfP);
                                log.debug("已删除文件或文件夹:{}", sonOfP);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}