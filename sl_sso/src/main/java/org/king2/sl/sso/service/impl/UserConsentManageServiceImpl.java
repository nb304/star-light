package org.king2.sl.sso.service.impl;

import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.exceptions.LogException;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.key.UserTimeoutCommand;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.utils.CookieUtils;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.sso.mapper.LocalLsUserTableMapper;
import org.king2.sl.sso.service.UserConsentManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 默认的用户同意规则Service
 */
@RestController("defaultUserConsentService")
public class UserConsentManageServiceImpl implements UserConsentManageService {

    /**
     * 注入操作Jedis的依赖
     */
    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 注入本地的用户表
     */
    @Autowired
    private LocalLsUserTableMapper localLsUserTableMapper;

    /**
     * 存放当前用户的TOKEN信息
     */
    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    /**
     * 存放当前用户的信息
     */
    private static final ThreadLocal<SlUserTable> USER_INFO = new ThreadLocal<>();

    /**
     * 用户是否已经完成激活锁
     */
    private static final Object USER_CONSENT_LOCK = new Object();

    @Override
    @RequestMapping("/consent")
    public SystemResult consent(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 获取登录的用户名信息
        String userName = checkUserLoginAndReturnUserName(request, response);

        synchronized (USER_CONSENT_LOCK) {
            // 更新用户信息之前需要判断用户是否已经完成激活
            SlUserTable slUserTable = localLsUserTableMapper.getUserName(userName);
            if ((slUserTable.getSlConsent() + "").equals("1")) {
                // 更新用户的信息
                localLsUserTableMapper.updateUserConsent(userName);

                // 更新成功，需要重新将信息写回Redis
                updateUserConsentAndReWriteRedis();
            } else {
                // 将已同意本站规则的用户重新写入Redis
                jedisCluster.set(UserCommandKey.USER_CONSENT_REDIS_KEY + slUserTable.getSlUserName(), "true");
            }
        }

        return new SystemResult("OK");
    }

    /**
     * 更新用户的同意规则，并将用户的数据重新写入Redis
     */
    private void updateUserConsentAndReWriteRedis() throws Exception {
        // 取出用户的Token和信息
        String token = TOKEN.get();
        SlUserTable slUserTable = USER_INFO.get();
        slUserTable.setSlConsent(2);
        // 将用户信息更新到Redis中
        jedisCluster.setex(token, UserTimeoutCommand.USER_TOKEN_REDIS_TIME_OUT, JsonUtils.objectToJson(slUserTable));

        // 将已同意本站规则的用户重新写入Redis
        jedisCluster.set(UserCommandKey.USER_CONSENT_REDIS_KEY + slUserTable.getSlUserName(), "true");
    }

    /**
     * 校验用户是否登录，并返回用户名
     *
     * @param request 请求
     * @return 返回用户名
     */
    private String checkUserLoginAndReturnUserName(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // 用户存在Cookie的信息
        String cookieValue = CookieUtils.getCookieValue(request, UserCommandKey.USER_TOKEN_COOKIE_KEY);
        if (StringUtils.isEmpty(cookieValue)) {
            throw new CheckValueException("用户没有登录，不能同意本站规则。");
        }
        // 从Redis当中获取用户信息
        String redisUserInfo = jedisCluster.get(cookieValue);
        if (StringUtils.isEmpty(redisUserInfo)) {
            throw new CheckValueException("用户没有登录，不能同意本站规则。");
        }

        SlUserTable slUserTable = null;
        try {
            // 转换用户信息
            slUserTable = JsonUtils.jsonToPojo(redisUserInfo, SlUserTable.class);
        } catch (Exception e) {
            // 删除本地Cookie以及远程Redis信息
            CookieUtils.deleteCookie(request, response, UserCommandKey.USER_TOKEN_COOKIE_KEY);
            jedisCluster.del(cookieValue);
            // 抛出异常信息供捕捉
            throw new LogException("用户Json转换异常:JSON:" + redisUserInfo + "-SlUserTable实体类：" + new SlUserTable());
        }
        // 查看当前用户是否已经同意本站规则
        if (!StringUtils.isEmpty(jedisCluster.get(UserCommandKey.USER_CONSENT_REDIS_KEY + slUserTable.getSlUserName()))) {
            // 说明已经同意
            throw new CheckValueException("用户已经同意本站规则。");
        }

        // 设置ThreadLocal
        TOKEN.set(cookieValue);
        USER_INFO.set(slUserTable);
        return slUserTable.getSlUserName();

    }


}
