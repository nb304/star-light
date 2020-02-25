package org.king2.sl.data.controller;

import org.king2.sl.common.annotations.CheckLogin;
import org.king2.sl.common.utils.SystemResult;
import org.king2.sl.data.appoint.MoneyManageAppoint;
import org.king2.sl.data.service.MoneyManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 金额管理的Controller
 */
@Controller
@Validated
public class MoneyManageController {

    @Autowired
    private MoneyManageAppoint moneyManageAppoint;

    @Autowired
    private MoneyManageService defaultMoneyManageService;

    @RequestMapping("/model")
    public String model() {
        return "model";
    }

    /**
     * 我的星币首页
     *
     * @param request
     * @return
     */
    @RequestMapping("/money")
    @CheckLogin
    public String index(HttpServletRequest request) {

        moneyManageAppoint.indexInit(request);
        request.setAttribute("navType", "money");
        return "StarMoneyManage/index";
    }

    /**
     * 显示金额的主页
     *
     * @param request
     * @return
     */
    @RequestMapping("/show/add/money")
    @CheckLogin
    public String showIndex(HttpServletRequest request) throws Exception {
        moneyManageAppoint.init(request);
        return "StarMoneyManage/AddMoney";
    }

    /**
     * 查询充值记录的详细信息
     *
     * @param request 请求
     * @param index   父页面的Array索引
     * @param orderId orderId
     * @return
     */
    @RequestMapping("/money/get")
    public String get(HttpServletRequest request,
                      @NotBlank(message = "索引不应该为空") @Pattern(regexp = "[0-9]{1,}", message = "索引类型错误") String index,
                      @NotBlank(message = "订单不应该为空") @Pattern(regexp = "[0-9]{1,}", message = "订单类型错误") String orderId) throws Exception {

        request.setAttribute("index", index);
        SystemResult orderMoneyInfo = defaultMoneyManageService.getOrderMoneyInfo(Integer.parseInt(orderId));
        request.setAttribute("result", orderMoneyInfo);
        return "StarMoneyManage/GetMoney";
    }
}
