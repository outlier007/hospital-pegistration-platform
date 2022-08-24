package com.lmclearn.yygh.hosp.controller;

import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.hosp.service.ScheduleService;
import com.lmclearn.yygh.model.hosp.Schedule;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    //根据医院编号和科室编号查询排班规则数据
    @GetMapping("/getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getSchedulePage(@PathVariable Integer page,
                             @PathVariable Integer limit,
                             @PathVariable String hoscode,
                             @PathVariable String depcode){
        Map<String,Object> map=scheduleService.getSchedulePage(page,limit,hoscode,depcode);
        return R.ok().data(map);
    }

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    @GetMapping("/getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetail( @PathVariable String hoscode,
                                @PathVariable String depcode,
                                @PathVariable String workDate) {
        List<Schedule> list = scheduleService.getDetailSchedule(hoscode,depcode,workDate);
        return R.ok().data("detail",list);
    }
}
