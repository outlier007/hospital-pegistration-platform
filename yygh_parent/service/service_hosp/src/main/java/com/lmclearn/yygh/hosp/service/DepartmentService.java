package com.lmclearn.yygh.hosp.service;

import com.lmclearn.yygh.model.hosp.Department;
import com.lmclearn.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {

    void saveDepartment(Map<String, Object> stringObjectMap);

    Page findDepartmentPage(Map<String,Object> stringObjectMap);

    void remove(String hoscode, String depcode);

    List<DepartmentVo> findDeptTree(String hoscode);

    Department getDepartment(String hoscode, String depcode);
}
