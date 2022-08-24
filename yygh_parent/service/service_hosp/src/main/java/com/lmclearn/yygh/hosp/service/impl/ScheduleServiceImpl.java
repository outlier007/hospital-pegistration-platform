package com.lmclearn.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lmclearn.yygh.common.exception.YyghException;
import com.lmclearn.yygh.hosp.repository.ScheduleRepository;
import com.lmclearn.yygh.hosp.service.DepartmentService;
import com.lmclearn.yygh.hosp.service.HospitalService;
import com.lmclearn.yygh.hosp.service.ScheduleService;
import com.lmclearn.yygh.model.hosp.BookingRule;
import com.lmclearn.yygh.model.hosp.Department;
import com.lmclearn.yygh.model.hosp.Hospital;
import com.lmclearn.yygh.model.hosp.Schedule;
import com.lmclearn.yygh.vo.hosp.BookingScheduleRuleVo;
import com.lmclearn.yygh.vo.hosp.ScheduleOrderVo;
import com.lmclearn.yygh.vo.hosp.ScheduleQueryVo;
import com.lmclearn.yygh.vo.order.OrderMqVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Resource
    private ScheduleRepository scheduleRepository;

    @Resource
    private HospitalService hospitalService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private DepartmentService departmentService;

    @Override
    public void saveSchedule(Map<String, Object> paramMap) {
        //将paramMao转换为Schedule对象
        String paramMaoString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(paramMaoString, Schedule.class);
        //根据医院编号和排班编号查询，查询是否已有排班信息
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());
        if (scheduleExist != null) {
            //有，更新
            schedule.setUpdateTime(new Date());
            schedule.setId(scheduleExist.getId());
            schedule.setCreateTime(scheduleExist.getCreateTime());
            schedule.setIsDeleted(scheduleExist.getIsDeleted());
            schedule.setStatus(scheduleExist.getStatus());
            scheduleRepository.save(schedule);
        } else {
            //没有，添加
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }

    }

    @Override
    public Page findSchedulePage(Map<String, Object> stringObjectMap) {

        String hoscode = (String) stringObjectMap.get("hoscode");//医院
        String depcode = (String) stringObjectMap.get("depcode");//科室
        Integer page = Integer.parseInt((String) stringObjectMap.get("page"));
        Integer limit = Integer.parseInt((String) stringObjectMap.get("limit"));
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.ASC, "createTime"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(scheduleQueryVo, schedule);
        schedule.setIsDeleted(0);

        Example<Schedule> example = Example.of(schedule);
        return scheduleRepository.findAll(example, pageable);
    }

    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule != null) {
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    //根据医院编号和科室编号查询排班规则数据
    @Override
    public Map<String, Object> getSchedulePage(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> map = new HashMap<>();

        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);
        //聚合对象
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),//设置聚合条件
                Aggregation.group("workDate")//根据workDate分组
                        .first("workDate").as("workDate")
                        .count().as("docCount")//当日值班医生数
                        .sum("reservedNumber").as("reservedNumber")//对总的预约数求和
                        .sum("availableNumber").as("availableNumber"),//剩余的可预约数求和
                Aggregation.sort(Sort.Direction.ASC, "workDate"),//按时间排序
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        //1.获取当前页列表
        //第一个参数：聚合对象
        //第二个参数：输入类型，与集合名称对应的pojo类的字节码
        //第三个参数：输出类型，聚合后要把字段分装到哪个对象中
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();//当前页对应的列表
        //设置周几
        for (BookingScheduleRuleVo mappedResult : mappedResults) {
            String dayOfWeek = this.getDayOfWeek(new DateTime(mappedResult.getWorkDate()));
            mappedResult.setDayOfWeek(dayOfWeek);
        }

        map.put("bookingScheduleRuleList", mappedResults);
        //2.获取总记录数 total
        //聚合对象
        Aggregation aggregation1 = Aggregation.newAggregation(
                Aggregation.match(criteria),//设置聚合条件
                Aggregation.group("workDate")//根据workDate分组
        );
        AggregationResults<BookingScheduleRuleVo> aggregate1 = mongoTemplate.aggregate(aggregation1, Schedule.class, BookingScheduleRuleVo.class);
        int total = aggregate1.getMappedResults().size();
        map.put("total", total);
        //获取医院名称
        Hospital hospital = hospitalService.findByHoscode(hoscode);
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hosname", hospital.getHosname());
        map.put("baseMap", baseMap);
        return map;
    }

    //根据医院编号 、科室编号和工作日期，查询排班详细信息
    @Override
    public List<Schedule> getDetailSchedule(String hoscode, String depcode, String workDate) {
        List<Schedule> byHoscodeAndDepcodeAndWorkDate = scheduleRepository.findByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        return byHoscodeAndDepcodeAndWorkDate;
    }

    /**
     * 根据日期获取周几数据
     *
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {

        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

    //获取可预约排班分页数据
    @Override
    public Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        //获取预约规则
        Hospital hospital = hospitalService.findByHoscode(hoscode);
        if (null == hospital) {
            throw new YyghException(20001, "没有相关医院信息");
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约日期分页数据
        IPage iPage = this.getListDate(page, limit, bookingRule);
        //获取当前页时间列表
        List<Date> records = iPage.getRecords();

        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(records);
        //聚合对象
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),//设置聚合条件
                Aggregation.group("workDate")//根据workDate分组
                        .first("workDate").as("workDate")
                        .count().as("docCount")//当日值班医生数
                        .sum("reservedNumber").as("reservedNumber")//对总的预约数求和
                        .sum("availableNumber").as("availableNumber")//剩余的可预约数求和
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        //聚合列表
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        //key：workDate
        Map<Date, BookingScheduleRuleVo> collect = mappedResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate, BookingScheduleRuleVo -> BookingScheduleRuleVo));
        List<BookingScheduleRuleVo> newList =new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            Date date = records.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = collect.get(date);
            if (bookingScheduleRuleVo == null) {
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室剩余预约数 -1表示无号
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前日期是周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            int len = records.size();
            //最后一页最后一条记录为即将预约 状态 0：正常 1：即将放号 -1：当天已停止挂号
            if (page == iPage.getPages() && i == len - 1) {
                bookingScheduleRuleVo.setStatus(1);
            } else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //当天预约如果过了停号时间， 不能预约
            if (page == 1 && i == 0) {
                //当天结束放号时间
                DateTime dateTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if (dateTime.isBeforeNow()){
                    //时间过了
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            newList.add(bookingScheduleRuleVo);
        }
        Map<String, Object> result = new HashMap<>();
        //可预约日期规则数据
        result.put("bookingScheduleList", newList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.findByHoscode(hoscode).getHosname());
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;

    }

    /**
     * 获取可预约日期分页数据
     */
    private IPage<Date> getListDate(int page, int limit, BookingRule bookingRule) {
        //当天放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //预约周期
        int cycle = bookingRule.getCycle();
        //如果当天放号时间已过，则预约周期后一天为即将放号时间，周期加1
        if (releaseTime.isBeforeNow()) {
            cycle += 1;
        }
        //可预约所有日期，最后一天显示即将放号倒计时
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //计算当前预约日期
            DateTime curDateTime = new DateTime().plusDays(i);
            dateList.add(new DateTime(curDateTime.toString("yyyy-MM-dd")).toDate());
        }
        //日期分页，由于预约周期不一样，页面一排最多显示7天数据，多了就要分页显示
        List<Date> pageDateList = new ArrayList<>();
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        if (end > dateList.size()) {
            end = dateList.size();
        }
        for (int i = start; i < end; i++) {
            pageDateList.add(dateList.get(i));
        }
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Date>(page, limit, dateList.size());
        //设置每页显示的数据
        iPage.setRecords(pageDateList);
        return iPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " " + timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    //根据排班id查询排班信息
    @Override
    public Schedule getScheduleById(String id) {
        Schedule schedule = scheduleRepository.findById(id).get();
        this.packageSchedule(schedule);
        return schedule;
    }

    private void packageSchedule(Schedule schedule) {
        Hospital hospital = hospitalService.findByHoscode(schedule.getHoscode());
        Department department = departmentService.getDepartment(schedule.getHoscode(), schedule.getDepcode());
        schedule.getParam().put("hosname",hospital.getHosname());
        schedule.getParam().put("depname",department.getDepname());
        Date workDate = schedule.getWorkDate();
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(workDate)));
    }

    //根据排班id获取预约下单数据
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //排班信息
        Schedule schedule = this.getScheduleById(scheduleId);
        if(schedule==null){
            throw new YyghException();
        }
        //获取预约规则信息
        Hospital hospital = hospitalService.findByHoscode(schedule.getHoscode());
        if(hospital==null){
            throw new YyghException();
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if (bookingRule==null){
            throw new YyghException();
        }
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospital.getHosname());
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname((String) schedule.getParam().get("depname"));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());
        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());
        return scheduleOrderVo;
    }

    //更新预约数
    @Override
    public void update(OrderMqVo orderMqVo) {
        Schedule schedule=null;
        if(orderMqVo.getAvailableNumber()!=null){
            //传了剩余可预约数，是下单
            schedule = scheduleRepository.findById(orderMqVo.getScheduleId()).get();
            schedule.setAvailableNumber(orderMqVo.getAvailableNumber());
            schedule.setReservedNumber(orderMqVo.getReservedNumber());
        }else {
            // 没有传剩余可预约数，是取消预约
            schedule =scheduleRepository.findByHosScheduleId(orderMqVo.getScheduleId());
            schedule.setAvailableNumber(schedule.getAvailableNumber()+1);
        }
        scheduleRepository.save(schedule);
    }
}
