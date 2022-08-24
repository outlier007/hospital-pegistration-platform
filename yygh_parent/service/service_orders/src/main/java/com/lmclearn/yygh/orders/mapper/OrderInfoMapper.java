package com.lmclearn.yygh.orders.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lmclearn.yygh.model.order.OrderInfo;
import com.lmclearn.yygh.vo.order.OrderCountQueryVo;
import com.lmclearn.yygh.vo.order.OrderCountVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-20
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    //统计每天平台预约数据
    List<OrderCountVo> countOrderInfoByQuery(OrderCountQueryVo orderCountQueryVo);

}
