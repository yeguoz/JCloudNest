package icu.yeguo.cloudnest.task;

import icu.yeguo.cloudnest.model.entity.Share;
import icu.yeguo.cloudnest.service.IShareService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class ShareCleanupTask {

    @Resource
    private IShareService shareService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void markExpiredSharesAsDeleted() {
        LocalDateTime now = LocalDateTime.now();
        boolean success = shareService.lambdaUpdate()
                .le(Share::getExpireTime, now)
                .isNull(Share::getDeletedAt)
                .set(Share::getDeletedAt, LocalDateTime.now())
                .update();
        log.info("逻辑删除过期分享链接: {}", success ? "成功" : "无数据");
    }
}