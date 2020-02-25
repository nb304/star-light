package org.king2.sl.sso.service;

import org.king2.sl.common.utils.SystemResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户登录管理的Service
 */
public interface UserLoginManageService {

    /**
     * 用户登录接口
     *
     * @param request  Request
     * @param response Response
     * @param userName 用户名
     * @param passWord 密码
     * @return
     */
    SystemResult login(HttpServletRequest request, HttpServletResponse response,
                       String userName, String passWord) throws Exception;
}
