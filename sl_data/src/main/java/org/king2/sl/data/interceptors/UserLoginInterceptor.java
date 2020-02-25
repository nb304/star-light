package org.king2.sl.data.interceptors;

import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.exceptions.LogException;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.utils.CookieUtils;
import org.king2.sl.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户登录成功的一个拦截器
 */
public class UserLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean login = (boolean) request.getAttribute(UserCommandKey.USER_IS_LOGIN_REQUEST_KEY);
        if (login) {
            /**
             * 如果是登录成功的操作，我们首先需要做一下的事情
             * 0、取出用户信息
             * 1、判断用户是否同意本站规则。
             */

            // 取出用户信息
            SlUserTable userTable = getUserInfoByRequest(request, response);

            // 判断用户是否同意本站规则。
            senSeUserIsConsent(request, userTable);
        }
        return true;
    }

    /**
     * 取出用户信息，通过Request
     *
     * @param request
     * @return
     */
    private SlUserTable getUserInfoByRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 从Request中获取用户的JSON串
        Object attribute = request.getAttribute(UserCommandKey.USER_LOGIN_SUCCESS_INFO_REQ_KEY);
        if (attribute == null) {
            clearUserInfo(request, response, jedisCluster, attribute == null ? "XG_NO_DEL" : attribute.toString());
            throw new CheckValueException("服务器当前不稳定，请刷新重试");
        }

        String toStringInfo = attribute.toString();
        SlUserTable slUserTable = null;
        try {
            // 转换用户信息
            slUserTable = JsonUtils.jsonToPojo(toStringInfo, SlUserTable.class);
            request.setAttribute(UserCommandKey.USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY, slUserTable);
        } catch (Exception e) {
            clearUserInfo(request, response, jedisCluster, attribute == null ? "XG_NO_DEL" : attribute.toString());
            // 抛出异常信息供捕捉
            throw new LogException("用户Json转换异常:JSON:" + toStringInfo + "-SlUserTable实体类：" + new SlUserTable());
        }

        return slUserTable;
    }

    /**
     * 判断用户是否同意本站规则
     *
     * @param request     请求
     * @param slUserTable 用户信息
     */
    private void senSeUserIsConsent(HttpServletRequest request, SlUserTable slUserTable) {
        // 判断用户是否同意规则
        if (StringUtils.isEmpty(jedisCluster.get(UserCommandKey.USER_CONSENT_REDIS_KEY + slUserTable.getSlUserName()))) {
            request.setAttribute(UserCommandKey.USER_CONSENT_REQUEST_KEY, false);
        } else {
            request.setAttribute(UserCommandKey.USER_CONSENT_REQUEST_KEY, true);
        }
    }

    /**
     * 清空用户的数据信息
     *
     * @param request      请求
     * @param response     响应
     * @param jedisCluster Jedis依赖
     */
    public static void clearUserInfo(HttpServletRequest request, HttpServletResponse response,
                                     JedisCluster jedisCluster, String token) {

        // 首先清空Cookie的信息
        CookieUtils.deleteCookie(request, response, UserCommandKey.USER_TOKEN_COOKIE_KEY);
        // 清空Redis中的信息
        jedisCluster.del(token);
    }
}
