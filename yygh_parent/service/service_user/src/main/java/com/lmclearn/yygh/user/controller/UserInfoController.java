package com.lmclearn.yygh.user.controller;


import com.google.gson.Gson;
import com.lmclearn.yygh.common.result.R;
import com.lmclearn.yygh.common.utils.JwtHelper;
import com.lmclearn.yygh.model.user.UserInfo;
import com.lmclearn.yygh.user.service.UserInfoService;
import com.lmclearn.yygh.user.utils.AuthContextHolder;
import com.lmclearn.yygh.user.utils.ConstantPropertiesUtil;
import com.lmclearn.yygh.user.utils.HttpClientUtils;
import com.lmclearn.yygh.vo.user.LoginVo;
import com.lmclearn.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-13
 */
@Api(description = "用户登录")
@RestController
@RequestMapping("/api/userinfo")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    //用户登录
    @ApiOperation(value = "用户登录")
    @PostMapping("login")
    public R login(@RequestBody LoginVo loginVo) {
        Map<String, Object> map = userInfoService.login(loginVo);
        return R.ok().data(map);
    }

    /**
     * 获取微信登录参数，二维码
     */
    @GetMapping("getLoginParam")
    @ResponseBody
    public R genQrConnect(HttpSession session) throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri", redirectUri);
        map.put("scope", "snsapi_login");
        map.put("state", System.currentTimeMillis() + "");//System.currentTimeM
        return R.ok().data(map);
    }

    //微信登录实现--与微信开放平台交互
    @GetMapping("/callback")
    public String callback(String code, String state) {
        //System.out.println(code+"====="+state);
//https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code
        StringBuffer accessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=").append(ConstantPropertiesUtil.WX_OPEN_APP_ID)
                .append("&secret=").append(ConstantPropertiesUtil.WX_OPEN_APP_SECRET)
                .append("&code=").append(code)
                .append("&grant_type=authorization_code");
        try {
            String jsonStr = HttpClientUtils.get(accessTokenUrl.toString());
            Gson gson = new Gson();
            Map map = gson.fromJson(jsonStr, Map.class);
            String access_token = (String) map.get("access_token");
            String openid = (String) map.get("openid");//获取到微信唯一标识
            //到数据库中查询是否有对应微信
            UserInfo userInfo = userInfoService.selectByOpenId(openid);
            if(userInfo==null){
                //第一次使用微信登录
                userInfo=new UserInfo();
                //去微信平台获取微信用户信息
                String append = new StringBuffer().append("https://api.weixin.qq.com/sns/userinfo?access_token=")
                        .append(access_token)
                        .append("&openid=")
                        .append(openid).toString();
                String userInfoStr = HttpClientUtils.get(append);
                Map json = gson.fromJson(userInfoStr, Map.class);
                userInfo.setOpenid(openid);
                userInfo.setStatus(1);
                userInfo.setNickName((String) json.get("nickname"));
                userInfoService.save(userInfo);
            }
            //返回用户信息：name，token
            Map<String, String> userMap = new HashMap<>();
            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)){
                name=userInfo.getNickName();
            }
            if (StringUtils.isEmpty(name)){
                name=userInfo.getPhone();
            }
            userMap.put("name",name);
            String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
            userMap.put("token",token);
            //查询手机号是否为空
            if(StringUtils.isEmpty(userInfo.getPhone())){
                //为空需要绑定手机号
                userMap.put("openid",openid);

            }else {
                //不为空,已经绑定手机号
                userMap.put("openid","");
            }
            //跳转到前端页面中
            return "redirect:http://localhost:3000/weixin/callback?token=" +userMap.get("token")
                    +"&openid="+userMap.get("openid")
                    +"&name="+URLEncoder.encode(userMap.get("name"),"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取用户id接口
    @GetMapping("/auth/getUserInfo")
    public R getUserInfo(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.selectById(userId);
        return R.ok().data("userInfo",userInfo);
    }

    //保存用户认证信息
    @PostMapping("auth/userAuth")
    public R saveUserAuth(@RequestBody UserAuthVo userAuthVo,HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        userInfoService.saveUserAuth(userId,userAuthVo);
        return R.ok();
    }

}

