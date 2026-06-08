package com.ncu.mentalhealth.service;

import com.ncu.mentalhealth.entity.*;
import com.ncu.mentalhealth.utils.Result;

import java.util.List;
import java.util.Map;

public interface StudentService {

    Result<Map<String, Object>> getStudentInfo(Long userId);

    Result<String> updateStudentInfo(Student student);

    Result<String> submitAssessment(SelfAssessment assessment);

    Result<List<SelfAssessment>> getAssessmentHistory(Long userId);

    Result<List<Map<String, Object>>> getCounselorList();

    Result<List<TimeSlot>> getAvailableSlots(Long counselorId);

    Result<String> createAppointment(Appointment appointment);

    Result<List<Map<String, Object>>> getMyAppointments(Long studentId);

    Result<String> cancelAppointment(Long appointmentId, String cancelReason);

    Result<String> sendAnonymousMessage(AnonymousMessage message);

    Result<List<AnonymousMessage>> getMyAnonymousMessages(Long studentId);

    Result<String> changePassword(Long userId, String oldPassword, String newPassword);
}
