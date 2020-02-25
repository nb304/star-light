package org.king2.sl.sso.controller;

import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.utils.CookieUtils;
import org.king2.sl.common.utils.SystemResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户推出的Controller
 */
@RestController
@CrossOrigin
public class ExitController {

    @Autowired
    private JedisCluster jedisCluster;

    @RequestMapping("/exit")
    public SystemResult exit(HttpServletRequest request, HttpServletResponse response) {

        // 需要删除Cookie和Redis的信息
        String cookieValue = CookieUtils.getCookieValue(request, UserCommandKey.USER_TOKEN_COOKIE_KEY);
        // 删除Cookie和远程的Redis
        CookieUtils.deleteCookie(request, response, UserCommandKey.USER_TOKEN_COOKIE_KEY);
        jedisCluster.del(cookieValue);
        return new SystemResult("ok");
    }
}
