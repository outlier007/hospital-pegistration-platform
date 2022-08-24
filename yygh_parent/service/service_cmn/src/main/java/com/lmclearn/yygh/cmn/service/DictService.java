package com.lmclearn.yygh.cmn.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.lmclearn.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 组织架构表 服务类
 * </p>
 *
 * @author lmclearn
 * @since 2022-07-31
 */
public interface DictService extends IService<Dict> {

    /**
     * 根据id获取子元素
     * @param pid
     * @return
     */
    List<Dict> gitDictListByPid(Integer pid);

    /**
     * 导出数据
     * @param response
     */
    void exportExcel(HttpServletResponse response);

    /**
     * 导入数据
     * @param file
     */
    void importData(MultipartFile file) throws IOException;

    /**
     * 根据上级编码和值获取数据字典名称
     * @param parentDictCode
     * @param value
     * @return
     */
    String getNameByParentDictCodeAndValue(String parentDictCode,String value);

    List<Dict> findByDictCode(String dictCode);
}
