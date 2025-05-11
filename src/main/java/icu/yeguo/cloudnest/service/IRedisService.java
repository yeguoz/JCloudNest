package icu.yeguo.cloudnest.service;

import java.util.List;
import java.util.Map;

public interface IRedisService {
    List<String> getRedisKeysByPattern(String pattern);

    Map<String, String> getMapByPattern(String pattern);
}
