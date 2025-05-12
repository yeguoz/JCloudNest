package icu.yeguo.cloudnest.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        String address = String.format("redis://%s:6379", redisHost);

        Config config = new Config();
        if (redisPassword.isEmpty()){
            config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(0);
            return Redisson.create(config);
        }
        config.useSingleServer()
                .setAddress(address)
                .setPassword(redisPassword)
                .setDatabase(0);

        return Redisson.create(config);
    }
}