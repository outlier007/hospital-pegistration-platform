package com.lmclearn.yygh.hosp;

import com.lmclearn.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-hosp")
public interface HospitalFeignClient {
 /**
 * 根据排班id获取预约下单数据
 */
 @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
 ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);


}