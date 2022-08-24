package com.lmclearn.yygh.hosp.controller;

import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.hosp.service.DepartmentService;

import com.lmclearn.yygh.vo.hosp.DepartmentVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 医院排班信息接口
 */
@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    /**
     * 根据医院编号查询医院的科室信息
     * @param hoscode
     * @return
     */
    @GetMapping("getDeptList/{hoscode}")
    public R getDeptList(@PathVariable("hoscode") String hoscode) {
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return R.ok().data("list",list);
    }
}
