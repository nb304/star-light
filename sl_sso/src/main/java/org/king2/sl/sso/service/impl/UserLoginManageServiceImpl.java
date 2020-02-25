package org.king2.sl.sso.service.impl;

import com.nb304.lock.dfs_redis_lock.service.Lock;
import lombok.Data;
import org.king2.sl.common.exceptions.CheckValueException;
import org.king2.sl.common.key.BookCommandKey;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.key.UserTimeoutCommand;
import org.king2.sl.common.pojo.SlBook;
import org.king2.sl.common.pojo.SlUserTable;
import org.king2.sl.common.state.UserState;
import org.king2.sl.common.utils.*;
import org.king2.sl.sso.mapper.LocalLsUserTableMapper;
import org.king2.sl.sso.pool.TaskPool;
import org.king2.sl.sso.service.UserLoginManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * 默认的用户登录接口
 */
@Service("defaultLoginImpl")
@SuppressWarnings("all")
public class UserLoginManageServiceImpl implements UserLoginManageService {

    /**
     * 注入JedisCluster
     */
    @Autowired
    private JedisCluster jedisCluster;
    /**
     * 注入本地的用户表
     */
    @Autowired
    private LocalLsUserTableMapper localLsUserTableMapper;
    /**
     * 存放着本次的code验证码信息
     */
    private ThreadLocal<String> code = new ThreadLocal<>();

    /**
     * 注入分布式锁
     */
    @Autowired
    private Lock lock;

    @Override
    public SystemResult login(HttpServletRequest request, HttpServletResponse response, String userName, String passWord) throws Exception {

        // 进行初始化的校验
        initCheck(new UserLoginData(request, response, userName, passWord));
        // 定义是否校验成功
        boolean isCheck = true;
        // 校验失败的信息
        String errorMsg = "";
        // 校验用户名和密码是否存在数据库当中
        String s = AESUtil.aesDecrypt(passWord);
        SlUserTable slUserTable = localLsUserTableMapper.get(userName, MD5Utils.md5(s.substring(1, s.length() - 1)));
        if (slUserTable == null) {
            isCheck = false;
            errorMsg = "用户名或密码错误!";
        } else if ((slUserTable.getSlUserState() + "").equals(UserState.NO_ACTIVE.getValue() + "")) {
            isCheck = false;
            errorMsg = "该用户还没有激活，清查看邮箱!";
        } else if (!(slUserTable.getSlUserState() + "").equals(UserState.NORMAL.getValue() + "")) {
            isCheck = false;
            errorMsg = "当前用户状态异常，请联系管理员2246483156!";
        }
        // 判断是否校验成功
        if (!isCheck) {
            /**
             * 校验失败的话我们需要重新刷新Cookie和Redis中的code（验证码）信息
             */
            refreshCookieAndRedisInfo(request, response);
            throw new CheckValueException(errorMsg);
        }
        /**
         * 到这里说明登陆成功了，登录成功我们需要删除Cookie中的值，删除Redis中的值
         * 并且重新写入新的Token信息
         */
        loginSuccessAfterHandler(request, response, slUserTable);

        return new SystemResult("登录成功");
    }

    /**
     * 登录成功后的操作
     *
     * @param request
     * @param response
     * @param slUserTable
     */
    private void loginSuccessAfterHandler(HttpServletRequest request, HttpServletResponse response, SlUserTable slUserTable) throws Exception {
        // 删除掉Cookie中和Redis中的操作
        String cookieValue = code.get();
        CookieUtils.deleteCookie(request, response, UserCommandKey.USER_CODE_COOKIE_KEY);
        // 删除Redis中的操作
        jedisCluster.del(cookieValue);

        String tokenValue = UserCommandKey.USER_TOKEN_COOKIE_KEY + MD5Utils.md5(UUID.randomUUID().toString() + System.currentTimeMillis());
        // 删除成功之后存入一个唯一token到客户端的Cookie中
        CookieUtils.setCookie(request, response, UserCommandKey.USER_TOKEN_COOKIE_KEY, tokenValue, UserTimeoutCommand.USER_TOKEN_COOKIE_TIME_OUT);
        // 将Token作为Redis中的Key存入Redis当中
        jedisCluster.setex(tokenValue, UserTimeoutCommand.USER_TOKEN_REDIS_TIME_OUT, JsonUtils.objectToJson(slUserTable));

        // 修改用户的最后登录时间
        localLsUserTableMapper.updateUserLastLoginTime(new Date(), slUserTable.getSlUserId());

        //  使用线程池将Cookie中历史记录的值同步到Redis当中
        TaskPool.TASK_POOL.execute(() -> {
            syncPastCookieValueGotoRedis(request, response, slUserTable);
        });
    }

    /**
     * 同步历史记录到redis当中
     *
     * @param request
     * @param response
     */
    private void syncPastCookieValueGotoRedis(HttpServletRequest request, HttpServletResponse response, SlUserTable slUserTable) {
        lock.lock(BookCommandKey.BOOK_PAST_REDIS_LOCK, 10000);
        // 获取用户的Cookie信息
        try {
            String pastValue = CookieUtils.getCookieValue(request, UserCommandKey.USER_PAST_COOKIE_KEY, true);
            if (!StringUtils.isEmpty(pastValue)) {
                // 进行同步
                List<SlBook> slBooks1 = JsonUtils.jsonToList(pastValue, SlBook.class);
                List<SlBook> slBooks = ((slBooks1 == null) ? new LinkedList<>() : slBooks1);
                for (SlBook slBook : slBooks) {
                    slBooks.add(0, slBook);
                }

                // 重新写入Redis中
                jedisCluster.set(UserCommandKey.USER_PAST_REDIS_KEY, JsonUtils.objectToJson(slBooks));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 系统文件路径
            String pathStr = ClassUtils.getDefaultClassLoader().getResource("").getPath().substring(1);
            try {
                lock.unlock(pathStr + "/unlock.lua", BookCommandKey.BOOK_PAST_REDIS_LOCK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 重新刷新Cookie和Redis的信息
     *
     * @param request
     * @param response
     */
    private void refreshCookieAndRedisInfo(HttpServletRequest request, HttpServletResponse response) {

        // 刷新Cookie的信息
        // 写入用户的Cookie当中
        String cookieValue = code.get();
        CookieUtils.setCookie(request, response, UserCommandKey.USER_CODE_COOKIE_KEY, cookieValue, UserTimeoutCommand.USER_CODE_COOKIE_TIME_OUT);
        /**
         * 写入Cookie成功，重新写入Redis当中，后面需要重新取出作比较
         * 验证码存在Redis中的Key就是存在Cookie中的Value
         */
        jedisCluster.setex(cookieValue, UserTimeoutCommand.USER_CODE_REDIS_TIME_OUT, "true");
    }

    /**
     * 初始的校验，校验验证码是否正确，校验用户名或密码是否正确
     *
     * @param data
     * @throws CheckValueException
     */
    private void initCheck(UserLoginData data) throws CheckValueException {
        // 校验验证码
        checkCode(data);
        // 校验用户名和密码
        checkUserNameAndPassWord(data);
    }


    /**
     * 校验验证码是否通过
     *
     * @param data 封装了reqeust，resp，uname，upass
     * @return 验证码是否存在
     */
    private void checkCode(UserLoginData data) throws CheckValueException {
        // 校验是否通过
        boolean isCheck = true;
        // 获取Cookie信息
        String cookieValue = CookieUtils.getCookieValue(data.getRequest(), UserCommandKey.USER_CODE_COOKIE_KEY);
        if (StringUtils.isEmpty(cookieValue)) {
            isCheck = false;
        } else if (StringUtils.isEmpty(jedisCluster.get(cookieValue))) {
            isCheck = false;
        }
        if (!isCheck) throw new CheckValueException("超时的登录，请重新打开或刷新登录页面。");
        code.set(cookieValue);
    }

    /**
     * 校验用户名或密码是否通过
     *
     * @param data 封装了reqeust，resp，uname，upass
     * @return 用户名密码是否校验正确
     */
    private void checkUserNameAndPassWord(UserLoginData data) throws CheckValueException {
        if (StringUtils.isEmpty(data.getUserName()) || StringUtils.isEmpty(data.getPassWord())) {
            throw new CheckValueException("用户名或密码不能为空");
        }
    }

}

/**
 * 用户登录的信息类
 */
@Data
class UserLoginData {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String userName;
    private String passWord;

    public UserLoginData(HttpServletRequest request, HttpServletResponse response, String userName, String passWord) {
        this.request = request;
        this.response = response;
        this.userName = userName;
        this.passWord = passWord;
    }
}
