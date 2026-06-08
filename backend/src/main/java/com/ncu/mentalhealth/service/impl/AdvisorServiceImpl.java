package com.ncu.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncu.mentalhealth.entity.*;
import com.ncu.mentalhealth.mapper.*;
import com.ncu.mentalhealth.service.AdvisorService;
import com.ncu.mentalhealth.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvisorServiceImpl implements AdvisorService {
    
    private final AdvisorMapper advisorMapper;
    private final RiskAlertMapper riskAlertMapper;
    private final KeyConcernStudentMapper keyConcernStudentMapper;
    private final ReferralSuggestionMapper referralSuggestionMapper;
    private final FollowUpTaskMapper followUpTaskMapper;
    private final StudentMapper studentMapper;
    private final UserMapper userMapper;
    private final CounselorMapper counselorMapper;
    private final ConsultationRecordMapper consultationRecordMapper;
    private final AppointmentMapper appointmentMapper;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    public Result<Advisor> getAdvisorInfo(Long userId) {
        LambdaQueryWrapper<Advisor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Advisor::getUserId, userId);
        Advisor advisor = advisorMapper.selectOne(wrapper);
        return advisor != null ? Result.success(advisor) : Result.error("辅导员信息不存在");
    }

    @Override
    public Result<List<Map<String, Object>>> getRiskAlerts(Long advisorId) {
        LambdaQueryWrapper<Advisor> advisorWrapper = new LambdaQueryWrapper<>();
        advisorWrapper.eq(Advisor::getUserId, advisorId);
        Advisor advisor = advisorMapper.selectOne(advisorWrapper);
        if (advisor == null) {
            return Result.error("辅导员信息不存在");
        }
        
        LambdaQueryWrapper<RiskAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskAlert::getAdvisorId, advisor.getId())
               .orderByDesc(RiskAlert::getCreatedAt);
        List<RiskAlert> alerts = riskAlertMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = alerts.stream().map(alert -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", alert.getId());
            map.put("studentId", alert.getStudentId());
            map.put("advisorId", alert.getAdvisorId());
            map.put("alertSource", alert.getAlertSource());
            map.put("alertType", alert.getAlertType());
            map.put("riskLevel", alert.getRiskLevel());
            map.put("isRead", alert.getIsRead());
            map.put("createdAt", alert.getCreatedAt());
            
            Student student = studentMapper.selectById(alert.getStudentId());
            if (student != null) {
                User user = userMapper.selectById(student.getUserId());
                map.put("studentName", user != null ? user.getName() : "未知");
            } else {
                map.put("studentName", "未知");
            }
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    public Result<Long> getUnreadAlertCount(Long advisorId) {
        LambdaQueryWrapper<Advisor> advisorWrapper = new LambdaQueryWrapper<>();
        advisorWrapper.eq(Advisor::getUserId, advisorId);
        Advisor advisor = advisorMapper.selectOne(advisorWrapper);
        if (advisor == null) {
            return Result.error("辅导员信息不存在");
        }
        
        LambdaQueryWrapper<RiskAlert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RiskAlert::getAdvisorId, advisor.getId())
               .eq(RiskAlert::getIsRead, 0);
        return Result.success(riskAlertMapper.selectCount(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> markAlertAsRead(Long alertId) {
        RiskAlert alert = riskAlertMapper.selectById(alertId);
        if (alert == null) {
            return Result.error("预警不存在");
        }
        
        alert.setIsRead(1);
        int rows = riskAlertMapper.updateById(alert);
        return rows > 0 ? Result.success("已标记为已读") : Result.error("操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addKeyConcernStudent(KeyConcernStudent concern) {
        LambdaQueryWrapper<KeyConcernStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KeyConcernStudent::getAdvisorId, concern.getAdvisorId())
               .eq(KeyConcernStudent::getStudentId, concern.getStudentId());
        
        if (keyConcernStudentMapper.selectCount(wrapper) > 0) {
            return Result.error("该学生已在重点关注列表中");
        }
        
        int rows = keyConcernStudentMapper.insert(concern);
        if (rows > 0) {
            // 自动创建回访任务
            FollowUpTask task = new FollowUpTask();
            task.setStudentId(concern.getStudentId());
            task.setAdvisorId(concern.getAdvisorId());
            task.setStatus("PENDING");
            task.setFeedback("学生已加入重点关注列表，请及时跟进回访。备注：" + (concern.getNotes() != null ? concern.getNotes() : "无"));
            followUpTaskMapper.insert(task);
            log.info("已自动创建回访任务, studentId: {}, taskId: {}", concern.getStudentId(), task.getId());
            return Result.success("添加成功，已自动创建回访任务");
        } else {
            return Result.error("添加失败");
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getKeyConcernStudents(Long advisorId) {
        LambdaQueryWrapper<Advisor> advisorWrapper = new LambdaQueryWrapper<>();
        advisorWrapper.eq(Advisor::getUserId, advisorId);
        Advisor advisor = advisorMapper.selectOne(advisorWrapper);
        if (advisor == null) {
            return Result.error("辅导员信息不存在");
        }
        
        List<KeyConcernStudent> concerns = keyConcernStudentMapper.selectList(
            new LambdaQueryWrapper<KeyConcernStudent>()
                .eq(KeyConcernStudent::getAdvisorId, advisor.getId())
        );
        
        List<Map<String, Object>> result = concerns.stream().map(concern -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("concern", concern);
            
            Student student = studentMapper.selectById(concern.getStudentId());
            map.put("student", student);
            
            if (student != null) {
                User user = userMapper.selectById(student.getUserId());
                map.put("user", user);
            }
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> removeKeyConcernStudent(Long concernId) {
        // 1. 先查询重点关注记录，获取学生ID和辅导员ID
        KeyConcernStudent concern = keyConcernStudentMapper.selectById(concernId);
        if (concern == null) {
            return Result.error("重点关注记录不存在");
        }
        
        // 2. 删除该学生在该辅导员下的待处理回访任务
        LambdaQueryWrapper<FollowUpTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(FollowUpTask::getStudentId, concern.getStudentId())
                   .eq(FollowUpTask::getAdvisorId, concern.getAdvisorId())
                   .eq(FollowUpTask::getStatus, "PENDING");
        
        followUpTaskMapper.delete(taskWrapper);
        log.info("移除重点关注时，已删除关联的待处理回访任务, studentId: {}, advisorId: {}", 
                 concern.getStudentId(), concern.getAdvisorId());
        
        // 3. 删除重点关注记录
        int rows = keyConcernStudentMapper.deleteById(concernId);
        return rows > 0 ? Result.success("移除成功，已清理关联回访任务") : Result.error("移除失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createReferralSuggestion(ReferralSuggestion suggestion) {
        LambdaQueryWrapper<FollowUpTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowUpTask::getStudentId, suggestion.getStudentId())
               .eq(FollowUpTask::getStatus, "COMPLETED");
        
        long completedCount = followUpTaskMapper.selectCount(wrapper);
        
        if (completedCount == 0) {
            return Result.error("无法创建转介建议：该学生还没有完成回访任务，请先进行回访");
        }
        
        LambdaQueryWrapper<ReferralSuggestion> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(ReferralSuggestion::getStudentId, suggestion.getStudentId())
                      .eq(ReferralSuggestion::getAdvisorId, suggestion.getAdvisorId())
                      .eq(ReferralSuggestion::getHandleStatus, "PENDING");
        
        long pendingCount = referralSuggestionMapper.selectCount(pendingWrapper);
        
        if (pendingCount > 0) {
            return Result.error("该学生已有待处理的转介建议，请等待咨询师处理后再提交新的建议");
        }
        
        suggestion.setHandleStatus("PENDING");
        int rows = referralSuggestionMapper.insert(suggestion);
        return rows > 0 ? Result.success("转介建议已提交") : Result.error("提交失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelReferralSuggestion(Long suggestionId) {
        log.info("取消转介建议, suggestionId: {}", suggestionId);
        
        ReferralSuggestion suggestion = referralSuggestionMapper.selectById(suggestionId);
        if (suggestion == null) {
            log.error("转介建议不存在, suggestionId: {}", suggestionId);
            return Result.error("转介建议不存在");
        }
        
        if (!"PENDING".equals(suggestion.getHandleStatus())) {
            log.error("转介建议状态不是待处理, status: {}", suggestion.getHandleStatus());
            return Result.error("只能取消待处理的转介建议");
        }
        
        suggestion.setHandleStatus("CANCELLED");
        int rows = referralSuggestionMapper.updateById(suggestion);
        
        if (rows > 0) {
            log.info("转介建议已取消, suggestionId: {}", suggestionId);
            return Result.success("已取消转介建议");
        } else {
            log.error("取消转介建议失败, suggestionId: {}", suggestionId);
            return Result.error("取消失败");
        }
    }

    @Override
    public Result<List<Map<String, Object>>> getReferralSuggestions(Long advisorId) {
        LambdaQueryWrapper<Advisor> advisorWrapper = new LambdaQueryWrapper<>();
        advisorWrapper.eq(Advisor::getUserId, advisorId);
        Advisor advisor = advisorMapper.selectOne(advisorWrapper);
        if (advisor == null) {
            return Result.error("辅导员信息不存在");
        }
        
        List<ReferralSuggestion> suggestions = referralSuggestionMapper.selectList(
            new LambdaQueryWrapper<ReferralSuggestion>()
                .eq(ReferralSuggestion::getAdvisorId, advisor.getId())
                .orderByDesc(ReferralSuggestion::getCreatedAt)
        );
        
        List<Map<String, Object>> result = suggestions.stream().map(suggestion -> {
            Map<String, Object> map = new java.util.HashMap<>();
            
            Student student = studentMapper.selectById(suggestion.getStudentId());
            if (student != null) {
                User user = userMapper.selectById(student.getUserId());
                map.put("student", student);
                map.put("user", user);
            } else {
                map.put("student", null);
                map.put("user", null);
            }
            
            map.put("id", suggestion.getId());
            map.put("advisorId", suggestion.getAdvisorId());
            map.put("studentId", suggestion.getStudentId());
            map.put("assignedCounselorId", suggestion.getAssignedCounselorId());
            map.put("reason", suggestion.getReason());
            map.put("handleStatus", suggestion.getHandleStatus());
            map.put("createdAt", suggestion.getCreatedAt());
            map.put("updatedAt", suggestion.getUpdatedAt());
            
            if (suggestion.getAssignedCounselorId() != null) {
                Counselor counselor = counselorMapper.selectById(suggestion.getAssignedCounselorId());
                if (counselor != null) {
                    User counselorUser = userMapper.selectById(counselor.getUserId());
                    map.put("counselor", counselor);
                    map.put("counselorUser", counselorUser);
                } else {
                    map.put("counselor", null);
                    map.put("counselorUser", null);
                }
            } else {
                map.put("counselor", null);
                map.put("counselorUser", null);
            }
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createFollowUpTask(FollowUpTask task) {
        // 去重检查：同一辅导员、同一学生、状态为待处理
        LambdaQueryWrapper<FollowUpTask> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FollowUpTask::getAdvisorId, task.getAdvisorId())
                   .eq(FollowUpTask::getStudentId, task.getStudentId())
                   .eq(FollowUpTask::getStatus, "PENDING");
        
        if (followUpTaskMapper.selectCount(checkWrapper) > 0) {
            return Result.error("该学生已有待处理的回访任务，请勿重复创建");
        }
        
        task.setStatus("PENDING");
        int rows = followUpTaskMapper.insert(task);
        return rows > 0 ? Result.success("回访任务已创建") : Result.error("创建失败");
    }

    @Override
    public Result<List<Map<String, Object>>> getFollowUpTasks(Long advisorId) {
        LambdaQueryWrapper<Advisor> advisorWrapper = new LambdaQueryWrapper<>();
        advisorWrapper.eq(Advisor::getUserId, advisorId);
        Advisor advisor = advisorMapper.selectOne(advisorWrapper);
        if (advisor == null) {
            return Result.error("辅导员信息不存在");
        }
        
        List<FollowUpTask> tasks = followUpTaskMapper.selectList(
            new LambdaQueryWrapper<FollowUpTask>()
                .eq(FollowUpTask::getAdvisorId, advisor.getId())
                .orderByDesc(FollowUpTask::getCreatedAt)
        );
        
        List<Map<String, Object>> result = tasks.stream().map(task -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("task", task);
            
            Student student = studentMapper.selectById(task.getStudentId());
            map.put("student", student);
            
            if (student != null) {
                User user = userMapper.selectById(student.getUserId());
                map.put("user", user);
            }
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> completeFollowUpTask(Long taskId, String feedback) {
        FollowUpTask task = followUpTaskMapper.selectById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        
        task.setStatus("COMPLETED");
        task.setFeedback(feedback);
        int rows = followUpTaskMapper.updateById(task);
        return rows > 0 ? Result.success("任务已完成") : Result.error("操作失败");
    }

    @Override
    public Result<List<Map<String, Object>>> getStudentConsultations(Long studentId) {
        // 查询该学生的所有咨询记录
        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getStudentId, studentId)
               .orderByDesc(ConsultationRecord::getCreatedAt);
        
        List<ConsultationRecord> records = consultationRecordMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = records.stream().map(record -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", record.getId());
            map.put("appointmentId", record.getAppointmentId());
            map.put("studentId", record.getStudentId());
            map.put("counselorId", record.getCounselorId());
            map.put("content", record.getContent());
            map.put("assessment", record.getAssessment());
            map.put("riskLevel", record.getRiskLevel());
            map.put("isClosed", record.getIsClosed());
            map.put("createdAt", record.getCreatedAt());
            map.put("updatedAt", record.getUpdatedAt());
            
            // 查询咨询师信息
            Counselor counselor = counselorMapper.selectById(record.getCounselorId());
            if (counselor != null) {
                User user = userMapper.selectById(counselor.getUserId());
                map.put("counselorName", user != null ? user.getName() : "未知");
                map.put("counselorTitle", counselor.getTitle());
            } else {
                map.put("counselorName", "未知");
                map.put("counselorTitle", "");
            }
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    public Result<List<Map<String, Object>>> getStudentAppointments(Long studentId) {
        // 查询该学生的所有预约记录
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getStudentId, studentId)
               .orderByDesc(Appointment::getCreatedAt);
        
        List<Appointment> appointments = appointmentMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = appointments.stream().map(appointment -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", appointment.getId());
            map.put("studentId", appointment.getStudentId());
            map.put("counselorId", appointment.getCounselorId());
            map.put("timeSlotId", appointment.getTimeSlotId());
            map.put("status", appointment.getStatus());
            map.put("reason", appointment.getReason());
            map.put("cancelReason", appointment.getCancelReason());
            map.put("createdAt", appointment.getCreatedAt());
            map.put("updatedAt", appointment.getUpdatedAt());
            
            // 查询咨询师信息
            Counselor counselor = counselorMapper.selectById(appointment.getCounselorId());
            if (counselor != null) {
                User user = userMapper.selectById(counselor.getUserId());
                map.put("counselorName", user != null ? user.getName() : "未知");
                map.put("counselorTitle", counselor.getTitle());
            } else {
                map.put("counselorName", "未知");
                map.put("counselorTitle", "");
            }
            
            // 查询时间段信息
            TimeSlot timeSlot = timeSlotMapper.selectById(appointment.getTimeSlotId());
            if (timeSlot != null) {
                map.put("date", timeSlot.getDate());
                map.put("startTime", timeSlot.getStartTime());
                map.put("endTime", timeSlot.getEndTime());
            }
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getCollegeStatistics(Long advisorId) {
        LambdaQueryWrapper<Advisor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Advisor::getUserId, advisorId);
        Advisor advisor = advisorMapper.selectOne(wrapper);
        if (advisor == null) {
            return Result.error("辅导员信息不存在");
        }
        
        Long totalStudents = studentMapper.selectCount(
            new LambdaQueryWrapper<Student>().eq(Student::getCollege, advisor.getCollege())
        );
        
        Long totalAlerts = riskAlertMapper.selectCount(
            new LambdaQueryWrapper<RiskAlert>().eq(RiskAlert::getAdvisorId, advisor.getId())
        );
        
        Long unreadAlerts = riskAlertMapper.selectCount(
            new LambdaQueryWrapper<RiskAlert>()
                .eq(RiskAlert::getAdvisorId, advisor.getId())
                .eq(RiskAlert::getIsRead, 0)
        );
        
        Long concernCount = keyConcernStudentMapper.selectCount(
            new LambdaQueryWrapper<KeyConcernStudent>().eq(KeyConcernStudent::getAdvisorId, advisor.getId())
        );
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("college", advisor.getCollege());
        stats.put("totalStudents", totalStudents);
        stats.put("totalAlerts", totalAlerts);
        stats.put("unreadAlerts", unreadAlerts);
        stats.put("concernCount", concernCount);
        
        return Result.success(stats);
    }
}
