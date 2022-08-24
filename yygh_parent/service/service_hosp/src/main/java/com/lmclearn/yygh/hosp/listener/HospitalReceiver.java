package com.lmclearn.yygh.hosp.listener;


import com.lmclearn.yygh.hosp.service.ScheduleService;
import com.lmclearn.yygh.model.hosp.Schedule;
import com.lmclearn.yygh.rabbit.MqConst;
import com.lmclearn.yygh.rabbit.RabbitService;
import com.lmclearn.yygh.vo.msm.MsmVo;
import com.lmclearn.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

//mq监听器
@Component
public class HospitalReceiver {

    @Resource
    private ScheduleService scheduleService;
    @Resource
    private RabbitService rabbitService;

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue(name = MqConst.QUEUE_ORDER, durable = "true"),
                    exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_ORDER),
                    key = MqConst.ROUTING_ORDER)})

    public void consume(OrderMqVo orderMqVo, Message message, Channel channel) {
        //下单成功更新预约数
        scheduleService.update(orderMqVo);

        //发送短信
        MsmVo msmVo = orderMqVo.getMsmVo();
        if (null != msmVo) {
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM,msmVo);
        }
    }

}
