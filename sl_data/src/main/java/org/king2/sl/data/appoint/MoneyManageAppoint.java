package org.king2.sl.data.appoint;

import org.king2.sl.common.key.MoneyCommandKey;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.pojo.SlMoney;
import org.king2.sl.common.pojo.SlRecharge;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.data.mapper.LocalSlMoneyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * 金额管理委托类
 */
@Component
public class MoneyManageAppoint {


    /**
     * RedisCluster实例
     */
    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 本地的余额Mapper
     */
    @Autowired
    private LocalSlMoneyMapper localSlMoneyMapper;

    /**
     * 线程共享的用户信息
     */
    public static final ThreadLocal<SlUserTable> SLUT = new ThreadLocal<>();

    /**
     * 金额首页的初始化
     *
     * @param request
     */
    public void init(HttpServletRequest request) throws IOException {

        // 首先需要初始化金额的类型，由于我们项目启动的时候已经加载了所以我们可以直接去Redis中查询
        String moneyTypes = jedisCluster.get(MoneyCommandKey.MONEY_TYPE_REDIS_CACHE_KEY);
        if (!StringUtils.isEmpty(moneyTypes)) {
            request.setAttribute("moneyType", JsonUtils.jsonToList(moneyTypes, SlMoney.class));
        }

    }

    /**
     * 首页的初始化
     *
     * @param request
     */
    public void indexInit(HttpServletRequest request) {
        // 查询用户的余额信息
        getUserBalanceByUserId(request);
        // 查询用户最近的九次充值记录
        getUserLastNineData(request);
    }

    /**
     * 查询用户最后九次的充值记录
     *
     * @param request
     */
    private void getUserLastNineData(HttpServletRequest request) {

        // 取出用户数据
        SlUserTable slUserTable = SLUT.get();
        // 判断Redis中是否存在数据
        String nineData = jedisCluster.get(MoneyCommandKey.MONEY_LATE_NINE_REDIS_KEY + "_" + slUserTable.getSlUserId());
        List<SlRecharge> recharges = null;
        if (StringUtils.isEmpty(nineData)) {
            // 查询数据库的信息
            recharges = localSlMoneyMapper.getUserLastNineDataByUserId(slUserTable.getSlUserId());
            try {
                jedisCluster.set(MoneyCommandKey.MONEY_LATE_NINE_REDIS_KEY + "_" + slUserTable.getSlUserId(),
                        CollectionUtils.isEmpty(recharges) ? "null" : JsonUtils.objectToJson(recharges));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (!"null".equals(nineData)) {
                try {
                    recharges = JsonUtils.jsonToList(nineData, SlRecharge.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        request.setAttribute("nineData", recharges);
    }

    /**
     * 根据用户的id查询用户的余额信息
     *
     * @param request
     */
    private void getUserBalanceByUserId(HttpServletRequest request) {

        // 取出用户的信息
        SlUserTable attribute = (SlUserTable) request.getAttribute(UserCommandKey.USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY);
        SLUT.set(attribute);

        // 判断Redis中是否存在数据
        String totalValue = jedisCluster.get(MoneyCommandKey.MONEY_TOTAL_REDIS_KEY + "_" + attribute.getSlUserId());
        Long userBalanceByUserId = null;
        if (StringUtils.isEmpty(totalValue)) {
            // 查询数据库的信息
            userBalanceByUserId = localSlMoneyMapper.getUserBalanceByUserId(attribute.getSlUserId());
            // 重新存入Redis中
            jedisCluster.set(MoneyCommandKey.MONEY_TOTAL_REDIS_KEY + "_" + attribute.getSlUserId(),
                    userBalanceByUserId == null ? "null" : userBalanceByUserId + "");
        } else {
            userBalanceByUserId = "null".equals(totalValue) ? 0 : Long.parseLong(totalValue);
        }

        // 存入Redis
        request.setAttribute("totalMoney", userBalanceByUserId == null ? 0 : userBalanceByUserId);
    }


}
