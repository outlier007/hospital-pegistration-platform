package com.lmclearn.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    public String uploadFile(MultipartFile file);

}
