package org.king2.sl.notify.config;

import com.nb304.lock.dfs_redis_lock.service.Lock;
import com.nb304.lock.dfs_redis_lock.service.impl.DfsRedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

/**
 * Jedis集群的配置类
 */
@Configuration
@SuppressWarnings("all")
public class JedisClusterConfig {

    @Autowired
    private RedisConfig redisConfig;

    @Bean
    public JedisCluster getJedisCluster() {

        /* 配置JedisPool */
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(200);
        poolConfig.setMaxIdle(30);
        poolConfig.setMinIdle(30);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(3000);
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(60000);
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);
        poolConfig.setNumTestsPerEvictionRun(-1);
        String[] nodes = redisConfig.getClusterNodes().split(",");
        Set<HostAndPort> set = new HashSet<>();
        for (String node : nodes) {
            String[] ipAndPort = node.split(":");
            set.add(new HostAndPort(ipAndPort[0].trim(), Integer.parseInt(ipAndPort[1])));
        }

        JedisCluster jedisCluster = new JedisCluster(set, redisConfig.getCommandTimeout(), redisConfig.getExpireSeconds(), poolConfig);
        return jedisCluster;
    }

    @Bean
    public Lock redisLock() throws Exception {
        return new DfsRedisLock(getJedisCluster());
    }
}
