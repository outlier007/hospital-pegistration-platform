package com.lmclearn.yygh.hosp.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lmclearn.yygh.model.hosp.HospitalSet;

/**
 * <p>
 * 医院设置表 服务类
 * </p>
 *
 * @author lmclearn
 * @since 2022-07-26
 */
public interface HospitalSetService extends IService<HospitalSet> {

    String getSignKey(String hoscode);
}
