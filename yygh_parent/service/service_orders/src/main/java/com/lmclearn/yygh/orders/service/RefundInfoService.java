package com.lmclearn.yygh.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lmclearn.yygh.model.order.PaymentInfo;
import com.lmclearn.yygh.model.order.RefundInfo;

/**
 * <p>
 * 退款信息表 服务类
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-23
 */
public interface RefundInfoService extends IService<RefundInfo> {

    RefundInfo saveRefund(PaymentInfo paymentInfo);
}
