package com.lmclearn.yygh.orders.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.enums.OrderStatusEnum;
import com.lmclearn.yygh.model.order.OrderInfo;
import com.lmclearn.yygh.orders.service.OrderInfoService;
import com.lmclearn.yygh.orders.utils.AuthContextHolder;
import com.lmclearn.yygh.vo.order.OrderCountQueryVo;
import com.lmclearn.yygh.vo.order.OrderCountVo;
import com.lmclearn.yygh.vo.order.OrderQueryVo;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-20
 */
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderInfoController {

    @Resource
    private OrderInfoService orderInfoService;

    //创建订单
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public R saveOrder(@PathVariable String scheduleId,@PathVariable Integer patientId){
       Long orderId= orderInfoService.saveOrder(scheduleId,patientId);
       return R.ok().data("orderId",orderId);
    }

    //订单列表（条件查询带分页）
    @GetMapping("auth/{page}/{limit}")
    public R list(@PathVariable Long page,
                  @PathVariable Long limit,
                  OrderQueryVo orderQueryVo,
                  HttpServletRequest request) {
        //设置当前用户id
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> pageParam = new Page<>(page,limit);
        IPage<OrderInfo> pageModel =orderInfoService.selectPage(pageParam,orderQueryVo);
        return R.ok().data("pageModel",pageModel);
    }

    //获取订单状态
    @GetMapping("auth/getStatusList")
    public R getStatusList() {
        return R.ok().data("statusList", OrderStatusEnum.getStatusList());
    }

    //根据订单id查询订单详情
    @GetMapping("auth/getOrders/{orderId}")
    public R getOrders(@PathVariable Long orderId) {
        OrderInfo orderInfo = orderInfoService.getOrderInfo(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }

    //取消预约
    @GetMapping("auth/cancelOrder/{orderId}")
    public R cancelOrder(@PathVariable Long orderId){
        boolean flag= orderInfoService.cancelOrder(orderId);
        return R.ok().data("flag",flag);
    }

    //统计
    @PostMapping("inner/getCountMap")
    public Map<String,Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo){
        return orderInfoService.getCountMap(orderCountQueryVo);
    }

}

