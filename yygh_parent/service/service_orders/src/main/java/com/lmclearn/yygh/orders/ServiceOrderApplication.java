package com.lmclearn.yygh.orders;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.lmclearn.yygh.orders.mapper")
@ComponentScan(basePackages = "com.lmclearn")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.lmclearn")//可以通过FeignClient调用其他模块
public class ServiceOrderApplication {
    //
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class,args);
    }
}
