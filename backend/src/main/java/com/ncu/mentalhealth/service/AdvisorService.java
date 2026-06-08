package com.ncu.mentalhealth.service;

import com.ncu.mentalhealth.entity.*;
import com.ncu.mentalhealth.utils.Result;

import java.util.List;
import java.util.Map;

public interface AdvisorService {
    Result<Advisor> getAdvisorInfo(Long userId);
    
    Result<List<Map<String, Object>>> getRiskAlerts(Long advisorId);
    
    Result<Long> getUnreadAlertCount(Long advisorId);
    
    Result<String> markAlertAsRead(Long alertId);
    
    Result<String> addKeyConcernStudent(KeyConcernStudent concern);
    
    Result<List<Map<String, Object>>> getKeyConcernStudents(Long advisorId);
    
    Result<String> removeKeyConcernStudent(Long concernId);
    
    Result<String> createReferralSuggestion(ReferralSuggestion suggestion);
    
    Result<String> cancelReferralSuggestion(Long suggestionId);
    
    Result<List<Map<String, Object>>> getReferralSuggestions(Long advisorId);
    
    Result<String> createFollowUpTask(FollowUpTask task);
    
    Result<List<Map<String, Object>>> getFollowUpTasks(Long advisorId);
    
    Result<String> completeFollowUpTask(Long taskId, String feedback);
    
    Result<Map<String, Object>> getCollegeStatistics(Long advisorId);
    
    Result<List<Map<String, Object>>> getStudentConsultations(Long studentId);
    
    Result<List<Map<String, Object>>> getStudentAppointments(Long studentId);
}
