package com.lmclearn.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmclearn.yygh.common.exception.YyghException;
import com.lmclearn.yygh.common.utils.JwtHelper;
import com.lmclearn.yygh.enums.AuthStatusEnum;
import com.lmclearn.yygh.model.user.Patient;
import com.lmclearn.yygh.model.user.UserInfo;
import com.lmclearn.yygh.user.mapper.UserInfoMapper;
import com.lmclearn.yygh.user.service.PatientService;
import com.lmclearn.yygh.user.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lmclearn.yygh.vo.user.LoginVo;
import com.lmclearn.yygh.vo.user.UserAuthVo;
import com.lmclearn.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author lmclearn
 * @since 2022-08-13
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1.获取用户输入的手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //2.对手机号和验证码进行非空校验
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code)) {
            throw new YyghException(20001, "用户名和密码不能为空");
        }
        // 3.比较Redis中的验证码
        String redisCode = redisTemplate.opsForValue().get(phone);
        if (!code.equals(redisCode)) {
            throw new YyghException(20001, "验证码不正确");
        }

        //4.判断是否是首次登录，如果是进行注册
        UserInfo userInfo = new UserInfo();
        //判断是否是微信登录
        String openid = loginVo.getOpenid();
        if (StringUtils.isEmpty(openid)) {
            //手机登录
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.eq("phone", phone);
            userInfo = baseMapper.selectOne(userInfoQueryWrapper);
            if (userInfo == null) {
                //首次登录，进行注册
                userInfo = new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                userInfo.setCreateTime(new Date());
                baseMapper.insert(userInfo);
            }
        } else {
            //微信登录,绑定手机号
            UserInfo userInfoFinal = new UserInfo();
            QueryWrapper<UserInfo> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("phone", phone);
            UserInfo userInfoPhone = baseMapper.selectOne(phoneWrapper);
            if (userInfoPhone != null) {
                //先用电话号码登录过
                BeanUtils.copyProperties(userInfoPhone, userInfoFinal);
                baseMapper.delete(phoneWrapper);
            } else {
                //先用微信登录
                userInfoFinal.setPhone(phone);
            }
            QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
            userInfoQueryWrapper.eq("openid", openid);
            userInfo = baseMapper.selectOne(userInfoQueryWrapper);
            userInfoFinal.setOpenid(userInfo.getOpenid());
            userInfoFinal.setNickName(userInfo.getNickName());
            userInfoFinal.setId(userInfo.getId());
            userInfoFinal.setStatus(userInfo.getStatus());
            baseMapper.updateById(userInfoFinal);
        }
        //5.判断用户的状态

        if (userInfo.getStatus() == 0) {
            throw new YyghException(20001, "该用户已经被禁用");
        }
        Map<String, Object> map = new HashMap<>();

        String name = userInfo.getName();

        if (StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();
        }
        if (StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();
        }
        map.put("name", name);
        String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
        map.put("token", token);
        //6.返回用户信息
        return map;
    }

    //根据openid查询用户
    @Override
    public UserInfo selectByOpenId(String openid) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("openid", openid);
        UserInfo userInfo = baseMapper.selectOne(userInfoQueryWrapper);
        return userInfo;
    }

    //根据id查询用户
    @Override
    public UserInfo selectById(Long userId) {
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.eq("id", userId);
        UserInfo userInfo = baseMapper.selectOne(userInfoQueryWrapper);
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        return userInfo;
    }

    //保存用户认证信息
    @Override
    public void saveUserAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        //认证人姓名
        userInfo.setName(userAuthVo.getName());
        //其他认证信息
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //进行信息更新
        baseMapper.updateById(userInfo);
    }

    //用户列表，到查询条件的分页
    @Override
    public Page<UserInfo> selectPage(Integer page, Integer limit, UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> userInfoPage = new Page<>(page, limit);
        //UserInfoQueryVo获取条件值
        String name = userInfoQueryVo.getKeyword(); //用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); //认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); //开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd(); //结束时间
        //对条件值进行非空判断
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)) {
            wrapper.like("name",name).or().eq("phone",name);
        }
        if(!StringUtils.isEmpty(status)) {
            wrapper.eq("status",status);
        }
        if(!StringUtils.isEmpty(authStatus)) {
            wrapper.eq("auth_status",authStatus);
        }
        if(!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time",createTimeEnd);
        }
        Page<UserInfo> Pages = baseMapper.selectPage(userInfoPage, wrapper);
        Pages.getRecords().parallelStream().forEach(item->{
            this.packageUserInfo(item);
        });


        return Pages;
    }

    //编号变成对应值封装
    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证状态编码
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
                //处理用户状态 0 1
                String statusString = userInfo.getStatus().intValue()==0 ?"锁定" : "正常";
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }

    //状态锁定和取消锁定
    @Override
    public void lock(Long userId, Integer status) {
        if(status == 0 || status == 1) {
            UserInfo userInfo = this.getById(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }

    //用户详情
    @Override
    public Map<String, Object> show(Long userId) {
            Map<String,Object> map = new HashMap<>();
            //根据userid查询用户信息
            UserInfo userInfo = this.packageUserInfo(baseMapper.selectById(userId));
            map.put("userInfo",userInfo);
            //根据userid查询就诊人信息
            List<Patient> patientList = patientService.findAllUserId(userId);
            map.put("patientList",patientList);
            return map;
    }

    //认证审批 2通过 -1不通过
    @Override
    public void approval(Long userId, Integer authStatus) {
        if(authStatus==2 || authStatus==-1) {
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }
}
