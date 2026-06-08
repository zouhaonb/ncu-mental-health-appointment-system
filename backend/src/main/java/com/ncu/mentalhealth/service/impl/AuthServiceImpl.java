package com.ncu.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncu.mentalhealth.entity.Counselor;
import com.ncu.mentalhealth.entity.User;
import com.ncu.mentalhealth.mapper.CounselorMapper;
import com.ncu.mentalhealth.mapper.UserMapper;
import com.ncu.mentalhealth.service.AuthService;
import com.ncu.mentalhealth.utils.JwtUtils;
import com.ncu.mentalhealth.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final CounselorMapper counselorMapper;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result<Map<String, Object>> login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        data.put("name", user.getName());

        return Result.success(data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> register(User user) {
        log.info("开始注册用户: {}, 角色: {}", user.getUsername(), user.getRole());
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);
        log.info("用户创建成功，ID: {}", user.getId());

        if ("COUNSELOR".equals(user.getRole())) {
            log.info("检测到咨询师角色，开始创建咨询师记录...");
            Counselor counselor = new Counselor();
            counselor.setUserId(user.getId());
            counselor.setIsActive(0);
            counselorMapper.insert(counselor);
            log.info("咨询师记录创建成功，ID: {}", counselor.getId());
        } else {
            log.info("非咨询师角色，跳过创建咨询师记录");
        }

        return Result.success("注册成功");
    }
}
