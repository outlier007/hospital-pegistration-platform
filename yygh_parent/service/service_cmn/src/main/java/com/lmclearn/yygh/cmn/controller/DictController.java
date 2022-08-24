package com.lmclearn.yygh.cmn.controller;


import com.alibaba.excel.EasyExcel;
import com.lmclearn.yygh.cmn.service.DictService;
import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.model.cmn.Dict;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 组织架构表 前端控制器
 * 字典表控制层
 * </p>
 *
 * @author lmclearn
 * @since 2022-07-31
 */
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin
public class DictController {

    @Resource
    private DictService dictService;

    @PostMapping("/importData")
    public R importData(MultipartFile file) throws IOException {
        dictService.importData(file);
        return R.ok();
    }

    /**
     * 字典导出
     *
     * @param response
     */
    @GetMapping("/exportData")
    private void exportExcel(HttpServletResponse response) {
        dictService.exportExcel(response);
    }

    /**
     * 根据父id查询子元素列表
     *
     * @param pid
     * @return
     */
    @GetMapping("/childList/{pid}")
    public R gitDictListByPid(@PathVariable Integer pid) {
        List<Dict> dictList = dictService.gitDictListByPid(pid);
        return R.ok().data("list", dictList);
    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping(value = "/getName/{value}")
    public String getName(@PathVariable("value") String value) {
        return dictService.getNameByParentDictCodeAndValue("", value);
    }

    @ApiOperation(value = "获取数据字典名称")
    @GetMapping("/getName/{parentDictCode}/{value}")
    public String getName(@PathVariable("parentDictCode") String parentDictCode, @PathVariable("value") String value) {
        return dictService.getNameByParentDictCodeAndValue(parentDictCode, value);
    }

    //省份信息
    @ApiOperation(value = "根据dictCode获取下级节点")
    @GetMapping(value = "/findByDictCode/{dictCode}")
    public R findByDictCode(@PathVariable String dictCode) {
        List<Dict> list = dictService.findByDictCode(dictCode);
        return R.ok().data("list", list);
    }

}

