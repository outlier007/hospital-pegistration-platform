package com.lmclearn.yygh.hosp.controller.api;

import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.hosp.service.DepartmentService;
import com.lmclearn.yygh.hosp.service.HospitalService;
import com.lmclearn.yygh.hosp.service.ScheduleService;
import com.lmclearn.yygh.model.hosp.Hospital;
import com.lmclearn.yygh.model.hosp.Schedule;
import com.lmclearn.yygh.vo.hosp.DepartmentVo;
import com.lmclearn.yygh.vo.hosp.HospitalQueryVo;
import com.lmclearn.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 与用户页面的api接口
 */
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {

    @Resource
    private HospitalService hospitalService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private ScheduleService scheduleService;

    //获取带查询条件的首页医院分页列表
    @GetMapping("/{page}/{limit}")
    public R index(@PathVariable Integer page,
                   @PathVariable Integer limit,
                   HospitalQueryVo hospitalQueryVo) {
        Page<Hospital> hospitals = hospitalService.selectPage(page, limit, hospitalQueryVo);
        return R.ok().data("pages", hospitals);
    }

    //根据医院名称查询医院
    @GetMapping("/findByNameLike/{name}")
    public R findByNameLike(@PathVariable(value = "name") String name) {
        List<Hospital> list = hospitalService.findByNameLike(name);
        return R.ok().data("list", list);
    }

    //根据医院编号查询医院科室列表
    @GetMapping("/department/list/{hoscode}")
    public R getDepartmentListByHoscode(@PathVariable String hoscode) {
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return R.ok().data("list", list);
    }

    //根据医院编号查询医院信息
    @GetMapping("/info/{hoscode}")
    public R info(@PathVariable String hoscode) {
        Hospital hospital = hospitalService.findByHoscode(hoscode);
        return R.ok().data("hospital", hospital);
    }

    //获取可预约排班分页日期数据
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getBookingSchedule(@PathVariable Integer page,
                                @PathVariable Integer limit,
                                @PathVariable String hoscode,
                                @PathVariable String depcode){
        Map<String,Object> map= scheduleService.getBookingSchedule(page,limit,hoscode,depcode);
        return R.ok().data("map",map);
    }

    //获取排班数据
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(
            @PathVariable String hoscode,
            @PathVariable String depcode,
            @PathVariable String workDate) {
        List<Schedule> scheduleList = scheduleService.getDetailSchedule(hoscode, depcode,workDate);
        return R.ok().data("scheduleList",scheduleList);
    }

    //根据排班id查询排班信息
    @GetMapping("getSchedule/{id}")
    public R getScheduleList(@PathVariable String id) {
        Schedule schedule = scheduleService.getScheduleById(id);
        return R.ok().data("schedule",schedule);
    }

    //根据排班id获取预约下单数据
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId) {
        return scheduleService.getScheduleOrderVo(scheduleId);
    }
}
