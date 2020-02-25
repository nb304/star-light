package org.king2.sl.data.init;

import org.king2.sl.common.key.MoneyCommandKey;
import org.king2.sl.common.pojo.SlMoney;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.data.mapper.LocalSlMoneyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 初始化金额类型
 * 由于怕用户通过前端改造金额的数值，所以我们需要通过数据库定义金额的类型
 */
@Component
public class InitMoneyType {

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private LocalSlMoneyMapper localSlMoneyMapper;


    @PostConstruct
    public void initMoneyType() throws Exception {
        // 查询Redis中是否存在数据
        String moneyTypeCache = jedisCluster.get(MoneyCommandKey.MONEY_TYPE_REDIS_CACHE_KEY);
        if (StringUtils.isEmpty(moneyTypeCache)) {
            // 说明需要去数据库查询数据并存入Redis中
            List<SlMoney> allMoney = localSlMoneyMapper.getAllMoney();
            if (!CollectionUtils.isEmpty(allMoney)) {
                jedisCluster.set(MoneyCommandKey.MONEY_TYPE_REDIS_CACHE_KEY, JsonUtils.objectToJson(allMoney));
            }
        }
    }
}
