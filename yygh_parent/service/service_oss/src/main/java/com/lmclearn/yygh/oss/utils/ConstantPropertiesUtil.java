package com.lmclearn.yygh.oss.utils;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
* 常量类，读取配置文件application.properties中的配置
*/
@Component
//@PropertySource("classpath:application.properties")
@ConfigurationProperties("aliyun.oss.file")
@Data
public class ConstantPropertiesUtil  {
 private String endpoint;

 private String keyid;

 private String keysecret;

 private String bucketname;

}