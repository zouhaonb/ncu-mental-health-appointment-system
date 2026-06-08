package com.ncu.mentalhealth.controller;

import com.ncu.mentalhealth.entity.FollowUpTask;
import com.ncu.mentalhealth.entity.KeyConcernStudent;
import com.ncu.mentalhealth.entity.ReferralSuggestion;
import com.ncu.mentalhealth.service.AdvisorService;
import com.ncu.mentalhealth.utils.JwtUtils;
import com.ncu.mentalhealth.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advisor")
@RequiredArgsConstructor
public class AdvisorController {
    
    private final AdvisorService advisorService;
    private final JwtUtils jwtUtils;

    private Long getUserIdFromToken(String token) {
        return jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
    }

    @GetMapping("/info")
    public Result getInfo(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        return advisorService.getAdvisorInfo(userId);
    }

    @GetMapping("/alerts/{advisorId}")
    public Result getRiskAlerts(@PathVariable Long advisorId) {
        return advisorService.getRiskAlerts(advisorId);
    }

    @GetMapping("/alerts/unread/{advisorId}")
    public Result getUnreadAlertCount(@PathVariable Long advisorId) {
        return advisorService.getUnreadAlertCount(advisorId);
    }

    @PutMapping("/alert/{id}/read")
    public Result<String> markAlertAsRead(@PathVariable Long id) {
        return advisorService.markAlertAsRead(id);
    }

    @PostMapping("/concern")
    public Result<String> addKeyConcernStudent(@RequestBody KeyConcernStudent concern) {
        return advisorService.addKeyConcernStudent(concern);
    }

    @GetMapping("/concerns/{advisorId}")
    public Result getKeyConcernStudents(@PathVariable Long advisorId) {
        return advisorService.getKeyConcernStudents(advisorId);
    }

    @DeleteMapping("/concern/{id}")
    public Result<String> removeKeyConcernStudent(@PathVariable Long id) {
        return advisorService.removeKeyConcernStudent(id);
    }

    @PostMapping("/referral")
    public Result<String> createReferralSuggestion(@RequestBody ReferralSuggestion suggestion) {
        return advisorService.createReferralSuggestion(suggestion);
    }

    @PutMapping("/referral/{id}/cancel")
    public Result<String> cancelReferralSuggestion(@PathVariable Long id) {
        return advisorService.cancelReferralSuggestion(id);
    }

    @GetMapping("/referrals/{advisorId}")
    public Result getReferralSuggestions(@PathVariable Long advisorId) {
        return advisorService.getReferralSuggestions(advisorId);
    }

    @PostMapping("/follow-up")
    public Result<String> createFollowUpTask(@RequestBody FollowUpTask task) {
        return advisorService.createFollowUpTask(task);
    }

    @GetMapping("/follow-ups/{advisorId}")
    public Result getFollowUpTasks(@PathVariable Long advisorId) {
        return advisorService.getFollowUpTasks(advisorId);
    }

    @PutMapping("/follow-up/{id}/complete")
    public Result<String> completeFollowUpTask(@PathVariable Long id, @RequestParam String feedback) {
        return advisorService.completeFollowUpTask(id, feedback);
    }

    @GetMapping("/student/consultations/{studentId}")
    public Result getStudentConsultations(@PathVariable Long studentId) {
        return advisorService.getStudentConsultations(studentId);
    }

    @GetMapping("/student/appointments/{studentId}")
    public Result getStudentAppointments(@PathVariable Long studentId) {
        return advisorService.getStudentAppointments(studentId);
    }

    @GetMapping("/statistics/{advisorId}")
    public Result getCollegeStatistics(@PathVariable Long advisorId) {
        return advisorService.getCollegeStatistics(advisorId);
    }
}
