package org.king2.sl.data.interceptors;

import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 判断用户是否登录的拦截器
 */
public class UserIsLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        initServer(request);

        // 从COOKIE当中获取用户的信息
        String cookieValue = CookieUtils.getCookieValue(request, UserCommandKey.USER_TOKEN_COOKIE_KEY);
        if (StringUtils.isEmpty(cookieValue)) {
            // 等于空说明没有登录
            request.setAttribute(UserCommandKey.USER_IS_LOGIN_REQUEST_KEY, false);
        } else {
            String str = jedisCluster.get(cookieValue);
            if (StringUtils.isEmpty(str)) {
                // 等于空说明没有登录
                request.setAttribute(UserCommandKey.USER_IS_LOGIN_REQUEST_KEY, false);
                // 清空Cookie信息
                CookieUtils.deleteCookie(request, response, UserCommandKey.USER_TOKEN_COOKIE_KEY);
            } else {
                // 将用户信息和用户CookieValue存入Request
                request.setAttribute(UserCommandKey.USER_LOGIN_SUCCESS_INFO_REQ_KEY, str);
                request.setAttribute(UserCommandKey.USER_CODE_COOKIE_VALUE, cookieValue);
                request.setAttribute(UserCommandKey.USER_IS_LOGIN_REQUEST_KEY, true);
            }

        }
        return true;
    }

    private void initServer(HttpServletRequest request) {
        // 获取服务器的信息
        String sso = jedisCluster.get("SSO");
        String data = jedisCluster.get("DATA");
        String book = jedisCluster.get("BOOK");
        String pay = jedisCluster.get("PAY");
        request.setAttribute("SSO", sso);
        request.setAttribute("DATA", data);
        request.setAttribute("BOOK", book);
        request.setAttribute("PAY", pay);
    }
}
