package com.lmclearn.yygh.task;

import com.lmclearn.yygh.rabbit.MqConst;
import com.lmclearn.yygh.rabbit.RabbitService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
public class ScheduledTask {

    @Resource
    private RabbitService rabbitService;

    @Scheduled(cron = "0/15 * * * * ?")
    public void printTime(){
        System.out.println(new Date().toLocaleString());
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,"XXXX");
    }

}
