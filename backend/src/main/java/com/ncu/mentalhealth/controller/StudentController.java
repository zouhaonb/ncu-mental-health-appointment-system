package com.ncu.mentalhealth.controller;

import com.ncu.mentalhealth.entity.AnonymousMessage;
import com.ncu.mentalhealth.entity.Appointment;
import com.ncu.mentalhealth.entity.SelfAssessment;
import com.ncu.mentalhealth.entity.Student;
import com.ncu.mentalhealth.service.StudentService;
import com.ncu.mentalhealth.utils.JwtUtils;
import com.ncu.mentalhealth.utils.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;
    private final JwtUtils jwtUtils;

    private Long getUserIdFromToken(String token) {
        return jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        return studentService.getStudentInfo(userId);
    }

    @PutMapping("/info")
    public Result<String> updateInfo(@RequestBody Student student) {
        return studentService.updateStudentInfo(student);
    }

    @PostMapping("/assessment")
    public Result<String> submitAssessment(@RequestBody SelfAssessment assessment) {
        return studentService.submitAssessment(assessment);
    }

    @GetMapping("/assessment/history/{userId}")
    public Result<List<SelfAssessment>> getAssessmentHistory(@PathVariable Long userId) {
        return studentService.getAssessmentHistory(userId);
    }

    @GetMapping("/counselors")
    public Result getCounselorList() {
        return studentService.getCounselorList();
    }

    @GetMapping("/slots/{counselorId}")
    public Result getAvailableSlots(@PathVariable Long counselorId) {
        return studentService.getAvailableSlots(counselorId);
    }

    @PostMapping("/appointment")
    public Result<String> createAppointment(@RequestBody Appointment appointment) {
        return studentService.createAppointment(appointment);
    }

    @GetMapping("/appointments/{studentId}")
    public Result getMyAppointments(@PathVariable Long studentId) {
        return studentService.getMyAppointments(studentId);
    }

    @Data
    static class CancelRequest {
        private String cancelReason;
    }

    @PutMapping("/appointment/{id}/cancel")
    public Result<String> cancelAppointment(@PathVariable Long id, @RequestBody CancelRequest request) {
        return studentService.cancelAppointment(id, request.getCancelReason());
    }

    @PostMapping("/anonymous-message")
    public Result<String> sendAnonymousMessage(@RequestBody AnonymousMessage message) {
        return studentService.sendAnonymousMessage(message);
    }

    @GetMapping("/anonymous-messages/{studentId}")
    public Result getMyAnonymousMessages(@PathVariable Long studentId) {
        return studentService.getMyAnonymousMessages(studentId);
    }

    @Data
    static class PasswordChangeRequest {
        private String oldPassword;
        private String newPassword;
    }

    @PutMapping("/password")
    public Result<String> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody PasswordChangeRequest request) {
        Long userId = getUserIdFromToken(token);
        return studentService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
    }
}
