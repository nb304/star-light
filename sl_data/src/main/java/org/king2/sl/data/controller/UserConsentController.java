package org.king2.sl.data.controller;

import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.service.range.UserConsentManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户同意本站规则
 */
@Controller
public class UserConsentController {


    @Autowired
    private UserConsentManageService userConsentManageService;

    public static ThreadLocal<Object> local = new ThreadLocal<>();

    /**
     * 用户同意接口
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping("/consent")
    @ResponseBody
    public SystemResult consent(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String consent = userConsentManageService.consent(request, response);
        return JsonUtils.jsonToPojo(consent, SystemResult.class);
    }
}
