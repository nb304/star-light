package org.king2.sl.data.appoint;

import org.king2.sl.common.key.BookCommandKey;
import org.king2.sl.common.pojo.SlBookType;
import org.king2.sl.common.pojo.SlDataType;
import org.king2.sl.common.pojo.SlServer;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.data.mapper.LocalServerMapper;
import org.king2.sl.data.mapper.LocalSlBookMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import redis.clients.jedis.JedisCluster;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 添加书本的委派类
 */
@Component
public class AddBookManageAppoint {

    /**
     * 注入RedisCluster实现类
     */
    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 注入本地的BookMapper
     */
    @Autowired
    private LocalSlBookMapper localSlBookMapper;

    /**
     * 注入本地的服务端Mapper
     */
    @Autowired
    private LocalServerMapper localServerMapper;

    /**
     * Book初始化的锁
     */
    private static final Object BOOK_INIT_LOCK = new Object();

    /**
     * 书本首先的初始化类
     *
     * @param request
     */
    public void bookInit(HttpServletRequest request) throws Exception {
        synchronized (BOOK_INIT_LOCK) {
            // 查询书本类型是否已经被初始化到Redis当中了
            String bookType = jedisCluster.get(BookCommandKey.BOOK_REDIS_TYPE_KEY);
            if (StringUtils.isEmpty(bookType)) {
                // 需要初始化书本的类型
                List<SlBookType> bookTypes = localSlBookMapper.getBookTypes();
                // 存入Redis和Request
                request.setAttribute("bookType", bookTypes);
                jedisCluster.set(BookCommandKey.BOOK_REDIS_TYPE_KEY, JsonUtils.objectToJson(bookTypes));
            } else {
                request.setAttribute("bookType", JsonUtils.jsonToList(bookType, SlBookType.class));
            }

            getBookDataType(request);

        }
    }

    /**
     * 获取书本数据的类型
     *
     * @param request
     */
    public void getBookDataType(HttpServletRequest request) throws Exception {
        // 查看书本数据的类型
        String bookDataType = jedisCluster.get(BookCommandKey.BOOK_DATA_REDIS_TYPE_KEY);
        if (StringUtils.isEmpty(bookDataType)) {
            // 需要初始化书本数据的类型
            List<SlDataType> dataTypes = localSlBookMapper.getDataTypes();
            // 存入Redis和Request
            request.setAttribute("bookDataType", dataTypes);
            jedisCluster.set(BookCommandKey.BOOK_DATA_REDIS_TYPE_KEY, JsonUtils.objectToJson(dataTypes));
        } else {
            request.setAttribute("bookDataType", JsonUtils.jsonToList(bookDataType, SlDataType.class));
        }
    }


}
