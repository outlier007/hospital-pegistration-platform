package com.lmclearn.yygh.hosp;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.lmclearn")
@MapperScan(basePackages = "com.lmclearn.yygh.hosp.mapper")
@SpringBootApplication
@EnableDiscoveryClient //开启nacos客户端支持
@EnableFeignClients(basePackages = "com.lmclearn")
public class ServiceHospApplication {
 public static void main(String[] args) {
 SpringApplication.run(ServiceHospApplication.class, args);
 }

 @Bean
 public PaginationInterceptor paginationInterceptor(){
  return new PaginationInterceptor();
 }

}
