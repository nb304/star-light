package org.king2.sl.notify.service;

/**
 * 异步通知Service
 */
public interface NotifyPayService {


    /**
     * 支付的异步回调
     *
     * @param orderNo 订单号
     */
    void pay(String orderNo) throws Exception;
}
