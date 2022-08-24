package com.lmclearn.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lmclearn.yygh.model.hosp.Hospital;
import com.lmclearn.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface HospitalService {

    void save(Map<String, Object> stringObjectMap);

    Hospital findByHoscode(String hoscode);


    Page<Hospital> selectPage(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    void updateStatus(String id, Integer status);

    Map<String,Object> getHospitalDetailById(String id);

    List<Hospital> findByNameLike(String name);
}
