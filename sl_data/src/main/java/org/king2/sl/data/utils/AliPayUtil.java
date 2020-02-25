package org.king2.sl.data.utils;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.king2.sl.data.alipay.ALiPayBean;
import org.king2.sl.data.config.AliPayConfig;

/**
 * 阿里支付的工具类
 */
public class AliPayUtil {

    /**
     * 连接
     *
     * @param aliPayBean
     * @return
     */
    public static String connect(ALiPayBean aliPayBean) throws AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient(
                AliPayConfig.getKey("gatewayUrl"),//支付宝网关
                AliPayConfig.getKey("app_id"),//appid
                AliPayConfig.getKey("merchant_private_key"),//商户私钥
                "json",
                AliPayConfig.getKey("charset"),//字符编码格式
                AliPayConfig.getKey("alipay_public_key"),//支付宝公钥
                AliPayConfig.getKey("sign_type")//签名方式
        );

        // 设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();

        //页面跳转同步通知页面路径
        alipayRequest.setReturnUrl(AliPayConfig.getKey("return_url"));
        // 服务器异步通知页面路径
        alipayRequest.setNotifyUrl(AliPayConfig.getKey("notify_url"));
        //封装参数
        alipayRequest.setBizContent(JSON.toJSONString(aliPayBean));
        //3、请求支付宝进行付款，并获取支付结果
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        //返回付款信息
        return result;
    }
}
