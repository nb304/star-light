package org.king2.sl.data.controller;

import org.king2.sl.common.annotations.CheckLogin;
import org.king2.sl.data.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户支付Controller
 */
@Controller
public class PayController {

    @Autowired
    private PayService defaultPayService;


    /**
     * 用户发起支付
     *
     * @param moneyTypeId 金额的类型Id
     * @return 返回支付页面
     */
    @RequestMapping("/pay")
    @ResponseBody
    @CheckLogin
    public String pay(@NotBlank(message = "金额类型不能为空")
                      @Pattern(regexp = "[0-9]{1,}", message = "金额类型错误") String moneyTypeId, HttpServletRequest request)
            throws Exception {

        String pay = defaultPayService.pay(moneyTypeId, request);
        return pay;
    }
}
