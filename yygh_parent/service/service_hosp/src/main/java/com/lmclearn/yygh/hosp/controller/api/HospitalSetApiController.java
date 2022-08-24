package com.lmclearn.yygh.hosp.controller.api;

import com.lmclearn.yygh.common.exception.YyghException;
import com.lmclearn.yygh.hosp.service.DepartmentService;
import com.lmclearn.yygh.hosp.service.HospitalService;
import com.lmclearn.yygh.hosp.service.HospitalSetService;
import com.lmclearn.yygh.hosp.service.ScheduleService;
import com.lmclearn.yygh.hosp.util.HttpRequestHelper;
import com.lmclearn.yygh.hosp.util.MD5;
import com.lmclearn.yygh.model.hosp.Hospital;
import com.lmclearn.yygh.result.Result;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 医院管理api接口
 */
@RestController
@RequestMapping("/api/hosp")
public class HospitalSetApiController {
    @Resource
    private HospitalService hospitalService;
    @Resource
    private HospitalSetService hospitalSetService;

    @Resource
    private ScheduleService scheduleService;

    @Resource
    private DepartmentService departmentService;

    /**
     * 上传医院
     *
     * @param request
     * @return
     */
    @PostMapping("/saveHospital")
    public Result saveHospital(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //校验
//        String sign = (String) stringObjectMap.get("sign");
//        String platformSignKey = hospitalSetService.getSignKey((String) stringObjectMap.get("hoscode"));
//        if (!StringUtils.isEmpty(sign) && !StringUtils.isEmpty(platformSignKey) && MD5.encrypt(platformSignKey).equals(sign)) {
//            //传输过程中“+”转换为了“ ”，因此我们要转换回来
//            String logoData = (String)stringObjectMap.get("logoData");
//            logoData = logoData.replaceAll(" ","+");
//            stringObjectMap.put("logoData",logoData);
//            hospitalService.save(stringObjectMap);
//        }else {
//            throw new YyghException(20001,"校验失败");
//        }
        //传输过程中“+”转换为了“ ”，因此我们要转换回来
        String logoData = (String) stringObjectMap.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        stringObjectMap.put("logoData",logoData);
        hospitalService.save(stringObjectMap);
        return Result.ok();
    }

    /**
     * 查询医院信息
     *
     * @return
     */
    @PostMapping("/hospital/show")
    public Result save(HttpServletRequest request) {
        Hospital hospital;
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验
//        //1 获取医院系统传递过来的签名,签名进行MD5加密
//        String sign = (String) stringObjectMap.get("sign");
//        //2 根据传递过来医院编码，查询数据库，查询签名
//        String platformSignKey = hospitalSetService.getSignKey((String) stringObjectMap.get("hoscode"));
//        //3 把数据库查询签名进行MD5加密
//        if (!StringUtils.isEmpty(sign) && !StringUtils.isEmpty(platformSignKey) && MD5.encrypt(platformSignKey).equals(sign)) {
//            String hoscode = (String) stringObjectMap.get("hoscode");
//            hospital=hospitalService.findByHoscode("hoscode");
//        }else {
//            throw new YyghException(20001,"校验失败");
//        }
        String hoscode = (String) stringObjectMap.get("hoscode");
        hospital = hospitalService.findByHoscode(hoscode);
        return Result.ok(hospital);
    }

    /**
     * 上传科室信息
     *
     * @return
     */
    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验 todo
        departmentService.saveDepartment(stringObjectMap);
        return Result.ok();
    }

    /**
     * 获取科室分页信息
     *
     * @param request
     * @return
     */
    @PostMapping("/department/list")
    public Result getDepartmentPage(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //检验 todo
        Page page = departmentService.findDepartmentPage(stringObjectMap);

        return Result.ok(page);
    }

    /**
     * 删除科室
     *
     * @param request
     * @return
     */
    @PostMapping("/department/remove")
    public Result removeDepartment(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //必须参数校验 TODO
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");
        departmentService.remove(hoscode, depcode);
        return Result.ok();
    }

    /**
     * 上传排班信息
     *
     * @return
     */
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request) {
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验 todo
        scheduleService.saveSchedule(paramMap);
        return Result.ok();
    }

    /**
     * 分页查询排班信息
     * @param request
     * @return
     */
    @PostMapping("/schedule/list")
    public Result getSchedule(HttpServletRequest request) {
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验 todo
        Page page=scheduleService.findSchedulePage(stringObjectMap);
        return Result.ok(page);
    }

    /**
     * 删除排班信息
     * @return
     */
    @PostMapping("/schedule/remove")
    public Result removeSchedule(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        //校验 todo
        String hoscode = (String)stringObjectMap.get("hoscode");//医院编号
        //必填
        String hosScheduleId = (String)stringObjectMap.get("hosScheduleId");//排班编号
        //根据医院编号与排班编号删除科室
        scheduleService.remove(hoscode,hosScheduleId);
        return Result.ok();
    }

}
