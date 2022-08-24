package com.lmclearn.yygh.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.lmclearn")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.lmclearn")
@MapperScan("com.lmclearn.yygh.user.mapper")
public class ServiceUserApplication {
 public static void main(String[] args) {
 SpringApplication.run(ServiceUserApplication.class, args);
 }
}