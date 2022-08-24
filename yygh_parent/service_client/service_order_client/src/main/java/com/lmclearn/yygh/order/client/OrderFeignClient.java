package com.lmclearn.yygh.order.client;

import com.lmclearn.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "service-orders")
@Repository
public interface OrderFeignClient {
 /**
 * 获取订单统计数据
 */
 @PostMapping("/api/order/orderInfo/inner/getCountMap")
 Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo);
}