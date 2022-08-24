package com.lmclearn.yygh.orders.controller;

import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.enums.PaymentTypeEnum;
import com.lmclearn.yygh.orders.service.PaymentService;
import com.lmclearn.yygh.orders.service.WeixinService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {
    @Resource
    private WeixinService weixinPayService;
    @Resource
    private PaymentService paymentService;

    /**
     * 下单 生成二维码
     */
    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable("orderId") Long orderId) {
        Map map = weixinPayService.createNative(orderId);
        return R.ok().data(map);
    }

    //根据订单id查询订单状态
    @GetMapping("/queryPayStatus/{orderId}")
    public R queryPayStatus(@PathVariable Long orderId){
        Map<String,String> map=weixinPayService.queryPayStatus(orderId);
        if (map==null){
            return  R.error().message("支付失败");
        }
        //支付成功
        if("SUCCESS".equals(map.get("trade_state"))){
            //更新订单状态
            //更改订单状态，处理支付结果
            String out_trade_no = map.get("out_trade_no");
            paymentService.paySuccess(out_trade_no,map);
            //更新支付记录状态
            return R.ok().message("支付成功");
        }
        return R.ok().message("支付中");
    }

}