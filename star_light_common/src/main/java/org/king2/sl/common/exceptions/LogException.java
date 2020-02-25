package org.king2.sl.common.exceptions;

/**
 * 报错一定要写入日志
 */
public class LogException extends Exception {

    public LogException(String msg) {
        super(msg);
    }
}
