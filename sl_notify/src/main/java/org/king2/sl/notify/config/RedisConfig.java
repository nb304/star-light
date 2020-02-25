package org.king2.sl.notify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RedisConfig
 */
@Component
@ConfigurationProperties(prefix = "spring.redis.cache")
@Data
public class RedisConfig {

    private int expireSeconds;
    private String clusterNodes;
    private int commandTimeout;
}
