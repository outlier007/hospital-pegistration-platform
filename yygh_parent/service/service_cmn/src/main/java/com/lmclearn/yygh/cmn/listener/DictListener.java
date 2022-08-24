package com.lmclearn.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.lmclearn.yygh.cmn.mapper.DictMapper;
import com.lmclearn.yygh.model.cmn.Dict;
import com.lmclearn.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

import java.util.Map;

/**
 * 监听器
 */
public class DictListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;

    public DictListener(DictMapper dictMapper){
        this.dictMapper=dictMapper;
    }

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext context) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        dictMapper.insert(dict);
    }


    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
