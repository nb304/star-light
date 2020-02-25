package org.king2.sl.data.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.king2.sl.common.key.UserCommandKey;
import org.king2.sl.common.utils.SystemResult;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 一个检查是否登录了的切面类
 */
@Aspect
@Component
public class CheckLoginAspect {

    @Around("@annotation(org.king2.sl.common.annotations.CheckLogin)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {

        /**
         * 获取到所有的参数信息
         */
        Object[] args = pjp.getArgs();
        HttpServletRequest request = null;
        int count = 0;
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof HttpServletRequest) {
                request = (HttpServletRequest) args[i];
                count = i;
                break;
            }
        }

        if (request == null) {
            return new SystemResult(100, "状态错误");
        }

        // 判断用户是否登录成功
        Object attribute = request.getAttribute(UserCommandKey.USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY);
        if (attribute != null) {
            request.setAttribute(UserCommandKey.USER_LOGIN_SUCCESS_POJO_INFO_REQ_KEY, attribute);
            args[count] = request;
            Object proceed = pjp.proceed(args);
            return proceed;
        } else {
            return new SystemResult("请登录后操作");
        }

    }
}
