package org.king2.sl.book.server.impl;

import com.nb304.lock.dfs_redis_lock.service.Lock;
import com.nb304.lock.dfs_redis_lock.service.impl.DfsRedisLock;
import org.king2.sl.book.mapper.LocalSlBookMapper;
import org.king2.sl.book.server.GetBookInfoManageService;
import org.king2.sl.common.key.BookCommandKey;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.pojo.SlBook;
import org.king2.sl.common.pojo.SlBookDe;
import org.king2.sl.common.pojo.SlBookDto;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.SystemResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisCluster;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 获取书本的信息
 */
@RestController
@RequestMapping("/book")
public class GetBookInfoManageServiceImpl implements GetBookInfoManageService {

    /**
     * 注入RedisCluster
     */
    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 注入Redis锁
     */
    @Autowired
    private Lock lock;

    @Autowired
    private LocalSlBookMapper localSlBookMapper;

    @Override
    @RequestMapping("/get")
    public SystemResult getBookInfoById(@NotBlank(message = "不能查询空的书本") String bookId) throws Exception {

        // 开启分布式锁
        lock.lock(BookCommandKey.HANDLER_BOOK_REDIS_LOCK, 10000);
        try {

            // 先从Redis中查询是否存在该信息
            SystemResult redisResult = getBookInfoByIdGotoRedis(bookId);
            // 通过类型判断是否直接返回
            if (redisResult.getStatus() != 772) {
                return redisResult;
            }

            // Redis中没有信息我们需要从数据库查询出信息并同步到Redis中
            SystemResult systemResult = redisNoMsgWantFindSQLAndSyncRedis(bookId);
            return systemResult;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            // 系统文件路径
            String pathStr = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
            try {
                lock.unlock(pathStr + "/unlock.lua", BookCommandKey.HANDLER_BOOK_REDIS_LOCK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Redis中没有数据，我们需要从SQL中查询数据并存入Redis
     *
     * @param bookId
     */
    private SystemResult redisNoMsgWantFindSQLAndSyncRedis(String bookId) throws Exception {

        // 查看书本是否存在
        SlBook bookById = localSlBookMapper.getBookById(Integer.parseInt(bookId));
        if (bookById == null) {
            // 往Redis中存一份信息，以防缓存穿透
            jedisCluster.set(BookCommandKey.BOOK_INFO_REDIS_KEY + bookId, "HC_CT");
        }

        SlBookDto slBookDto = new SlBookDto();
        slBookDto.setSlBook(bookById);
        // 查询这个书本的所有内容
        List<SlBookDe> slBookContentByBId = localSlBookMapper.getSlBookContentByBId(Integer.parseInt(bookId));
        slBookDto.setSlBookDes(slBookContentByBId);

        // 查询完成后存入Redis
        String result = JsonUtils.objectToJson(slBookDto);
        jedisCluster.set(BookCommandKey.BOOK_INFO_REDIS_KEY, result);
        return new SystemResult(result);
    }

    /**
     * 从Redis中查询是否存在该信息
     *
     * @param bookId 书本的id
     * @return 返回Book的信息
     */
    private SystemResult getBookInfoByIdGotoRedis(String bookId) {
        // 从Redis中查询信息
        String bookInfo = jedisCluster.get(BookCommandKey.BOOK_INFO_REDIS_KEY + bookId);
        if ("HC_CT".equals(bookInfo)) {
            return new SystemResult(771, "暂无数据");
        } else if (StringUtils.isEmpty(bookInfo)) {
            return new SystemResult(772, "暂无BOOK");
        }

        return new SystemResult(bookInfo);
    }

}
