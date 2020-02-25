package org.king2.sl.sso.controller;

import cn.hutool.extra.mail.MailUtil;
import com.alibaba.druid.util.StringUtils;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.sso.appoint.LoginIndexAppoint;
import org.king2.sl.sso.service.UserActiveManageService;
import org.king2.sl.sso.service.UserLoginManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Decoder;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

/**
 * 登录首页Controller
 */
@Controller
public class LoginIndexController {

    /**
     * 注入登录Appoint
     */
    @Autowired
    private LoginIndexAppoint loginIndexAppoint;

    /**
     * 注入用户登录Service
     */
    @Autowired
    private UserLoginManageService defaultLoginImpl;

    /**
     * 用户默认的激活服务
     */
    @Autowired
    private UserActiveManageService defaultUserActiveService;

    /**
     * 显示登录的首页信息
     *
     * @return
     */
    @RequestMapping("/")
    public String index(HttpServletRequest request, HttpServletResponse response) {
        loginIndexAppoint.init(request, response);
        return "login";
    }

    /**
     * 用户登录
     *
     * @param request  Request
     * @param response Response
     * @param userName 用户名
     * @param passWord 密码
     * @return
     */
    @PostMapping("/login")
    @ResponseBody
    public SystemResult login(HttpServletRequest request, HttpServletResponse response,
                              String userName, String passWord) throws Exception {

        SystemResult login = defaultLoginImpl.login(request, response, userName, passWord);
        return login;
    }

    /**
     * 用户的激活
     *
     * @param uName
     * @return
     */
    @RequestMapping("/active")
    public String active(@NotBlank(message = "激活链接失效，请等待系统重发") String uName,
                         @NotBlank(message = "激活链接失效，请等待系统重发") String email) throws Exception {
        defaultUserActiveService.active(uName, email);
        return "ActiveSuccess";
    }

}
