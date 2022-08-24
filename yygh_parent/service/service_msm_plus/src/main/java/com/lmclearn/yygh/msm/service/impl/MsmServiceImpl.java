package com.lmclearn.yygh.msm.service.impl;


import com.lmclearn.yygh.msm.service.MsmService;
import com.lmclearn.yygh.msm.utils.HttpUtils;
import com.lmclearn.yygh.msm.utils.RandomUtil;
import com.lmclearn.yygh.vo.msm.MsmVo;
import org.apache.http.HttpResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MsmServiceImpl implements MsmService {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Override
    public boolean sendCode(String phone) {
        String s = redisTemplate.opsForValue().get(phone);
        //判断是否已经请求验证码
        if (!StringUtils.isEmpty(s)){
            return true;
        }
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "8f23b32e810a41cc99e6625fa8b2ff3d";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);//手机号
        String code = RandomUtil.getFourBitRandom();//四位验证码
        querys.put("param", "code:"+code);
        querys.put("tpl_id", "TP1711063");
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            redisTemplate.opsForValue().set(phone,code,365, TimeUnit.DAYS);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void send(MsmVo msmVo) {
        String phone = msmVo.getPhone();
        System.out.println("就诊人预约挂号成功短信已发送到："+phone);
    }
}
