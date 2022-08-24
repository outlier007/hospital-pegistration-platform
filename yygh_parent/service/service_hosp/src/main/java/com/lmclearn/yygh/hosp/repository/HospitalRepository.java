package com.lmclearn.yygh.hosp.repository;
import com.lmclearn.yygh.model.hosp.Department;
import com.lmclearn.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital,String> {

    Hospital findByHoscode(String hoscode);


    List<Hospital> findByHosnameLike(String name);
}