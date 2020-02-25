package org.king2.sl.notify.controller;

import com.alipay.api.internal.util.AlipaySignature;
import com.nb304.lock.dfs_redis_lock.service.Lock;
import org.king2.sl.common.enums.OrderStateEnum;
import org.king2.sl.common.key.BalanceCommandKey;
import org.king2.sl.common.key.MoneyCommandKey;
import org.king2.sl.common.key.OrderCommandKey;
import org.king2.sl.common.pojo.SlBalance;
import org.king2.sl.common.pojo.SlRecharge;
import org.king2.sl.common.utils.JsonUtils;
import org.king2.sl.notify.config.AliPayConfig;
import org.king2.sl.notify.mapper.LocalBalanceMapper;
import org.king2.sl.notify.mapper.LocalOrderMapper;
import org.king2.sl.notify.mapper.LocalSlMoneyMapper;
import org.king2.sl.notify.service.NotifyPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.JedisCluster;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 异步通知Controller
 */
@Controller
@RequestMapping("/zfb/pay")
public class NotifyPayController {

    /**
     * 日志
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyPayController.class);

    /**
     * 支付宝公钥
     */
    public static final String KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtReLsAZLqH71oc7kn3qeLkrlPJuSkaZ80wiB4eHrF2xZ2D9t18kJX2Oa/lWoce1kfkvvdPnSEBivrytxl9VrcUUluqMGLYZB7HHPKGeP1HAU//QedUArnpQ1oc1ZgmUFNUkLdEAvO39hQhBJE6IicrX4XjHlnZB5kI92nAHWGLS9S7Lda5g3eKkTY1bPJZaeG3E6kL46Z9oPPsRlL+tRMwpvAJNwkvfN4ACNTsb7p3LbaL5K80zlSIskzlmpIjMKNHXwV5kWnrPBaEP9a1lykcLsH/v8jkRfNhG6gm+NvNlXIrdfmtePYFt9qQhHjpJ/9noVHIbNEoiSi5rvZ8LrmQIDAQAB";

    @Autowired
    private NotifyPayService defaultNotifyPayService;


    /**
     * 支付成功
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/success")
    @Transactional(rollbackFor = Exception.class)
    public void success(HttpServletRequest request, HttpServletResponse response) throws Exception {

        System.out.println("异步开始");
        // 防止乱码
        PrintWriter out = response.getWriter();
        request.setCharacterEncoding("utf-8");

        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String str : requestParams.keySet()) {
            String name = str;
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            System.out.println("name:" + name + "    value:" + valueStr);
            params.put(name, valueStr);
        }

        // 验签
        /*boolean signVerified = AlipaySignature.rsaCheckV1(params,
                KEY, "utf-8", "RSA2"); //调用SDK验证签名

        if (!signVerified) {
            LOGGER.error("验签失败:fail");
            out.print("fail");
            return;
        }*/

        // 验签成功取出订单号以及支付状态
        String out_trade_no = params.get("out_trade_no");
        //交易状态
        String trade_status = "TRADE_SUCCESS";
        // new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");

        if ("TRADE_SUCCESS".equals(trade_status)) {
            // 交易成功
            defaultNotifyPayService.pay(out_trade_no);
            out.println("success");
        }

        out.println("error");
    }


}
