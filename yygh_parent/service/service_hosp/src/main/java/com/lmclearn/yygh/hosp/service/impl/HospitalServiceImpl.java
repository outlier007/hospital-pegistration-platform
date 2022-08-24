package com.lmclearn.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lmclearn.yygh.cmn.client.DictFeignClient;
import com.lmclearn.yygh.enums.DictEnum;
import com.lmclearn.yygh.hosp.repository.HospitalRepository;
import com.lmclearn.yygh.hosp.service.DepartmentService;
import com.lmclearn.yygh.hosp.service.HospitalService;
import com.lmclearn.yygh.model.hosp.Hospital;
import com.lmclearn.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class HospitalServiceImpl implements HospitalService {

    @Resource
    private HospitalRepository hospitalRepository;

    @Resource
    private DictFeignClient dictFeignClient;


    @Override
    public Hospital findByHoscode(String hoscode) {
        return hospitalRepository.findByHoscode(hoscode);
    }



    @Override
    public void save(Map<String, Object> stringObjectMap) {
        String jsonString = JSONObject.toJSONString(stringObjectMap);
        Hospital hospital = JSONObject.parseObject(jsonString, Hospital.class);
        //根据医院编号查询医院信息
        Hospital mongoHospital = hospitalRepository.findByHoscode(hospital.getHoscode());

        if(mongoHospital==null){
            //如果没有做添加操作
            //0：未上线 1：已上线
            hospital.setStatus(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);

        }else {
            //如果有做更新操作
            hospital.setStatus(mongoHospital.getStatus());
            hospital.setCreateTime(mongoHospital.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(mongoHospital.getIsDeleted());
            hospital.setId(mongoHospital.getId());
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {

        PageRequest pageRequest = PageRequest.of(page-1, limit, Sort.by(Sort.Direction.ASC, "createTime"));

        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo,hospital);

        //创建匹配器，即如何使用查询条件
        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) //改变默认字符匹配方式：模糊查询
                .withIgnoreCase(true); //改变默认大小写忽略方式：忽略大小写

        Example<Hospital> example=Example.of(hospital,matcher);
        Page<Hospital> pages = hospitalRepository.findAll(example, pageRequest);
        pages.getContent().stream().forEach(item->{
            this.packHospital(item);
        });

        return pages;
    }

    private Hospital packHospital(Hospital item) {
        //医院等级
        String hospitalLevel = dictFeignClient.getName(DictEnum.HOSTYPE.getDictCode(), item.getHostype());
        //省
        String provinceName = dictFeignClient.getName(item.getProvinceCode());
        //区、市
        String cityName = dictFeignClient.getName(item.getCityCode());
        //县
        String districtName = dictFeignClient.getName(item.getDistrictCode());
        item.getParam().put("hostypeString",hospitalLevel);
        item.getParam().put("fullAddress",provinceName+cityName+districtName+item.getAddress());
        return item;
    }

    @Override
    public void updateStatus(String id, Integer status) {
        if(status.intValue()==0||status.intValue()==1){
            Hospital hospital = hospitalRepository.findById(id).get();
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    @Override
    public Map<String,Object> getHospitalDetailById(String id) {
        Optional<Hospital> hospitalOptional = hospitalRepository.findById(id);
        Hospital hospital = hospitalOptional.get();
        hospital = this.packHospital(hospital);
        HashMap<String, Object> map = new HashMap<>();
        map.put("hospital",hospital);
        map.put("bookingRule",hospital.getBookingRule());
        //不需要重复返回
        hospital.setBookingRule(null);
        return map;
    }

    //根据医院名称查询医院
    @Override
    public List<Hospital> findByNameLike(String name) {
        return hospitalRepository.findByHosnameLike(name);
    }
}
