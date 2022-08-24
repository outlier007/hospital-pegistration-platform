package com.lmclearn.yygh.oss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(value = "com.lmclearn")
public class OssMainStarter {
    public static void main(String[] args) {
        SpringApplication.run(OssMainStarter.class,args);
    }
}
