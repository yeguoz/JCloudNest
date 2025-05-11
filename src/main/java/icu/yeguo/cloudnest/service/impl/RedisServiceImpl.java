package icu.yeguo.cloudnest.service.impl;

import icu.yeguo.cloudnest.service.IRedisService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RedisServiceImpl implements IRedisService {

    private final static String UPLOAD_ID = "uploadId";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<String> getRedisKeysByPattern(String pattern) {
        List<String> keys = new ArrayList<>();

        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)
                    .build();

            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new RuntimeException("Scan keys failed", e);
            }
            return null;
        });
        return keys;
    }

    @Override
    public Map<String, String> getMapByPattern(String pattern) {
        HashMap<String, String> hashMap = new HashMap<>();

        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)
                    .build();

            try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                    byte[] keyBytes = cursor.next();
                    String redisKey = new String(keyBytes, StandardCharsets.UTF_8);

                    byte[] uploadIdBytes = connection.hGet(keyBytes, UPLOAD_ID.getBytes(StandardCharsets.UTF_8));
                    String uploadId = new String(uploadIdBytes, StandardCharsets.UTF_8);
                    hashMap.put(uploadId, redisKey);
                }
            } catch (Exception e) {
                throw new RuntimeException("Scan keys failed", e);
            }
            return null;
        });
        return hashMap;
    }
}
