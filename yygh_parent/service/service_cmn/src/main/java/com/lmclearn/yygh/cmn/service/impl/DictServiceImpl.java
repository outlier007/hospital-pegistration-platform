package com.lmclearn.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lmclearn.yygh.cmn.listener.DictListener;
import com.lmclearn.yygh.cmn.mapper.DictMapper;
import com.lmclearn.yygh.cmn.service.DictService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lmclearn.yygh.common.exception.YyghException;
import com.lmclearn.yygh.model.cmn.Dict;
import com.lmclearn.yygh.vo.cmn.DictEeVo;
import com.sun.deploy.net.URLEncoder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.Cacheable;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务实现类
 * </p>
 *
 * @author lmclearn
 * @since 2022-07-31
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //@Cacheable(value = "dict")
    @Override
    public List<Dict> gitDictListByPid(Integer pid) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", pid);
        List<Dict> dicts = baseMapper.selectList(queryWrapper);
        for (Dict dict : dicts) {
            boolean result = hasChild(dict);
            dict.setHasChildren(result);
        }
        return dicts;
    }

    @Override
    public void exportExcel(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            List<Dict> dictList = baseMapper.selectList(null);
            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());
            for (Dict dict : dictList) {
                DictEeVo dictVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictVo);
                dictVoList.add(dictVo);
            }
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void importData(MultipartFile file) throws IOException {

        EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();

    }

    //判断当前元素是否有下一级子元素，有返回true
    private boolean hasChild(Dict dict) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", dict.getId());
        Integer count = baseMapper.selectCount(queryWrapper);
        return count > 0;
    }

    @Override
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {
        //判断parentDictCode是否为空
        //为空，就只根据value来查询
        if (StringUtils.isEmpty(parentDictCode)) {
            QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
            dictQueryWrapper.eq("value", value);
            Dict dict = baseMapper.selectOne(dictQueryWrapper);
            if (dict != null) {
                return dict.getName();
            }
        } else {
            //不为空，先通过parentDictCode把dict对象查询出来，dict中的id和value查询
            QueryWrapper<Dict> dictQueryWrapper = new QueryWrapper<>();
            dictQueryWrapper.eq("dict_code", parentDictCode);
            Dict dict = baseMapper.selectOne(dictQueryWrapper);
            if (dict == null) {
                throw new YyghException(20001,"查询失败");
            }
            QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("parent_id", dict.getId());
            queryWrapper.eq("value", value);
            Dict newDict = baseMapper.selectOne(queryWrapper);
            return newDict.getName();
        }
        return null;
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(queryWrapper);
        if(dict!=null){
            Long parentId = dict.getId();
            QueryWrapper<Dict> parentQueryWrapper = new QueryWrapper<>();
            parentQueryWrapper.eq("parent_id",parentId);
            List<Dict> dicts = baseMapper.selectList(parentQueryWrapper);
            return dicts;
        }
        return null;
    }
}
