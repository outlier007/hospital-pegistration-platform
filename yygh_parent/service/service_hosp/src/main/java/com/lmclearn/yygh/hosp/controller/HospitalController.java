package com.lmclearn.yygh.hosp.controller;

import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.hosp.service.HospitalService;
import com.lmclearn.yygh.model.hosp.Hospital;
import com.lmclearn.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin //解决跨域问题
public class HospitalController {

    @Resource
    private HospitalService hospitalService;

    /**
     * 获取分页列表
     * @param page
     * @param limit
     * @param hospitalQueryVo
     * @return
     */
    @GetMapping("/{page}/{limit}")
    public R getHospitalPage(@PathVariable(value = "page") Integer page,
                             @PathVariable(value = "limit") Integer limit,
                             HospitalQueryVo hospitalQueryVo){
        Page<Hospital> pageObj=hospitalService.selectPage(page,limit,hospitalQueryVo);
        return R.ok().data("pages",pageObj);
    }

    //更新医院上线、下线状态
    @PutMapping("updateStatus/{id}/{status}")
    public R updateStatus(@PathVariable("id") String id,@PathVariable("status") Integer status){
        hospitalService.updateStatus(id,status);
        return R.ok();
    }

    /**
     * 获取医院详情信息
     * @param id
     * @return
     */
    @GetMapping("detail/{id}")
    public R detail(@PathVariable String id) {
        return R.ok().data("hospital",hospitalService.getHospitalDetailById(id));
    }
}
