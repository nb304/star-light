package org.king2.sl.sso.appoint;

import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.key.UserTimeoutCommand;
import org.king2.sl.common.utils.CookieUtils;
import org.king2.sl.common.utils.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 登录首先的委托类
 */
@Component
public class LoginIndexAppoint {

    @Autowired
    private JedisCluster jedisCluster;


    /**
     * 初始化登录类
     */
    public void init(HttpServletRequest request, HttpServletResponse response) {

        // 需要判断是否是已经登录成功
        if (isLogin(request, response)) {
            // 登录成功后的逻辑
            request.setAttribute("isLogin", true);
        } else {
            /**
             * 用户没有登录，那么需要初始化一个验证码，以防别人轰炸。
             * 再写入验证码之前我们需要判断Cookie当中是否存在值，如果存在的话则需要重新刷新Redis中的值
             */
            String cookieValue = CookieUtils.getCookieValue(request, UserCommandKey.USER_CODE_COOKIE_KEY);
            if (StringUtils.isEmpty(cookieValue)) {
                cookieValue = UserCommandKey.USER_CODE_COOKIE_KEY + MD5Utils.md5(UUID.randomUUID().toString() + System.currentTimeMillis());
            }

            // 写入用户的Cookie当中
            CookieUtils.setCookie(request, response, UserCommandKey.USER_CODE_COOKIE_KEY, cookieValue, UserTimeoutCommand.USER_CODE_COOKIE_TIME_OUT);
            /**
             * 写入Cookie成功，重新写入Redis当中，后面需要重新取出作比较
             * 验证码存在Redis中的Key就是存在Cookie中的Value
             */
            jedisCluster.setex(cookieValue, UserTimeoutCommand.USER_CODE_REDIS_TIME_OUT, "true");
        }
    }

    /**
     * 判断是否登录成功
     *
     * @return
     */
    public boolean isLogin(HttpServletRequest request, HttpServletResponse response) {

        // 判断用户是否登录成功需要查看Cookie和Redis当中是否存在值
        String cookieValue = CookieUtils.getCookieValue(request, UserCommandKey.USER_TOKEN_COOKIE_KEY);
        if (StringUtils.isEmpty(cookieValue)) {
            return false;
        } else if (StringUtils.isEmpty(jedisCluster.get(cookieValue))) {
            // 删除Cookie信息
            CookieUtils.deleteCookie(request, response, UserCommandKey.USER_TOKEN_COOKIE_KEY);
            return false;
        } else {
            return true;
        }
    }
}
