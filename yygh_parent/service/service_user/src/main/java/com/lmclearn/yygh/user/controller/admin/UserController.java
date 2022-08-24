package com.lmclearn.yygh.user.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.model.user.UserInfo;
import com.lmclearn.yygh.user.service.UserInfoService;
import com.lmclearn.yygh.vo.user.UserInfoQueryVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/admin/userinfo")
public class UserController {

    @Resource
    private UserInfoService userInfoService;

    //用户列表，到查询条件的分页
    @GetMapping("{page}/{limit}")
    public R list(@PathVariable Integer page,
                  @PathVariable Integer limit,
                  UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo>  pageModel=userInfoService.selectPage(page,limit,userInfoQueryVo);
        return R.ok().data("pageModel",pageModel);
    }

    //状态锁定和取消锁定
    @GetMapping("lock/{userId}/{status}")
    public R lock(
            @PathVariable("userId") Long userId,
            @PathVariable("status") Integer status){
        userInfoService.lock(userId, status);
        return R.ok();
    }

    //用户详情
    @GetMapping("show/{userId}")
    public R show(@PathVariable Long userId) {
        Map<String,Object> map = userInfoService.show(userId);
        return R.ok().data(map);
    }

    //认证审批
    @GetMapping("approval/{userId}/{authStatus}")
    public R approval(@PathVariable Long userId,@PathVariable Integer authStatus) {
        userInfoService.approval(userId,authStatus);
        return R.ok();
    }

}
