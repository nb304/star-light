package org.king2.sl.data.service.impl;

import org.king2.sl.common.pojo.SlMoney;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.mapper.LocalSlMoneyMapper;
import org.king2.sl.data.service.MoneyManageService;
import org.king2.sl.data.utils.MoneyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCluster;

/**
 * 默认的余额管理Service
 */
@Service("defaultMoneyManageService")
public class MoneyManageServiceImpl implements MoneyManageService {

    @Autowired
    private JedisCluster jedisCluster;
    @Autowired
    private LocalSlMoneyMapper localSlMoneyMapper;
    @Autowired
    private MoneyUtil moneyUtil;

    /**
     * 获取金额的信息
     *
     * @param typeId
     * @return
     * @throws Exception
     */
    @Override
    public SystemResult getOrderMoneyInfo(Integer typeId) throws Exception {

        // 获取金额信息
        SlMoney money = moneyUtil.getMoneyById(typeId);
        return new SystemResult(money);
    }
}
