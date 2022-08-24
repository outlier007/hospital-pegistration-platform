package com.lmclearn.yygh.msm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = {"com.lmclearn"})
@EnableDiscoveryClient //开启nacos客户端支持
public class ServiceMsmsApplication {
    //
    public static void main(String[] args) {
        SpringApplication.run(ServiceMsmsApplication.class,args);
    }
}
