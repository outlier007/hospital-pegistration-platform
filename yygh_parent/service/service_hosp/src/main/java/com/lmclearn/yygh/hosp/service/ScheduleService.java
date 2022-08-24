package com.lmclearn.yygh.hosp.service;

import com.lmclearn.yygh.model.hosp.Schedule;
import com.lmclearn.yygh.vo.hosp.ScheduleOrderVo;
import com.lmclearn.yygh.vo.order.OrderMqVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void saveSchedule(Map<String, Object> paramMap);

    Page findSchedulePage(Map<String, Object> stringObjectMap);

    void remove(String hoscode, String hosScheduleId);

    Map<String, Object> getSchedulePage(Integer page, Integer limit, String hoscode, String depcode);

    List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule getScheduleById(String id);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    void update(OrderMqVo orderMqVo);
}
