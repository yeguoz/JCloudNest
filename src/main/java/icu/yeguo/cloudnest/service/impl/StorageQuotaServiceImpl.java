package icu.yeguo.cloudnest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.yeguo.cloudnest.constant.UploadConstant;
import icu.yeguo.cloudnest.exception.BusinessException;
import icu.yeguo.cloudnest.model.dto.UploadRequest;
import icu.yeguo.cloudnest.model.entity.Group;
import icu.yeguo.cloudnest.model.entity.User;
import icu.yeguo.cloudnest.model.vo.UserVO;
import icu.yeguo.cloudnest.service.IGroupService;
import icu.yeguo.cloudnest.service.IRedisService;
import icu.yeguo.cloudnest.service.IStorageQuotaService;
import icu.yeguo.cloudnest.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static icu.yeguo.cloudnest.constant.UserConstant.USER_VO;

@Service
public class StorageQuotaServiceImpl implements IStorageQuotaService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IRedisService redisService;
    @Resource
    private IUserService userService;
    @Resource
    private IGroupService groupService;
    @Resource
    private HttpSession session;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public void checkAvailableSpace(UserVO userVO, Long fileSize, UploadRequest uploadRequest) {
        List<String> uploadIds = uploadRequest.getUploadIds();
        AtomicLong reservedSize = new AtomicLong(fileSize);
        String pattern = String.format("upload:session:info:user:%s:*", userVO.getId());
        List<String> keyList = redisService.getRedisKeysByPattern(pattern);

        User user = userService.getById(userVO.getId());
        Group group = groupService.getOne(new LambdaQueryWrapper<Group>().eq(Group::getId, userVO.getGroupId()));
        long maxStorage = group.getMaxStorage();
        long usedStorage = user.getUsedStorage();

        if (keyList.isEmpty()) {
            if (reservedSize.get() + usedStorage > maxStorage) {
                throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "存储空间不足");
            }
        }

        for (String key : keyList) {
            String fileSizeStr = (String) stringRedisTemplate.opsForHash().get(key, UploadConstant.FILE_SIZE);
            String uploadIdStr = (String) stringRedisTemplate.opsForHash().get(key, UploadConstant.UPLOAD_ID);
            if (fileSizeStr == null) continue;

            // 计算在上传队列中的文件大小
            if (uploadIdStr != null && uploadIds.contains(uploadIdStr)) {
                long size = Long.parseLong(fileSizeStr);
                reservedSize.addAndGet(size);
            }

            if (reservedSize.get() + usedStorage > maxStorage) {
                throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "存储空间不足");
            }
        }
    }

    public void updateUserStorage(UserVO userVO, Long size) {
        String lockKey = "lock:updateUsedStorage:" + userVO.getId();
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        try {
            // 尝试获取锁，最多等待 30 秒，锁的持有时间为 60 秒
            locked = lock.tryLock(30, 60, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(HttpServletResponse.SC_BAD_REQUEST, "无法获取分布式锁，请稍后重试");
            }
            // 更新用户存储空间大小
            boolean b = userService.lambdaUpdate()
                    .eq(User::getId, userVO.getId())
                    .setSql("used_storage = used_storage + {0}", size)
                    .update();

            if (!b) {
                throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "更新失败");
            }

            User user = userService.getById(userVO.getId());
            userVO.setUsedStorage(user.getUsedStorage());
            session.setAttribute(USER_VO, userVO);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}