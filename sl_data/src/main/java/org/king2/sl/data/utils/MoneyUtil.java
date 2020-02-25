package org.king2.sl.data.utils;

import com.nb304.lock.dfs_redis_lock.service.Lock;
import org.king2.sl.common.key.BookCommandKey;
import org.king2.sl.common.key.MoneyCommandKey;
import org.king2.sl.common.pojo.SlMoney;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.mapper.LocalSlMoneyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.List;

/**
 * 金额的工具类
 */
@Component
public class MoneyUtil {

    @Autowired
    private JedisCluster jedisCluster;
    @Autowired
    private LocalSlMoneyMapper localSlMoneyMapper;
    @Autowired
    private Lock lock;

    /**
     * 获取Money的类型
     *
     * @return
     * @throws IOException
     */
    public List<SlMoney> getType() throws Exception {

        // 加锁
        lock.lock(MoneyCommandKey.MONEY_TYPE_LOCK, 10000);
        try {
            // 查询Redis中的信息
            String types = jedisCluster.get(MoneyCommandKey.MONEY_TYPE_REDIS_CACHE_KEY);
            List<SlMoney> slMonies = null;
            if (!StringUtils.isEmpty(types)) {
                slMonies = JsonUtils.jsonToList(types, SlMoney.class);
            } else {
                // 说明需要去数据库查询数据并存入Redis中
                slMonies = localSlMoneyMapper.getAllMoney();
                if (!CollectionUtils.isEmpty(slMonies)) {
                    jedisCluster.set(MoneyCommandKey.MONEY_TYPE_REDIS_CACHE_KEY, JsonUtils.objectToJson(slMonies));
                }
            }

            return slMonies;
        } finally {
            // 系统文件路径
            String pathStr = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
            try {
                lock.unlock(pathStr + "/unlock.lua", MoneyCommandKey.MONEY_TYPE_LOCK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取金额
     *
     * @param typeId
     * @return
     */
    public SlMoney getMoneyById(Integer typeId) throws Exception {
        List<SlMoney> type = getType();
        SlMoney slMoney = null;
        for (SlMoney money : type) {
            if ((typeId + "").equals(money.getSlMoneyId() + "")) {
                slMoney = money;
                break;
            }
        }

        return slMoney;
    }

}
