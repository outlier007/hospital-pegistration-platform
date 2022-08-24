package com.lmclearn.yygh.orders.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lmclearn.yygh.common.exception.YyghException;
import com.lmclearn.yygh.enums.RefundStatusEnum;
import com.lmclearn.yygh.model.order.PaymentInfo;
import com.lmclearn.yygh.model.order.RefundInfo;
import com.lmclearn.yygh.orders.mapper.RefundInfoMapper;
import com.lmclearn.yygh.orders.service.RefundInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 退款信息表 服务实现类
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-23
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {


    //保存退款记录
    @Override
    public RefundInfo saveRefund(PaymentInfo paymentInfo) {
        QueryWrapper<RefundInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",paymentInfo.getOrderId());
        RefundInfo refundInfo = baseMapper.selectOne(queryWrapper);
        if (refundInfo!=null){
            //有退款记录直接返回
            return refundInfo;
        }
        refundInfo=new RefundInfo();
        refundInfo.setCreateTime(new Date());
        refundInfo.setOrderId(paymentInfo.getOrderId());
        refundInfo.setPaymentType(paymentInfo.getPaymentType());
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo.setSubject(paymentInfo.getSubject());

//        refundInfo.setTradeNo(paymentInfo.getTradeNo());
//        paymentInfo.setSubject("test");
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
