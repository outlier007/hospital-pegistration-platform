package com.lmclearn.yygh.user.controller;


import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.model.user.Patient;
import com.lmclearn.yygh.user.service.PatientService;
import com.lmclearn.yygh.user.utils.AuthContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-18
 */
//就诊人管理接口
@RestController
@RequestMapping("/api/userinfo/patient")
public class PatientController {

    @Resource
    private PatientService patientService;
    //添加就诊人
    @PostMapping("auth/save")
    public R savePatient(@RequestBody Patient patient, HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    //删除就诊人
    @DeleteMapping("auth/remove/{id}")
    public R removeById(@PathVariable("id") Integer id){
        patientService.removeById(id);
        return R.ok();
    }
    //根据id获取就诊人信息
    @GetMapping("auth/get/{id}")
    public R getPatientById(@PathVariable("id") Integer id){
        Patient patient = patientService.getPatientById(id);
        return R.ok().data("patient",patient);
    }
    //修改就诊人信息
    @PutMapping("auth/update")
    public R updatePatient(@RequestBody Patient patient){
        patientService.updateById(patient);
        return R.ok();
    }
    //获取当前用户的就诊人列表
    @GetMapping("auth/findAll")
    public R findAll(HttpServletRequest request){
        //获取当前登录用户id
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list = patientService.findAllUserId(userId);
        return R.ok().data("list",list);
    }
}

