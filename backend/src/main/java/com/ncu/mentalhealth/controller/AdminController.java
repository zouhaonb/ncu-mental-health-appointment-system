package com.ncu.mentalhealth.controller;

import com.ncu.mentalhealth.entity.Article;
import com.ncu.mentalhealth.entity.SystemConfig;
import com.ncu.mentalhealth.entity.User;
import com.ncu.mentalhealth.utils.Result;
import com.ncu.mentalhealth.service.AdminService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;

    @GetMapping("/users")
    public Result getUserList(@RequestParam(required = false) String role) {
        return adminService.getUserList(role);
    }

    @GetMapping("/colleges")
    public Result getCollegeList() {
        return adminService.getCollegeList();
    }

    @PostMapping("/user")
    public Result<String> createUser(@RequestBody User user) {
        return adminService.createUser(user);
    }

    @PutMapping("/user")
    public Result<String> updateUser(@RequestBody User user) {
        return adminService.updateUser(user);
    }

    @DeleteMapping("/user/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        return adminService.deleteUser(id);
    }

    @PutMapping("/user/{id}/toggle-status")
    public Result<String> toggleUserStatus(@PathVariable Long id) {
        return adminService.toggleUserStatus(id);
    }

    @GetMapping("/counselors")
    public Result getCounselorList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return adminService.getCounselorList(status, keyword);
    }

    @PutMapping("/counselor/{id}/approve")
    public Result<String> approveCounselor(@PathVariable Long id) {
        return adminService.approveCounselor(id);
    }

    @Data
    static class RejectRequest {
        private String reason;
    }

    @PutMapping("/counselor/{id}/reject")
    public Result<String> rejectCounselor(@PathVariable Long id, @RequestBody RejectRequest request) {
        return adminService.rejectCounselor(id, request.getReason());
    }

    @PutMapping("/counselor")
    public Result<String> updateCounselor(@RequestBody Map<String, Object> counselorData) {
        return adminService.updateCounselor(counselorData);
    }

    @PutMapping("/counselor/{id}/toggle-status")
    public Result<String> toggleCounselorStatus(@PathVariable Long id) {
        return adminService.toggleCounselorStatus(id);
    }

    @PostMapping("/counselor")
    public Result<String> createCounselor(@RequestBody Map<String, Object> counselorData) {
        return adminService.createCounselor(counselorData);
    }

    @GetMapping("/counselors/statistics")
    public Result getCounselorStatistics() {
        return adminService.getCounselorStatistics();
    }

    @GetMapping("/articles")
    public Result getArticleList() {
        return adminService.getArticleList();
    }

    @PostMapping("/article")
    public Result<String> createArticle(@RequestBody Article article) {
        return adminService.createArticle(article);
    }

    @PutMapping("/article")
    public Result<String> updateArticle(@RequestBody Article article) {
        return adminService.updateArticle(article);
    }

    @DeleteMapping("/article/{id}")
    public Result<String> deleteArticle(@PathVariable Long id) {
        return adminService.deleteArticle(id);
    }

    @GetMapping("/configs")
    public Result getConfigList() {
        return adminService.getConfigList();
    }

    @PutMapping("/config")
    public Result<String> updateConfig(@RequestBody SystemConfig config) {
        return adminService.updateConfig(config);
    }

    @GetMapping("/statistics")
    public Result getSystemStatistics() {
        return adminService.getSystemStatistics();
    }

    @GetMapping("/statistics/history")
    public Result getHistoryStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return adminService.getHistoryStatistics(startDate, endDate);
    }
}
