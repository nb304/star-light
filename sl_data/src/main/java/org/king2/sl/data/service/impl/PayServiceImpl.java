package org.king2.sl.data.service.impl;

import com.nb304.lock.dfs_redis_lock.service.Lock;
import org.king2.sl.common.enums.OrderStateEnum;
import org.king2.sl.common.key.MoneyCommandKey;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.pojo.SlMoney;
import org.king2.sl.common.pojo.SlRecharge;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.utils.MD5Utils;
import org.king2.sl.data.alipay.ALiPayBean;
import org.king2.sl.data.config.AliPayConfig;
import org.king2.sl.data.mapper.LocalPayMapper;
import org.king2.sl.data.service.PayService;
import org.king2.sl.data.utils.AliPayUtil;
import org.king2.sl.data.utils.MoneyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.UUID;


/**
 * 默认的支付接口
 */
@Service("defaultPayService")
public class PayServiceImpl implements PayService {

    /**
     * 金额的工具类
     */
    @Autowired
    private MoneyUtil moneyUtil;
    /**
     * Redis锁
     */
    @Autowired
    private Lock lock;
    /**
     * 本地支付的Mapper
     */
    @Autowired
    private LocalPayMapper localPayMapper;

    @Override
    public String pay(String moneyId, HttpServletRequest request) throws Exception {

        // 获取金额的类型列表
        SlMoney money = moneyUtil.getMoneyById(Integer.parseInt(moneyId));
        // 获取用户的信息
        SlUserTable attribute = (SlUserTable) request.getAttribute(UserCommandKey.USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY);
        // 初始化订单信息
        SlRecharge slRecharge = initRecharge(money, attribute);

        // 发起支付宝请求前的操作
        requestALiPayBeforeHandler(slRecharge);

        // 请求支付宝进行支付
        String connect = AliPayUtil.connect(new ALiPayBean()
                .setOut_trade_no(slRecharge.getSlRechargeOrder())
                .setBody("用户购买星币")
                .setSubject("星光漫画")
                .setTotal_amount(money.getSlRmb() + ""));
        return connect;
    }

    /**
     * 请求阿里支付的前的操作
     *
     * @param slRecharge
     */
    private void requestALiPayBeforeHandler(SlRecharge slRecharge) {

        // 需要将充值记录存入SQL
        localPayMapper.inset(slRecharge);
    }

    /**
     * 初始化充值记录
     *
     * @param money
     * @return
     */
    public SlRecharge initRecharge(SlMoney money, SlUserTable userTable) {
        // 加锁
        lock.lock(MoneyCommandKey.MONEY_ORDER_ID_LOCK, 10000);

        SlRecharge slRecharge;
        try {
            slRecharge = new SlRecharge();
            slRecharge.setSlRechargeOrder(MD5Utils.md5(UUID.randomUUID().toString()) + System.currentTimeMillis());
            slRecharge.setSlMoneyId(money.getSlMoneyId());
            slRecharge.setSlRechargeUser(userTable.getSlUserId());
            slRecharge.setSlRechargeState(OrderStateEnum.UNPAID.getId());
            slRecharge.setSlRechargeCreateTime(new Date());
            slRecharge.setSlMoneyName(money.getSlMoneyName());
        } finally {
            // 系统文件路径
            String pathStr = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
            try {
                lock.unlock(pathStr + "/unlock.lua", MoneyCommandKey.MONEY_ORDER_ID_LOCK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return slRecharge;
    }


}
