package org.king2.sl.sso.task;

import cn.hutool.extra.mail.MailUtil;
import lombok.Data;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.king2.sl.common.enums.OrderTellEnum;
import org.king2.sl.common.enums.UserMessageEnum;
import org.king2.sl.common.pojo.SlMessage;
import org.king2.sl.common.pojo.SlRecharge;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.sso.mapper.LocalLsUserTableMapper;
import org.king2.sl.sso.mapper.LocalRechargeMapper;
import org.king2.sl.sso.mapper.LocalSlUserMessageMapper;
import org.king2.sl.sso.pool.TaskPool;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.concurrent.*;

/**
 * 每日凌晨0点清空数据
 */
@EnableScheduling
@Component
public class TaskClearData {

    /**
     * 本地的用户Mapper
     */
    @Autowired
    private LocalLsUserTableMapper localLsUserTableMapper;

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskClearData.class);

    /**
     * 定义线程池
     */
    private static final ExecutorService POOL = Executors.newFixedThreadPool(10);

    /**
     * 本地的订单Mapper
     */
    @Autowired
    private LocalRechargeMapper localRechargeMapper;

    /**
     * 注入事务
     */
    @Autowired
    private PlatformTransactionManager platformTransactionManager;
    @Autowired
    private TransactionDefinition transactionDefinition;

    /**
     * 本地的MessageMapper
     */
    @Autowired
    private LocalSlUserMessageMapper localSlUserMessageMapper;

    @Scheduled(cron = "0 0 1/5 * * ?")
    public void main() {
        POOL.execute(() -> {
            // 告诉用户账户没有被正确激活
            tellUserAccountNotActive();
        });

        POOL.execute(() -> {
            // 告诉用户他没有支付的订单
            tellUserOrderNotPay();
        });

    }

    /**
     * 告诉那些用户，订单没有正常支付
     */
    public void tellUserOrderNotPay() {
        /**
         * 保证任务之间顺序执行
         */
        System.out.println("执行了方法");
        CountDownLatch latch = new CountDownLatch(1);
        // 创建一个CountDownLatch
        CountDownLatch countDownLatch = new CountDownLatch(1);
        // 查询出所有没有支付成功的订单
        List<SlRecharge> allNotPaySuccess = localRechargeMapper.getAllNotPaySuccess();
        if (!CollectionUtils.isEmpty(allNotPaySuccess)) {
            /**
             * 说明有未支付成功的订单，那么这里就分为以下几种情况
             * 1、支付失败，但是没有通知，所以我们需要定义要通知的用户集合，并且需要定义修改订单通知的集合
             * 2、支付失败，但是已经通知了，所以我们没有必要在进行通知了，需要把无效的数据删除，后面可以改成改状态。
             */
            // 定义要通知的用户集合
            List<Integer> yesTellUserIds = new LinkedList<>();
            // 定义需要修改的订单id
            List<Integer> yesUpdateOrderIds = new LinkedList<>();
            // 需要删除的订单id
            List<Integer> yesDelOrderIds = new LinkedList<>();
            // 定义方法是否需要进行RollBack
            InvokeCAndR invokeRollback = new InvokeCAndR();

            for (SlRecharge notPaySuccess : allNotPaySuccess) {
                if (notPaySuccess.getSlTell().equals(OrderTellEnum.SUCCESS_TELL + "")) {
                    // 说明已经发送过通知了
                    yesDelOrderIds.add(notPaySuccess.getSlRechargeId());
                } else if (notPaySuccess.getSlTell().equals(OrderTellEnum.NO_TELL + "")) {
                    // 说明没有发送通知，那么就需要进行用户的通知
                    if (!yesTellUserIds.contains(notPaySuccess.getSlRechargeUser())) {
                        yesTellUserIds.add(notPaySuccess.getSlRechargeUser());
                    }
                    yesUpdateOrderIds.add(notPaySuccess.getSlRechargeId());
                }
            }

            // 定义提交任务的集合
            List<Future<Object>> futures = new ArrayList<>();
            try {
                futures.add(POOL.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        // 开启事务
                        TransactionStatus transaction = null;
                        try {
                            transaction = platformTransactionManager.getTransaction(transactionDefinition);
                            // 通知用户订单未支付
                            if (!CollectionUtils.isEmpty(yesTellUserIds)) {
                                // 可以发送至用户的邮箱，以及在站内通知用户
                                for (Integer yesTellUserId : yesTellUserIds) {
                                    SlUserTable user = localLsUserTableMapper.getUserById(yesTellUserId);
                                    sendEmail(user);
                                }

                                // 发出站内信，通知用户订单未支付
                                SlMessage slMessage = new SlMessage();
                                slMessage.setCreateTime(new Date());
                                slMessage.setSlMessageContent("您好，我是来自星光漫画的小天使，您有一笔未支付的订单，请前往'我的星币 -> 未支付的订单'中查看");
                                slMessage.setSlReadState(UserMessageEnum.NO_READ + "");
                                slMessage.setSlSendUserId(0);

                                // 发送站内信息
                                localSlUserMessageMapper.sendMsg(slMessage, yesTellUserIds);
                            }
                            //  修改订单的通知状态
                            if (!CollectionUtils.isEmpty(yesUpdateOrderIds)) {
                                localRechargeMapper.updateRechargeState(OrderTellEnum.SUCCESS_TELL + "", yesUpdateOrderIds);
                            }
                            /**
                             * 锁住线程，等待后面SQL的执行完毕
                             * 如果出现异常则rollback
                             */
                            latch.countDown();
                            countDownLatch.await();

                            // 判断是否需要进行rollback
                            if (invokeRollback.isCAndR()) {
                                // 进行Rollback
                                platformTransactionManager.rollback(transaction);
                            } else {
                                // 进行Commit
                                platformTransactionManager.commit(transaction);
                            }
                        } catch (Exception e) {
                            platformTransactionManager.rollback(transaction);
                            throw e;
                        } finally {
                            latch.countDown();
                        }
                        return "ok";
                    }

                    /**
                     * 发送邮件
                     * @param slUserTable
                     */
                    private void sendEmail(SlUserTable slUserTable) {
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("您好，我是来自星光漫画的小天使，您的账号：" + slUserTable.getSlUserName() + "有一笔订单没有完成支付哦。");
                        try {
                            MailUtil.send(slUserTable.getSlUserEmail(), "支付失败", stringBuffer.toString(), true);
                        } catch (Exception e) {
                            LOGGER.error("发送邮件失败:" + e);
                        }
                    }

                }));

                // 删除那些已经通知过的订单信息
                futures.add(POOL.submit(new Callable<Object>() {
                    @Override
                    public Object call() throws InterruptedException {
                        latch.await();
                        try {
                            if (!CollectionUtils.isEmpty(yesDelOrderIds)) {
                                localRechargeMapper.deleteSuccessTellOrder(yesDelOrderIds);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            invokeRollback.setCAndR(true);

                        } finally {
                            countDownLatch.countDown();
                        }
                        return "ok";
                    }
                }));

                // 等待线程执行完毕
                for (Future<Object> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 告诉用户账户没有被正确激活
     */
    private void tellUserAccountNotActive() {

        // 首先需要查询出所有没有被激活的账户
        List<SlUserTable> allNotActive = localLsUserTableMapper.getAllNotActive();
        // 发出邮件
        for (SlUserTable slUserTable : allNotActive) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("您好，我是来自星光漫画的小天使，您的账号：" + slUserTable.getSlUserName() + "由于长时间没有激活已被系统删除，如需激活请重新注册哦；");
            try {
                MailUtil.send(slUserTable.getSlUserEmail(), "账号激活失效", stringBuffer.toString(), true);
            } catch (Exception e) {
                LOGGER.error("发送邮件失败:" + e);
            }
        }
        // 删除没有激活的账户
        localLsUserTableMapper.deleteNotActive();
    }

}

@Data
class InvokeCAndR {
    private boolean cAndR = false;

}
