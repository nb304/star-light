package org.king2.sl.sso.service;

import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.utils.SystemResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户注册管理Service
 */
public interface UserRegisterManageService {

    /**
     * 用户注册
     *
     * @param content 加密中的信息
     * @return 返回注册的结果
     */
    SystemResult register(String content, HttpServletRequest request) throws Exception;
}
