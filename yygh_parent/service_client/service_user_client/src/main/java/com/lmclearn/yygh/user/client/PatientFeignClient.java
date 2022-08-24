package com.lmclearn.yygh.user.client;

import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.model.user.Patient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-user")
public interface PatientFeignClient {
    //根据id获取就诊人信息
    @GetMapping("/api/userinfo/patient/auth/get/{id}")
    public R getPatientById(@PathVariable("id") Integer id);

}
