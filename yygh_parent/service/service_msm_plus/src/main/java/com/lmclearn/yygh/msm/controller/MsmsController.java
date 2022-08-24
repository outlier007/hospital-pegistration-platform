package com.lmclearn.yygh.msm.controller;

import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.msm.service.MsmService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/msm")
public class MsmsController {

    @Resource
    private MsmService msmService;

    @ApiOperation(value = "发送验证码")
    @PostMapping("/send/{phone}")
    public R sendCode(@PathVariable(value = "phone") String phone) {
        boolean flag = msmService.sendCode(phone);
        if (flag) {
            return R.ok();
        } else {
            return R.error().message("发送验证码失败");
        }
    }
}
