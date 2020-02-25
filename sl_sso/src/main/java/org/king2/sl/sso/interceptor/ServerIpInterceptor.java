package org.king2.sl.sso.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 服务器端IP
 */
public class ServerIpInterceptor implements HandlerInterceptor {

    @Autowired
    private JedisCluster jedisCluster;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sso = jedisCluster.get("SSO");
        String data = jedisCluster.get("DATA");
        request.setAttribute("SSO", sso);
        request.setAttribute("DATA", data);
        return true;
    }
}
