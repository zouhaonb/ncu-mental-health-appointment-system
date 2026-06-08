package com.ncu.mentalhealth.service;

import com.ncu.mentalhealth.entity.User;
import com.ncu.mentalhealth.utils.Result;

import java.util.Map;

public interface AuthService {

    Result<Map<String, Object>> login(String username, String password);

    Result<String> register(User user);
}
