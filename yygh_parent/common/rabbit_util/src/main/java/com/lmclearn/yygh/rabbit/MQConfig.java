package com.lmclearn.yygh.rabbit;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MQConfig {

    //可向MQ中发送对象
    @Bean
    public MessageConverter messageConverter() {
        //将对象转换成json数据
        return new Jackson2JsonMessageConverter();
    }
}