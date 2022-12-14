package com.lmclearn.yygh.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lmclearn.yygh.model.order.OrderInfo;
import com.lmclearn.yygh.model.order.PaymentInfo;

import java.util.Map;

public interface PaymentService extends IService<PaymentInfo> {
 /**
 * 保存交易记录
 * @param order
 * @param paymentType 支付类型（1：微信 2：支付宝）
 */
 void savePaymentInfo(OrderInfo order, Integer paymentType);

    void paySuccess(String out_trade_no, Map<String, String> map);
}