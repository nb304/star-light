package org.king2.sl.common.key;

/**
 * 用户操作的KEY
 * 包含了Cookie中的redis当中的
 */
public class UserCommandKey {

    /**
     * 用户验证码存在客户端Cookie当中的KEY
     */
    public static final String USER_CODE_COOKIE_KEY = "www.taobao.com";

    /**
     * 用户唯一TOKEN存在客户端Cookie当中的KEY
     */
    public static final String USER_TOKEN_COOKIE_KEY = "www.baidu.com";

    /**
     * 用户同意本站规则存在Redis中的Key
     */
    public static final String USER_CONSENT_REDIS_KEY = "USER_CONSENT_REDIS_KEY";

    /**
     * 用户是否登录，存在Request中的Key
     */
    public static final String USER_IS_LOGIN_REQUEST_KEY = "login";

    /**
     * 用户登录成功存在Request中的信息
     */
    public static final String USER_LOGIN_SUCCESS_INFO_REQ_KEY = "loginInfo";

    /**
     * 用户登录成功转换成后存在Request中的信息
     */
    public static final String USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY = "loginInfoPojo";

    /**
     * 用户Cookie中的VALUE的Key
     */
    public static final String USER_CODE_COOKIE_VALUE = "USER_CODE_COOKIE_VALUE";

    /**
     * 用户同意规则，存在Request中的key
     */
    public static final String USER_CONSENT_REQUEST_KEY = "USER_CONSENT_REQUEST_KEY";

    /**
     * 用户历史记录-Cookie
     */
    public static final String USER_PAST_COOKIE_KEY = "USER_PAST_COOKIE_KEY";
    /**
     * 用户历史记录-REDIS
     */
    public static final String USER_PAST_REDIS_KEY = "USER_PAST_REDIS_KEY";
}
