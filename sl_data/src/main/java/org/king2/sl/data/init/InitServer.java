package org.king2.sl.data.init;

import org.king2.sl.common.pojo.SlServer;
import org.king2.sl.data.mapper.LocalServerMapper;
import org.king2.sl.data.mapper.LocalSlBookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.JedisCluster;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 初始化服务器的信息
 * 将服务器的信息存入Redis当中
 */
@Component
@RequestMapping("/")
public class InitServer {

    /**
     * 注入RedisCluster实现类
     */
    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 注入本地的BookMapper
     */
    @Autowired
    private LocalServerMapper localServerMapper;

    /**
     * 将服务器的信息存入Redis当中
     */
    @PostConstruct
    public void postConstruct() {
        // 初始化服务器信息
        init();
    }


    /**
     * 重置服务器的信息
     *
     * @return
     */
    @RequestMapping("/clear/server")
    @ResponseBody
    public String clearServer() {

        init();
        return "重置服务器信息成功！";
    }


    public void init() {
        List<SlServer> all = localServerMapper.getAll();
        for (SlServer slServer : all) {
            jedisCluster.set(slServer.getSlServerName(), slServer.getSlServerIp());
        }
    }

}
