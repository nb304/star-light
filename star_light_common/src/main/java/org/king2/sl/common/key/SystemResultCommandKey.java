package org.king2.sl.common.key;

/**
 * 系统返回值状态
 */
public class SystemResultCommandKey {

    /**
     * 成功
     */
    public static final Integer SUCCESS = 200;

    /**
     * 校验值失败异常
     */
    public static final Integer CHECK_VALUE_ERROR = 100;

    /**
     * 系统异常
     */
    public static final Integer SYSTEM_ERROR = 500;

    /**
     * 用户没有登入异常
     */
    public static final Integer USER_NOT_LOGIN = 101;
}
