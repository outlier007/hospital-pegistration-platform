package com.lmclearn.yygh.statistics.controller;

import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.order.client.OrderFeignClient;
import com.lmclearn.yygh.vo.order.OrderCountQueryVo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/admin/statistics")
public class StatisticsController {
    @Resource
    private OrderFeignClient orderFeignClient;

    // 获取订单统计数据
    @PostMapping("getCountMap")
    public R getCountMap(OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> map = orderFeignClient.getCountMap(orderCountQueryVo);
        return R.ok().data(map);
    }
}
