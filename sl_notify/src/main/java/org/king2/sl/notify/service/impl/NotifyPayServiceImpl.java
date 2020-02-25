package org.king2.sl.notify.service.impl;

import com.nb304.lock.dfs_redis_lock.service.Lock;
import org.king2.sl.common.enums.OrderStateEnum;
import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.key.BalanceCommandKey;
import org.king2.sl.common.key.MoneyCommandKey;
import org.king2.sl.common.key.OrderCommandKey;
import org.king2.sl.common.pojo.SlBalance;
import org.king2.sl.common.pojo.SlMoney;
import org.king2.sl.common.pojo.SlRecharge;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.notify.mapper.LocalBalanceMapper;
import org.king2.sl.notify.mapper.LocalOrderMapper;
import org.king2.sl.notify.mapper.LocalSlMoneyMapper;
import org.king2.sl.notify.service.NotifyPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.JedisCluster;

import java.util.Date;
import java.util.List;

/**
 * 默认的异步通知Service
 */
@Service("defaultNotifyPayService")
public class NotifyPayServiceImpl implements NotifyPayService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyPayServiceImpl.class);

    /**
     * 本地的余额Mapper
     */
    @Autowired
    private LocalSlMoneyMapper localSlMoneyMapper;

    /**
     * redis默认的分布式锁
     */
    @Autowired
    private Lock lock;

    /**
     * 本地的订单Mapper
     */
    @Autowired
    private LocalOrderMapper localOrderMapper;

    /**
     * 本地余额Mapper
     */
    @Autowired
    private LocalBalanceMapper localBalanceMapper;

    /**
     * RedisCluster
     */
    @Autowired
    private JedisCluster jedisCluster;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(String orderNo) throws Exception {
        // 需要修改订单信息，并增加余额
        SlRecharge slRecharge = updateOrder(orderNo);
        // 增加用户的余额信息
        if (slRecharge != null) {
            addUserBalance(slRecharge);
        } else {
            LOGGER.error("订单号不存在:" + orderNo);
            throw new CheckValueException("订单号不存在:" + orderNo);
        }
    }


    /**
     * 跟新订单的状态
     *
     * @param orderNo
     * @return
     */
    public SlRecharge updateOrder(String orderNo) throws Exception {
        lock.lock(OrderCommandKey.ORDER_UPDATE_KEY, 10000);
        SlRecharge rechargeByOrder = null;
        try {
            // 根据订单号查询订单的信息 TODO 以后可以更换到查询远程服务器的信息
            rechargeByOrder = localOrderMapper.getRechargeByOrder(orderNo);
            if (rechargeByOrder != null) {
                // 判断一下订单的状态
                if ((OrderStateEnum.UNPAID.getId() + "").equals(rechargeByOrder.getSlRechargeState() + "")) {

                    // 说明用户之前没有支付成功，所以我们需要进行改状态
                    localOrderMapper.updateRechargeStateByOrderId(rechargeByOrder.getSlRechargeId(), OrderStateEnum.SUCCESS.getId());
                } else {
                    LOGGER.warn("订单已经支付:" + orderNo);
                    throw new CheckValueException("订单已经支付");
                }
            } else {
                LOGGER.error("支付宝发来成功消息，但是数据库并未查询到订单信息");
            }
        } catch (Exception e) {
            LOGGER.error("错误ERROR:" + e);
            throw e;
        } finally {
            // 系统文件路径
            String pathStr = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
            lock.unlock(pathStr + "/unlock.lua", OrderCommandKey.ORDER_UPDATE_KEY);
        }

        return rechargeByOrder;
    }


    /**
     * 增加用户的余额信息
     *
     * @param slRecharge
     */
    public void addUserBalance(SlRecharge slRecharge) throws Exception {

        lock.lock(BalanceCommandKey.UPDATE_BALANCE_KEY, 10000);
        try {
            // 首先需要查询出用户的余额  TODO 后面肯定是查询远程服务的
            SlBalance balanceByUserId = localBalanceMapper.getBalanceByUserId(slRecharge.getSlRechargeUser());
            balanceByUserId = refresh(balanceByUserId, slRecharge);
            // 判断用户之前是否有余额信息
            if (balanceByUserId != null) {
                if (balanceByUserId.getSlBalanceId() == null) {
                    // 说明是新增
                    localBalanceMapper.insertBalance(balanceByUserId);
                } else {
                    // 说明是更新
                    localBalanceMapper.updateBalance(balanceByUserId);
                }
            }

            // 更新Redis的数据
            updateRedisData(balanceByUserId);
        } finally {
            // 系统文件路径
            String pathStr = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
            lock.unlock(pathStr + "/unlock.lua", BalanceCommandKey.UPDATE_BALANCE_KEY);
        }

    }


    /**
     * 更新Redis中的数据信息
     *
     * @param balanceByUserId
     */
    private void updateRedisData(SlBalance balanceByUserId) throws Exception {

        lock.lock(MoneyCommandKey.MONEY_LOCK + balanceByUserId.getSlUserId(), 10000);
        try {
            // 修改用户的余额数量
            jedisCluster.set(MoneyCommandKey.MONEY_TOTAL_REDIS_KEY + "_" + balanceByUserId.getSlUserId(), balanceByUserId.getSlBalance() + "");
            // 修改用户最近的充值次数
            // 查询数据库的信息
            List<SlRecharge> userLastNineDataByUserId = localSlMoneyMapper.getUserLastNineDataByUserId(balanceByUserId.getSlUserId());
            jedisCluster.set(MoneyCommandKey.MONEY_LATE_NINE_REDIS_KEY + "_" + balanceByUserId.getSlUserId(),
                    CollectionUtils.isEmpty(userLastNineDataByUserId) ? "null" : JsonUtils.objectToJson(userLastNineDataByUserId));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 系统文件路径
            String pathStr = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
            lock.unlock(pathStr + "/unlock.lua", MoneyCommandKey.MONEY_LOCK + balanceByUserId.getSlUserId());
        }
    }

    /**
     * 刷新余额信息
     *
     * @param balanceByUserId
     * @return
     */
    private SlBalance refresh(SlBalance balanceByUserId, SlRecharge slRecharge) {

        if (balanceByUserId == null) {
            balanceByUserId = new SlBalance();
        }
        // 查询订单金额的赠送金额
        SlMoney moneyByMId = localSlMoneyMapper.getMoneyByMId(slRecharge.getSlMoneyId());
        balanceByUserId.setSlBalance((balanceByUserId.getSlBalanceId() == null ?
                0 : balanceByUserId.getSlBalance()) + (slRecharge.getSlMoneyName() + moneyByMId.getSlGiveMoney()));
        balanceByUserId.setSlUserId(slRecharge.getSlRechargeUser());
        balanceByUserId.setSlUpdateTime(new Date());
        return balanceByUserId;
    }
}
