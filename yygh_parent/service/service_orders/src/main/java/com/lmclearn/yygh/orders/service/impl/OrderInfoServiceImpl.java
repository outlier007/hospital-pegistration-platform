package com.lmclearn.yygh.orders.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmclearn.yygh.common.exception.YyghException;
import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.enums.OrderStatusEnum;
import com.lmclearn.yygh.hosp.HospitalFeignClient;
import com.lmclearn.yygh.model.order.OrderInfo;
import com.lmclearn.yygh.model.user.Patient;
import com.lmclearn.yygh.orders.mapper.OrderInfoMapper;
import com.lmclearn.yygh.orders.service.OrderInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lmclearn.yygh.orders.service.WeixinService;
import com.lmclearn.yygh.orders.utils.HttpRequestHelper;
import com.lmclearn.yygh.rabbit.MqConst;
import com.lmclearn.yygh.rabbit.RabbitService;
import com.lmclearn.yygh.user.client.PatientFeignClient;
import com.lmclearn.yygh.vo.hosp.ScheduleOrderVo;
import com.lmclearn.yygh.vo.msm.MsmVo;
import com.lmclearn.yygh.vo.order.OrderCountQueryVo;
import com.lmclearn.yygh.vo.order.OrderCountVo;
import com.lmclearn.yygh.vo.order.OrderMqVo;
import com.lmclearn.yygh.vo.order.OrderQueryVo;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-20
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Resource
    private HospitalFeignClient hospitalFeignClient;

    @Resource
    private PatientFeignClient patientFeignClient;

    @Resource
    private RabbitService rabbitService;

    @Resource
    private WeixinService weixinService;

    @Override
    public Long saveOrder(String scheduleId, Integer patientId) {
        //根据scheduleId查询出医院排班信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        //根据patientId获取就诊人信息
        R r = patientFeignClient.getPatientById(patientId);
        Patient patient = JSONObject.parseObject(JSONObject.toJSONString(r.getData().get("patient")), Patient.class);
        //平台系统调用第三方医院系统：确认是否还能挂号
        //使用map集合封装需要传过医院数据
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode", scheduleOrderVo.getHoscode());
        paramMap.put("depcode", scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId", scheduleOrderVo.getHosScheduleId());
        paramMap.put("reserveDate", new DateTime(scheduleOrderVo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount", scheduleOrderVo.getAmount()); //挂号费用
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType", patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex", patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone", patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode", patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode", patient.getDistrictCode());
        paramMap.put("address", patient.getAddress());
        //联系人
        paramMap.put("contactsName", patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone", patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        //String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey
        paramMap.put("sign", "");
        //使用httpclient发送请求，请求医院接口
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");
        int code = result.getIntValue("code");
        if (code != 200) {
            //不能挂号抛出异常
            throw new YyghException(20001, "挂号异常");
        }
        //可以挂号
        //把上面得到的三部分信息插入到order_info表中
        OrderInfo orderInfo = new OrderInfo();
        JSONObject jsonObject = result.getJSONObject("data");
        //预约记录唯一标识（医院预约记录主键）
        String hosRecordId = jsonObject.getString("hosRecordId");
        //预约序号
        Integer number = jsonObject.getInteger("number");
        //取号时间
        String fetchTime = jsonObject.getString("fetchTime");
        //取号地址
        String fetchAddress = jsonObject.getString("fetchAddress");
        //设置添加数据--排班数据
        BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
        //订单号
        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        //用户id
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setScheduleId(scheduleOrderVo.getHosScheduleId());
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId.longValue());
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        //设置添加数据--医院接口返回数据
        orderInfo.setHosRecordId(hosRecordId);
        orderInfo.setNumber(number);
        orderInfo.setFetchTime(fetchTime);
        orderInfo.setFetchAddress(fetchAddress);
        baseMapper.insert(orderInfo);
        //更新排班数据中的剩余预约人数
        //排班可预约数
        Integer reservedNumber = jsonObject.getInteger("reservedNumber");
        //排班剩余预约数
        Integer availableNumber = jsonObject.getInteger("availableNumber");
        //给就诊人发送预约成功短信提醒 todo
        // 使用rabbitMQ
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setReservedNumber(reservedNumber);
        orderMqVo.setAvailableNumber(availableNumber);
        orderMqVo.setScheduleId(scheduleId);
        //封装短信信息
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(patient.getPhone());
        msmVo.setTemplateCode("您{code}预约的议程");
        orderMqVo.setMsmVo(msmVo);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        //返回订单id
        return orderInfo.getId();
    }

    //订单列表（条件查询带分页）
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo) {
        //orderQueryVo获取条件值
        String name = orderQueryVo.getKeyword(); //医院名称
        Long patientId = orderQueryVo.getPatientId(); //就诊人名称
        String orderStatus = orderQueryVo.getOrderStatus(); //订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();
        //对条件值进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(name)) {
            wrapper.like("hosname", name);
        }
        if (!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id", patientId);
        }
        if (!StringUtils.isEmpty(orderStatus)) {
            wrapper.eq("order_status", orderStatus);
        }
        if (!StringUtils.isEmpty(reserveDate)) {
            wrapper.ge("reserve_date", reserveDate);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);
        }
        //调用mapper的方法
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        //编号变成对应值封装
        pages.getRecords().stream().forEach(item -> {
            this.packOrderInfo(item);
        });
        return pages;
    }

    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

    //根据订单id查询订单详情
    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return this.packOrderInfo(orderInfo);
    }

    //取消预约
    @Override
    public boolean cancelOrder(Long orderId) {
        //1.先判断当前时间是否过了平台退号截止时间
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        DateTime dateTime = new DateTime(orderInfo.getQuitTime());
        if (dateTime.isBeforeNow()) {
            //过了截止时间
            throw new YyghException(20001, "已过退号截止时间");
        }
        //2.没有过，平台系统调用医院系统，确认是否可以取消预约
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        JSONObject jsonObject = HttpRequestHelper.sendRequest(reqMap, "http://localhost:9998/order/updateCancelStatus");
        if (jsonObject.getIntValue("code") != 200) {
            //2.1 如果返回不能取消则抛出异常
            throw new YyghException(20001, "不能取消");
        }
        //2.2如果可以取消，取消，完成退款
        boolean flag = weixinService.refund(orderId);

        if (!flag) {
            throw new YyghException(20001, "退款失败");
        }
        //3.更新订单表订单状态，支付记录表支付记录状态
        //更改订单状态
        orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
        this.updateById(orderInfo);
        //发送mq信息更新预约数 我们与下单成功更新预约数使用相同的mq信息，不设置可预约数与剩余预约数，接收端可预约数减
        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(orderInfo.getScheduleId());
        //4.更新排班数据+1，发送短信提醒
        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(orderInfo.getPatientPhone());
        orderMqVo.setMsmVo(msmVo);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        return true;
    }

    //发送消息到msm，让其发送消息给用户
    @Override
    public void patientTips() {
        // String nowTime = new DateTime().toString("yyyy-MM-dd");
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        // 给预约了今天就诊的用户发消息
        queryWrapper.eq("reserve_date", new DateTime().toString("yyyy-MM-dd"));
        queryWrapper.ne("order_status", OrderStatusEnum.CANCLE.getStatus());
        List<OrderInfo> orderInfoList = baseMapper.selectList(queryWrapper);
        for (OrderInfo orderInfo : orderInfoList) {
            //短信提醒
            MsmVo msmVo = new MsmVo();
            msmVo.setPhone(orderInfo.getPatientPhone());
            //msmVo.setParam(param);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }

    //mp：
    @Override
    public Map<String,Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        HashMap<String, Object> map = new HashMap<>();
        List<OrderCountVo> orderCountVos = baseMapper.countOrderInfoByQuery(orderCountQueryVo);
        List<String> dateList = orderCountVos.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        List<Integer> countList = orderCountVos.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        map.put("dateList",dateList);
        map.put("countList",countList);
        return map;
    }
}