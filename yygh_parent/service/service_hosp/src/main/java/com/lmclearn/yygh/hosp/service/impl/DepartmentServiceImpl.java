package com.lmclearn.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lmclearn.yygh.hosp.repository.DepartmentRepository;
import com.lmclearn.yygh.hosp.service.DepartmentService;
import com.lmclearn.yygh.model.hosp.Department;
import com.lmclearn.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {


    @Resource
    private DepartmentRepository departmentRepository;

    @Override
    public void remove(String hoscode, String depcode) {
        Department department = departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
        if (department != null) {
            departmentRepository.deleteById(department.getId());
        }
    }


    @Override
    public void saveDepartment(Map<String, Object> stringObjectMap) {
        //转换Department成对象
        String jsonString = JSONObject.toJSONString(stringObjectMap);
        Department department = JSONObject.parseObject(jsonString, Department.class);
        //查询MongoDB是否有科室信息
        Department targetDepartment = departmentRepository.findByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());
        if (targetDepartment != null) {
            //有，做更新
            department.setCreateTime(targetDepartment.getCreateTime());
            department.setUpdateTime(new Date());
            department.setIsDeleted(targetDepartment.getIsDeleted());
            department.setId(targetDepartment.getId());
            departmentRepository.save(department);
        } else {
            //没有，做添加
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }
    }

    @Override
    public Page findDepartmentPage(Map<String, Object> stringObjectMap) {
        String hoscode = (String) stringObjectMap.get("hoscode");
        //获取分页信息
        Integer page = Integer.parseInt((String) stringObjectMap.get("page"));
        Integer limit = Integer.parseInt((String) stringObjectMap.get("limit"));
        //设值查询条件
        Department department = new Department();
        department.setHoscode(hoscode);
        Sort sort = Sort.by(Sort.Direction.ASC, "createTime");
        Example<Department> example = Example.of(department);
        PageRequest pageRequest = PageRequest.of(page, limit, sort);

        return departmentRepository.findAll(example, pageRequest);
    }

    //根据医院编号查询医院的科室信息
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        //1.根据医院编号查询医院的科室信息
        Department department = new Department();
        department.setHoscode(hoscode);
        Example example=Example.of(department);
        List<Department> departmentList = departmentRepository.findAll(example);
        //2.组装前端所需的数据
        //根据大科室编号 bigcode 分组，获取每个大科室里面下级子科室
        //key:大科室编号，value:当前大科室下的所有子科室列表
        Map<String, List<Department>> deparmentMap =departmentList.stream().collect(Collectors.groupingBy(Department::getBigcode));

        //大科室列表
        ArrayList<DepartmentVo> bigDeptList = new ArrayList<>();

        for (Map.Entry<String,List<Department>> entry: deparmentMap.entrySet()) {
            DepartmentVo departmentVo = new DepartmentVo();//大科室对象
            String bigcode = entry.getKey();//大科室编号
            departmentVo.setDepcode(bigcode);

            List<Department> smallDeptList = entry.getValue();//大科室下的小科室列表

            departmentVo.setDepname(smallDeptList.get(0).getBigname());//大科室名字

            //封装大科室下所有小科室
            List<DepartmentVo> children = new ArrayList<>();

            for (Department department1 : smallDeptList) {
                DepartmentVo small = new DepartmentVo();
                String depcode = department1.getDepcode();
                String depname = department1.getDepname();
                small.setDepcode(depcode);
                small.setDepname(depname);

                children.add(small);
            }
            departmentVo.setChildren(children);//将每一个小科室设置到大科室中
            bigDeptList.add(departmentVo);
        }

        return bigDeptList;
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.findByHoscodeAndDepcode(hoscode, depcode);
    }
}
