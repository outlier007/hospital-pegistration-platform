package com.lmclearn.yygh.hosp.controller;

import com.lmclearn.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/yygh/user")
@Slf4j
@RestController
//@CrossOrigin
public class UserController {

    @PostMapping("/login")
    public R login(){
        return R.ok().data("token","admin-token");
    }

    @GetMapping("/info")
    public R info(String token){
        log.info("token:" +token);
        return R.ok().data("roles","[admin]")
                .data("introduction","I am a super administrator")
                .data("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                .data("name","Super Admin");
    }
}
