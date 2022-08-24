package com.lmclearn.yygh.orders.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.lmclearn.yygh.common.exception.YyghException;
import com.lmclearn.yygh.enums.PaymentTypeEnum;
import com.lmclearn.yygh.enums.RefundStatusEnum;
import com.lmclearn.yygh.model.order.OrderInfo;
import com.lmclearn.yygh.model.order.PaymentInfo;
import com.lmclearn.yygh.model.order.RefundInfo;
import com.lmclearn.yygh.orders.service.OrderInfoService;
import com.lmclearn.yygh.orders.service.PaymentService;
import com.lmclearn.yygh.orders.service.RefundInfoService;
import com.lmclearn.yygh.orders.service.WeixinService;
import com.lmclearn.yygh.orders.utils.ConstantPropertiesUtils;
import com.lmclearn.yygh.orders.utils.HttpClient;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinServiceImpl implements WeixinService {

    @Resource
    private OrderInfoService orderInfoService;

    @Resource
    private PaymentService paymentService;

    @Resource
    private RefundInfoService refundInfoService;

    //根据订单id获取生成微信支付二维码所需的数据
    @Override
    public Map createNative(Long orderId) {
        //1.根据订单id获取订单信息
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        //2.添加交易支付记录
        paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
        //3.准备参数，xml格式，调用微信服务器接口进行支付
        Map paramMap = new HashMap();
        paramMap.put("appid", ConstantPropertiesUtils.APPID);
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        Date reserveDate = orderInfo.getReserveDate();
        String reserveDateString = new DateTime(reserveDate).toString("yyyy-MM-dd");
        String body = reserveDateString + "就诊" + orderInfo.getDepname();
        paramMap.put("body", body);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        //paramMap.put("total_fee", orderInfo.getAmount().multiply(new BigDecimal("100")).longValue()+"");
        paramMap.put("total_fee", "1");//为了测试
        //终端ip
        paramMap.put("spbill_create_ip", "127.0.0.1");
        //回调地址
        paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNoyify");
        paramMap.put("trade_type", "NATIVE");
        try {
            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            httpClient.setHttps(true);
            httpClient.post();

            String content = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            //分装返回结果集
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }
    }

    //根据订单的id查询订单的状态
    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        //1、封装参数
        Map paramMap = new HashMap<>();
        paramMap.put("appid", ConstantPropertiesUtils.APPID);
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
        paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        try {
            //2、设置请求
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();
            String content = client.getContent();
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据订单id退款
    @Override
    public boolean refund(Long orderId) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        PaymentInfo paymentInfo = paymentService.getOne(queryWrapper);
        if (paymentInfo == null) {
            throw new YyghException(20001, "没有该订单的支付记录");
        }
        //2.2.1在退款记录中添加一条退款记录数据
        RefundInfo refundInfo = refundInfoService.saveRefund(paymentInfo);
        //如果已经退款直接返回true
        if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus()) {
            return true;
        }
        //2.2.2请求微信服务器退款,需要证书支持
        Map<String, String> paramMap = new HashMap<>(8);
        paramMap.put("appid", ConstantPropertiesUtils.APPID); //公众账号ID
        paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER); //商户编号
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
        paramMap.put("transaction_id", paymentInfo.getTradeNo()); //微信订单号
        paramMap.put("out_trade_no", paymentInfo.getOutTradeNo()); //商户订单编号
        paramMap.put("out_refund_no", "tk" + paymentInfo.getOutTradeNo()); //商户退款单号
        paramMap.put("total_fee", "1");
        paramMap.put("refund_fee", "1");
        try {
            String paramXml = WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                //更改支付记录表支付记录状态
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
