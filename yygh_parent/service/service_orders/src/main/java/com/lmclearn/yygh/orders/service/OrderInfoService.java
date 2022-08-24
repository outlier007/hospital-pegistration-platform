package com.lmclearn.yygh.orders.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lmclearn.yygh.model.order.OrderInfo;
import com.lmclearn.yygh.vo.order.OrderCountQueryVo;
import com.lmclearn.yygh.vo.order.OrderCountVo;
import com.lmclearn.yygh.vo.order.OrderQueryVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-20
 */
public interface OrderInfoService extends IService<OrderInfo> {

    Long saveOrder(String scheduleId, Integer patientId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    OrderInfo getOrderInfo(Long orderId);

    boolean cancelOrder(Long orderId);

    void patientTips();

    /**
     * 订单统计
     */
    Map<String,Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
