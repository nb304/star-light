package org.king2.sl.sso.controller;

import org.king2.sl.common.annotations.Log;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.sso.service.UserRegisterManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

/**
 * 注册Controller
 */
@Controller
public class RegisterController {

    /**
     * 注入默认的注册接口
     */
    @Autowired
    private UserRegisterManageService defaultRegisterImpl;


    /**
     * 显示注册页面
     *
     * @return
     */
    @RequestMapping("/register")
    public String registerIndex(HttpServletRequest request) {
        // 初始化SESSION
        request.getSession().setAttribute("register_session", "true");
        return "register";
    }

    /**
     * 用户注册
     *
     * @return 返回用户注册的状态
     */
    @Log
    @RequestMapping("/register/log")
    @ResponseBody
    public SystemResult register(@NotBlank(message = "请重新注册") String content, HttpServletRequest request) throws Exception {

        SystemResult register = defaultRegisterImpl.register(content, request);
        return register;
    }
}
