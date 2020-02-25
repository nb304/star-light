package org.king2.sl.data.service;

import org.king2.sl.common.utils.SystemResult;

import java.io.IOException;

/**
 * 余额管理Service
 */
public interface MoneyManageService {


    /**
     * 根据类型获取金额的类型
     *
     * @param typeId
     * @return
     */
    SystemResult getOrderMoneyInfo(Integer typeId) throws Exception;
}
