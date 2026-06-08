package com.ncu.mentalhealth.controller;

import com.ncu.mentalhealth.entity.User;
import com.ncu.mentalhealth.service.AuthService;
import com.ncu.mentalhealth.service.TokenBlacklistService;
import com.ncu.mentalhealth.utils.JwtUtils;
import com.ncu.mentalhealth.utils.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtils jwtUtils;

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        return authService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                long expiration = jwtUtils.getExpirationFromToken(token);
                tokenBlacklistService.addToBlacklist(token, expiration);
                return Result.success("退出登录成功");
            } catch (Exception e) {
                return Result.error("退出登录失败");
            }
        }
        return Result.error("Token无效");
    }
}
