package org.king2.sl.common.key;

/**
 * 用户数据超时时间的操作类
 */
public class UserTimeoutCommand {

    /**
     * 用户验证码存在客户端Cookie当中的超时时间（单位:s）
     */
    public static final Integer USER_CODE_COOKIE_TIME_OUT = 60 * 10;

    /**
     * 用户验证码存在客户端REDIS当中的超时时间（单位:s）
     */
    public static final Integer USER_CODE_REDIS_TIME_OUT = 60 * 10;

    /**
     * 用户唯一TOKEN存在客户端Cookie当中的超时时间（单位:s）
     */
    public static final Integer USER_TOKEN_COOKIE_TIME_OUT = 60 * 60 * 24;

    /**
     * 用户唯一TOKEN存在客户端REDIS当中的超时时间（单位:s）
     */
    public static final Integer USER_TOKEN_REDIS_TIME_OUT = 60 * 60 * 24;
}
