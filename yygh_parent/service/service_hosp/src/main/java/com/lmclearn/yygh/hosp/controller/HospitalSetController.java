package com.lmclearn.yygh.hosp.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.hosp.service.HospitalSetService;
import com.lmclearn.yygh.hosp.util.MD5;
import com.lmclearn.yygh.model.hosp.HospitalSet;
import com.lmclearn.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiOperation;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * 医院设置表 前端控制器
 * </p>
 *
 * @author lmclearn
 * @since 2022-07-26
 */
//医院设置接口
@Api(description = "医院设置接口")
@RestController
@RequestMapping("/admin/hosp/hospital-set")
//@CrossOrigin
@Slf4j
public class HospitalSetController {

    @Resource
    private HospitalSetService hospitalSetService;

    //localhost:8201/hosp/hospital-set/finAll
    //查询所有医院设置
    @ApiOperation(value = "医院设置列表")
    @GetMapping("/finAll")
    public R findAll(){
        List<HospitalSet> list = hospitalSetService.list();
        if (list!=null||list.size()>0){
            return R.ok();
        }else {
            return R.error();
        }
    }

    @ApiOperation(value = "医院设置删除")
    @DeleteMapping("/remove/{id}")
    public R removeById(@PathVariable Long id){
        boolean result = hospitalSetService.removeById(id);
        if(result){
            return R.ok();
        }else {
            return R.error();
        }
    }

    @ApiOperation(value = "查询医院分页信息")
    @GetMapping("/{current}/{limit}")
    public R pageList(@PathVariable Integer current, @PathVariable Integer limit){
        Page page = new Page(current, limit);
        hospitalSetService.page(page);
        return R.ok().data("total",page.getTotal()).data("rows",page.getRecords());
    }

    @ApiOperation(value = "分页条件查询医院信息")
    @PostMapping("/{page}/{limit}")
    public R pageQuery(@PathVariable Integer page, @PathVariable Integer limit , @RequestBody HospitalSetQueryVo hospitalSetQueryVo){
        Page<HospitalSet> pageParam = new Page<>(page,limit);
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
            String hoscode = hospitalSetQueryVo.getHoscode();
            String hosname = hospitalSetQueryVo.getHosname();
            if(!StringUtils.isEmpty(hoscode)){
                queryWrapper.like("hoscode",hoscode);
            }
            if(!StringUtils.isEmpty(hosname)){
                queryWrapper.like("hosname",hosname);
            }
            hospitalSetService.page(pageParam,queryWrapper);
        pageParam.getTotal();
        return R.ok().data("total",pageParam.getTotal()).data("rows",pageParam.getRecords());
    }

    //医院添加
    @ApiOperation(value = "医院添加")
    @PostMapping("/saveHospSet")
    public  R save (@RequestBody HospitalSet hospitalSet ){
        hospitalSet.setStatus(0);
        //签名秘钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));
        hospitalSetService.save(hospitalSet);
        return R.ok();
    }

    //根据医院id查询
    @ApiOperation(value = "根据医院id查询")
    @GetMapping("/getHospSet/{id}")
    public R getById(@PathVariable Integer id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return R.ok().data("item" ,hospitalSet);
    }

    //根据id修改医院设置
    @ApiOperation(value = "根据id修改医院设置")
    @PostMapping("/updateHospSet")
    public R updateById(@RequestBody HospitalSet hospitalSet){
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

    //批量删除医院
    @ApiOperation(value = "批量删除医院")
    @DeleteMapping("/batchRemove")
    public R batchRemoveHospitalSet(@RequestBody List<Integer> idList){
        boolean isSuccess = hospitalSetService.removeByIds(idList);
        if(isSuccess){
            return R.ok();
        }else {
            return R.error();
        }
    }

    //医院设置锁定和解锁
    @PutMapping("lockHospitalSet/{id}/{status}")
    public R lockHospitalSet(@PathVariable Integer id,@PathVariable Integer status){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        hospitalSetService.updateById(hospitalSet);
        return R.ok();
    }

}

