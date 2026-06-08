package com.ncu.mentalhealth.service;

import com.ncu.mentalhealth.entity.Article;
import com.ncu.mentalhealth.entity.SystemConfig;
import com.ncu.mentalhealth.entity.User;
import com.ncu.mentalhealth.utils.Result;

import java.util.List;
import java.util.Map;

public interface AdminService {
    
    Result<List<User>> getUserList(String role);

    Result<List<String>> getCollegeList();

    Result<String> createUser(User user);

    Result<String> updateUser(User user);

    Result<String> deleteUser(Long userId);

    Result<String> toggleUserStatus(Long userId);

    Result<List<Map<String, Object>>> getCounselorList(String status, String keyword);

    Result<String> approveCounselor(Long counselorId);

    Result<String> rejectCounselor(Long counselorId, String reason);

    Result<String> updateCounselor(Map<String, Object> counselorData);

    Result<String> toggleCounselorStatus(Long counselorId);

    Result<String> createCounselor(Map<String, Object> counselorData);

    Result<Map<String, Long>> getCounselorStatistics();

    Result<List<Article>> getArticleList();

    Result<String> createArticle(Article article);

    Result<String> updateArticle(Article article);

    Result<String> deleteArticle(Long articleId);

    Result<List<SystemConfig>> getConfigList();

    Result<String> updateConfig(SystemConfig config);

    Result<Map<String, Object>> getSystemStatistics();
    
    Result<Map<String, Object>> getHistoryStatistics(String startDate, String endDate);

}
