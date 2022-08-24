package com.lmclearn.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lmclearn.yygh.model.user.Patient;

import java.util.AbstractList;
import java.util.List;

/**
 * <p>
 * 就诊人表 服务类
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-18
 */
public interface PatientService extends IService<Patient> {

    Patient getPatientById(Integer id);

    List<Patient> findAllUserId(Long userId);
}
