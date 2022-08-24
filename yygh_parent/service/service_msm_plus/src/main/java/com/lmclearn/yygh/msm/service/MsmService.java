package com.lmclearn.yygh.msm.service;

import com.lmclearn.yygh.vo.msm.MsmVo;

public interface MsmService {
    boolean sendCode(String phone);

    void send(MsmVo msmVo);
}
