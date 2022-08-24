package com.lmclearn.yygh.oss.controller;

import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.oss.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;


@RestController
@RequestMapping("/admin/oss/file")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    public R upload(@RequestParam("file")MultipartFile file){
        String file1 = fileService.uploadFile(file);
        return R.ok().message("文件上传成功").data("url",file1);
    }
}
