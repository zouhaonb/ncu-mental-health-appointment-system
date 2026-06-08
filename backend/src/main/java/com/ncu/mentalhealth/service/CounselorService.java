package com.ncu.mentalhealth.service;

import com.ncu.mentalhealth.entity.AnonymousMessage;
import com.ncu.mentalhealth.entity.ConsultationRecord;
import com.ncu.mentalhealth.entity.TimeSlot;
import com.ncu.mentalhealth.utils.Result;

import java.util.List;
import java.util.Map;

public interface CounselorService {
    Result getCounselorInfo(Long userId);
    Result updateCounselorInfo(Map<String, Object> updateData);
    Result<String> addTimeSlot(TimeSlot slot);
    Result<List<TimeSlot>> getMyTimeSlots(Long counselorId);
    Result<String> deleteTimeSlot(Long slotId);

    Result<String> updateTimeSlot(Long slotId, TimeSlot slot);

    Result<String> toggleTimeSlotStatus(Long slotId);

    Result<String> batchAddTimeSlots(List<TimeSlot> slots);
    Result getAppointments(Long counselorId, String status);
    Result<String> confirmAppointment(Long id);
    Result<String> rejectAppointment(Long id, String reason);
    Result<String> completeAppointment(Long id);
    Result<String> saveConsultationRecord(ConsultationRecord record);
    Result getConsultationRecords(Long counselorId);
    Result<List<AnonymousMessage>> getAnonymousMessages();
    Result<String> replyAnonymousMessage(Long id, String reply, Long userId);
    
    // 转介建议相关接口
    Result<List<Map<String, Object>>> getReferralSuggestionsForCounselor(Long counselorId);
    Result<String> handleReferralSuggestion(Long suggestionId, String action);
    Result<String> acceptReferralWithTimeSlot(Long suggestionId, Long timeSlotId);
}
