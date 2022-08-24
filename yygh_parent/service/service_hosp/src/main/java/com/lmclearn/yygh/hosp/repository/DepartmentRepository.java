package com.lmclearn.yygh.hosp.repository;

import com.lmclearn.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DepartmentRepository extends MongoRepository<Department,String> {

    Department findByHoscodeAndDepcode(String hoscode, String depcode);
}
