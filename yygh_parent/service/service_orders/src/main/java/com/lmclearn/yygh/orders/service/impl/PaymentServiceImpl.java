package com.lmclearn.yygh.orders.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lmclearn.yygh.enums.OrderStatusEnum;
import com.lmclearn.yygh.enums.PaymentStatusEnum;
import com.lmclearn.yygh.model.order.OrderInfo;
import com.lmclearn.yygh.model.order.PaymentInfo;
import com.lmclearn.yygh.orders.mapper.PaymentMapper;
import com.lmclearn.yygh.orders.service.OrderInfoService;
import com.lmclearn.yygh.orders.service.PaymentService;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Resource
    private OrderInfoService orderInfoService;
    /**
     * 保存交易记录
     *
     * @param orderInfo
     * @param paymentType 支付类型（2：微信 1：支付宝）
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderInfo.getId());
        // queryWrapper.eq("payment_type", paymentType);
        Integer count = baseMapper.selectCount(queryWrapper);
        //如果有记录就不在添加
        if (count > 0) {
            return;
        }
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd");
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(orderInfo.getAmount());
        baseMapper.insert(paymentInfo);
    }

    //更新订单状态
    //更改订单状态，处理支付结果
    @Override
    public void paySuccess(String out_trade_no, Map<String, String> map) {
        //1 更新订单状态
        QueryWrapper<OrderInfo> wrapperOrder = new QueryWrapper<>();
        wrapperOrder.eq("out_trade_no",out_trade_no);
        OrderInfo orderInfo = orderInfoService.getOne(wrapperOrder);
        //状态已经支付
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoService.updateById(orderInfo);
        //2 更新支付记录状态
        QueryWrapper<PaymentInfo> wrapperPayment = new QueryWrapper<>();
        wrapperPayment.eq("out_trade_no",out_trade_no);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapperPayment);
        //设置状态
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(map.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(map.toString());
        baseMapper.updateById(paymentInfo);
    }
}