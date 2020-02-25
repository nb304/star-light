package org.king2.sl.data.alipay;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 支付宝Bean
 */
@Data
@Accessors(chain = true)
public class ALiPayBean {

    // 订单号
    private String out_trade_no;
    // 订单名称
    private String subject;
    // 付款金额
    private String total_amount;
    // 商品描述
    private String body;
    // 超时时间
    private String timeout_express = "10m";
    private String product_code = "FAST_INSTANT_TRADE_PAY";
}
