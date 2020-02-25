package org.king2.sl.data.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 支付接口
 */
public interface PayService {

    /**
     * 支付接口
     *
     * @param moneyId
     * @return
     */
    String pay(String moneyId, HttpServletRequest request) throws Exception;
}
