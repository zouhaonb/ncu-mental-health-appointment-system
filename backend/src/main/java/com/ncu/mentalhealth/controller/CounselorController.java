package com.ncu.mentalhealth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncu.mentalhealth.entity.Counselor;
import com.ncu.mentalhealth.entity.ConsultationRecord;
import com.ncu.mentalhealth.entity.TimeSlot;
import com.ncu.mentalhealth.mapper.CounselorMapper;
import com.ncu.mentalhealth.service.CounselorService;
import com.ncu.mentalhealth.utils.JwtUtils;
import com.ncu.mentalhealth.utils.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/counselor")
@RequiredArgsConstructor
public class CounselorController {
    
    private final CounselorService counselorService;
    private final JwtUtils jwtUtils;
    private final CounselorMapper counselorMapper;

    private Long getUserIdFromToken(String token) {
        return jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));
    }

    @GetMapping("/info")
    public Result getInfo(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        return counselorService.getCounselorInfo(userId);
    }

    @PutMapping("/info")
    public Result updateInfo(@RequestBody Map<String, Object> updateData, @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        updateData.put("userId", userId);
        return counselorService.updateCounselorInfo(updateData);
    }

    @PostMapping("/avatar/upload")
    public Result uploadAvatar(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|jpeg|png|gif|bmp|JPG|JPEG|PNG|GIF|BMP)$")) {
            return Result.error("仅支持 jpg、jpeg、png、gif、bmp 格式的图片");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.error("图片大小不能超过2MB");
        }

        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;
            String filePath = uploadDir + fileName;

            file.transferTo(new File(filePath));

            String avatarUrl = "http://localhost:8080/uploads/avatars/" + fileName;

            Long userId = getUserIdFromToken(token);
            LambdaQueryWrapper<Counselor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Counselor::getUserId, userId);
            Counselor counselor = counselorMapper.selectOne(wrapper);

            if (counselor != null) {
                counselor.setAvatar(avatarUrl);
                counselorMapper.updateById(counselor);
            }

            Map<String, String> data = new HashMap<>();
            data.put("url", avatarUrl);
            return Result.success(data);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @PostMapping("/slot")
    public Result<String> addTimeSlot(@RequestBody TimeSlot slot) {
        return counselorService.addTimeSlot(slot);
    }

    @PostMapping("/slots/batch")
    public Result<String> batchAddTimeSlots(@RequestBody List<TimeSlot> slots) {
        return counselorService.batchAddTimeSlots(slots);
    }

    @GetMapping("/slots/{counselorId}")
    public Result getMyTimeSlots(@PathVariable Long counselorId) {
        return counselorService.getMyTimeSlots(counselorId);
    }

    @DeleteMapping("/slot/{id}")
    public Result<String> deleteTimeSlot(@PathVariable Long id) {
        return counselorService.deleteTimeSlot(id);
    }

    @PutMapping("/slot/{id}")
    public Result<String> updateTimeSlot(@PathVariable Long id, @RequestBody TimeSlot slot) {
        return counselorService.updateTimeSlot(id, slot);
    }

    @PutMapping("/slot/{id}/toggle-status")
    public Result<String> toggleTimeSlotStatus(@PathVariable Long id) {
        return counselorService.toggleTimeSlotStatus(id);
    }

    @GetMapping("/appointments/{counselorId}")
    public Result getAppointments(@PathVariable Long counselorId, 
                                  @RequestParam(required = false) String status) {
        return counselorService.getAppointments(counselorId, status);
    }

    @PutMapping("/appointment/{id}/confirm")
    public Result<String> confirmAppointment(@PathVariable Long id) {
        return counselorService.confirmAppointment(id);
    }

    @Data
    static class RejectRequest {
        private String reason;
    }

    @PutMapping("/appointment/{id}/reject")
    public Result<String> rejectAppointment(@PathVariable Long id, @RequestBody RejectRequest request) {
        return counselorService.rejectAppointment(id, request.getReason());
    }

    @PutMapping("/appointment/{id}/complete")
    public Result<String> completeAppointment(@PathVariable Long id) {
        return counselorService.completeAppointment(id);
    }

    @PostMapping("/record")
    public Result<String> saveConsultationRecord(@RequestBody ConsultationRecord record) {
        return counselorService.saveConsultationRecord(record);
    }

    @GetMapping("/records/{counselorId}")
    public Result getConsultationRecords(@PathVariable Long counselorId) {
        return counselorService.getConsultationRecords(counselorId);
    }

    @GetMapping("/anonymous-messages")
    public Result getAnonymousMessages() {
        return counselorService.getAnonymousMessages();
    }

    @Data
    static class ReplyRequest {
        private String reply;
    }

    @PutMapping("/anonymous-message/{id}/reply")
    public Result<String> replyAnonymousMessage(@PathVariable Long id, 
                                                @RequestBody ReplyRequest request,
                                                @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        return counselorService.replyAnonymousMessage(id, request.getReply(), userId);
    }

    @GetMapping("/referral-suggestions")
    public Result getReferralSuggestions(@RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        // 先获取咨询师ID
        Result counselorInfo = counselorService.getCounselorInfo(userId);
        if (counselorInfo.getCode() != 200) {
            return Result.error("获取咨询师信息失败");
        }
        Long counselorId = ((Map<String, Object>) counselorInfo.getData()).get("id") != null ? 
            Long.valueOf(((Map<String, Object>) counselorInfo.getData()).get("id").toString()) : null;
        if (counselorId == null) {
            return Result.error("咨询师信息不存在");
        }
        return counselorService.getReferralSuggestionsForCounselor(counselorId);
    }

    @PutMapping("/referral-suggestion/{id}/handle")
    public Result<String> handleReferralSuggestion(@PathVariable Long id, 
                                                    @RequestParam String action) {
        return counselorService.handleReferralSuggestion(id, action);
    }

    @PutMapping("/referral-suggestion/{id}/accept")
    public Result<String> acceptReferralWithTimeSlot(@PathVariable Long id,
                                                      @RequestParam Long timeSlotId) {
        return counselorService.acceptReferralWithTimeSlot(id, timeSlotId);
    }
}
