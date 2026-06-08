package com.ncu.mentalhealth.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncu.mentalhealth.entity.*;
import com.ncu.mentalhealth.mapper.*;
import com.ncu.mentalhealth.service.StudentService;
import com.ncu.mentalhealth.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    
    private final StudentMapper studentMapper;
    private final CounselorMapper counselorMapper;
    private final UserMapper userMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final AppointmentMapper appointmentMapper;
    private final SelfAssessmentMapper selfAssessmentMapper;
    private final RiskAlertMapper riskAlertMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final AnonymousMessageMapper anonymousMessageMapper;
    
    @Override
    public Result<Map<String, Object>> getStudentInfo(Long userId) {
        log.info("查询学生信息, userId: {}", userId);

        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(wrapper);

        if (student == null) {
            log.warn("学生信息不存在, userId: {}", userId);
            return Result.error("学生信息不存在");
        }

        log.info("查询到学生信息: {}", student);

        User user = userMapper.selectById(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", student.getId());
        result.put("userId", student.getUserId());
        result.put("username", user != null ? user.getUsername() : "");
        result.put("name", user != null ? user.getName() : "");
        result.put("studentNo", student.getStudentNo());
        result.put("college", student.getCollege());
        result.put("className", student.getClassName());
        result.put("grade", student.getGrade());
        result.put("advisorId", student.getAdvisorId());

        if (user != null) {
            result.put("phone", user.getPhone());
            result.put("email", user.getEmail());
        }

        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateStudentInfo(Student student) {
        int rows = studentMapper.updateById(student);
        return rows > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> submitAssessment(SelfAssessment assessment) {
        log.info("提交测评, studentId: {}, scaleType: {}, totalScore: {}", 
                 assessment.getStudentId(), assessment.getScaleType(), assessment.getTotalScore());
        
        // 根据系统配置动态判断风险等级
        String riskLevel = determineRiskLevel(assessment.getScaleType(), assessment.getTotalScore());
        assessment.setRiskLevel(riskLevel);
        
        log.info("根据配置计算风险等级: {}", riskLevel);
        
        int rows = selfAssessmentMapper.insert(assessment);
        if (rows <= 0) {
            return Result.error("提交失败");
        }
        
        // 中高风险自动创建预警
        if ("MEDIUM".equals(riskLevel) || "HIGH".equals(riskLevel)) {
            Student student = studentMapper.selectById(assessment.getStudentId());
            if (student != null && student.getAdvisorId() != null) {
                LambdaQueryWrapper<RiskAlert> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(RiskAlert::getStudentId, assessment.getStudentId())
                           .eq(RiskAlert::getAlertSource, "SELF_ASSESSMENT")
                           .eq(RiskAlert::getAlertType, assessment.getScaleType());
                
                if (riskAlertMapper.selectCount(checkWrapper) == 0) {
                    RiskAlert alert = new RiskAlert();
                    alert.setStudentId(assessment.getStudentId());
                    alert.setAdvisorId(student.getAdvisorId());
                    alert.setAlertSource("SELF_ASSESSMENT");
                    alert.setAlertType(assessment.getScaleType());
                    alert.setRiskLevel(riskLevel);
                    alert.setIsRead(0);
                    riskAlertMapper.insert(alert);
                    log.info("已创建风险预警, studentId: {}, alertType: {}, riskLevel: {}", 
                             assessment.getStudentId(), assessment.getScaleType(), riskLevel);
                }
            }
        }
        
        return Result.success("提交成功");
    }
    
    /**
     * 根据系统配置动态判断风险等级
     * @param scaleType 量表类型 DEPRESSION/ANXIETY
     * @param standardScore 标准分
     * @return 风险等级 NORMAL/LOW/MEDIUM/HIGH
     */
    private String determineRiskLevel(String scaleType, Integer standardScore) {
        if (standardScore == null) {
            return "NORMAL";
        }
        
        // 读取系统配置
        int sdsThreshold = 42; // SDS抑郁量表高风险阈值，默认值
        int sasThreshold = 40; // SAS焦虑量表高风险阈值，默认值
        
        try {
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(SystemConfig::getConfigKey, "sds_threshold", "sas_threshold");
            List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
            
            for (SystemConfig config : configs) {
                if ("sds_threshold".equals(config.getConfigKey())) {
                    sdsThreshold = Integer.parseInt(config.getConfigValue());
                    log.info("加载SDS高风险阈值: {}", sdsThreshold);
                } else if ("sas_threshold".equals(config.getConfigKey())) {
                    sasThreshold = Integer.parseInt(config.getConfigValue());
                    log.info("加载SAS高风险阈值: {}", sasThreshold);
                }
            }
        } catch (Exception e) {
            log.warn("读取系统配置失败，使用默认值 (SDS:42, SAS:40)", e);
        }
        
        if ("DEPRESSION".equals(scaleType)) {
            // SDS抑郁量表风险等级（标准分）
            // 正常<33，轻度33-37，中度38-41，重度≥42
            if (standardScore < 33) return "NORMAL";
            if (standardScore < 38) return "LOW";
            if (standardScore < sdsThreshold) return "MEDIUM";
            return "HIGH";
        } else {
            // SAS焦虑量表风险等级（标准分）
            // 正常<31，轻度31-35，中度36-39，重度≥40
            if (standardScore < 31) return "NORMAL";
            if (standardScore < 36) return "LOW";
            if (standardScore < sasThreshold) return "MEDIUM";
            return "HIGH";
        }
    }

    @Override
    public Result<List<SelfAssessment>> getAssessmentHistory(Long userId) {
        log.info("查询学生测评历史, userId: {}", userId);
        
        // 先根据 userId 查询 student 表获取 studentId
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(studentWrapper);
        
        if (student == null) {
            log.warn("未找到学生信息, userId: {}", userId);
            return Result.error("未找到学生信息");
        }
        
        Long studentId = student.getId();
        log.info("找到学生信息, studentId: {}", studentId);
        
        // 根据 studentId 查询测评记录
        LambdaQueryWrapper<SelfAssessment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SelfAssessment::getStudentId, studentId)
               .orderByDesc(SelfAssessment::getCreatedAt);
        List<SelfAssessment> assessments = selfAssessmentMapper.selectList(wrapper);
        
        log.info("查询到 {} 条测评记录", assessments.size());
        return Result.success(assessments);
    }

    @Override
    public Result<List<Map<String, Object>>> getCounselorList() {
        log.info("查询咨询师列表");
        LambdaQueryWrapper<Counselor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Counselor::getIsActive, 1);
        List<Counselor> counselors = counselorMapper.selectList(wrapper);

        List<Map<String, Object>> result = counselors.stream().map(counselor -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", counselor.getId());
            map.put("userId", counselor.getUserId());
            map.put("title", counselor.getTitle());
            map.put("specialization", counselor.getSpecialization());
            map.put("description", counselor.getDescription());
            map.put("avatar", counselor.getAvatar());

            User user = userMapper.selectById(counselor.getUserId());
            map.put("name", user != null ? user.getName() : "未知");
            map.put("phone", user != null ? user.getPhone() : "");
            map.put("email", user != null ? user.getEmail() : "");

            return map;
        }).toList();

        log.info("查询到 {} 个咨询师", result.size());
        return Result.success(result);
    }

    @Override
    public Result<List<TimeSlot>> getAvailableSlots(Long counselorId) {
        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeSlot::getCounselorId, counselorId)
               .eq(TimeSlot::getStatus, "ACTIVE")
               .ge(TimeSlot::getDate, LocalDate.now())
               .orderByAsc(TimeSlot::getDate, TimeSlot::getStartTime);

        List<TimeSlot> allSlots = timeSlotMapper.selectList(wrapper);

        List<TimeSlot> availableSlots = allSlots.stream()
                .filter(slot -> slot.getBookedCount() < slot.getMaxAppointments())
                .toList();

        return Result.success(availableSlots);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> createAppointment(Appointment appointment) {
        log.info("学生预约, appointment: {}", appointment);
        
        // 从当前登录用户获取 userId
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("用户未登录");
            return Result.error("用户未登录");
        }
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("预约用户 userId: {}", userId);
        
        // 根据 userId 查询 student 表获取 studentId
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(studentWrapper);
        
        if (student == null) {
            log.error("未找到学生信息, userId: {}", userId);
            return Result.error("未找到学生信息，请先完善学生信息");
        }
        
        Long studentId = student.getId();
        log.info("找到学生信息, studentId: {}, studentNo: {}", studentId, student.getStudentNo());
        
        // 使用正确的 studentId
        appointment.setStudentId(studentId);
        
        TimeSlot slot = timeSlotMapper.selectById(appointment.getTimeSlotId());
        if (slot == null) {
            return Result.error("时段不存在");
        }

        // 新增：检查时段是否已过期
        if (slot.getDate().isBefore(LocalDate.now())) {
            return Result.error("该时段已过期，无法预约");
        }

        if (slot.getBookedCount() >= slot.getMaxAppointments()) {
            return Result.error("该时段已满");
        }

        LambdaQueryWrapper<Appointment> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Appointment::getStudentId, studentId)
                   .eq(Appointment::getTimeSlotId, appointment.getTimeSlotId())
                   .ne(Appointment::getStatus, "CANCELLED");

        long existingCount = appointmentMapper.selectCount(checkWrapper);
        if (existingCount > 0) {
            return Result.error("您已经预约了这个时段，请勿重复预约");
        }

        appointment.setStatus("PENDING");
        int rows = appointmentMapper.insert(appointment);
        if (rows <= 0) {
            return Result.error("预约失败");
        }

        slot.setBookedCount(slot.getBookedCount() + 1);
        timeSlotMapper.updateById(slot);
        
        log.info("预约成功, appointmentId: {}, studentId: {}", appointment.getId(), studentId);

        return Result.success("预约成功，等待确认");
    }

    @Override
    public Result<List<Map<String, Object>>> getMyAppointments(Long studentId) {
        List<Appointment> appointments = appointmentMapper.selectList(
            new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getStudentId, studentId)
                .orderByDesc(Appointment::getCreatedAt)
        );

        List<Map<String, Object>> result = appointments.stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("appointment", app);

            Counselor counselor = counselorMapper.selectById(app.getCounselorId());
            if (counselor != null) {
                User user = userMapper.selectById(counselor.getUserId());
                Map<String, Object> counselorInfo = new HashMap<>();
                counselorInfo.put("id", counselor.getId());
                counselorInfo.put("name", user != null ? user.getName() : "未知");
                counselorInfo.put("title", counselor.getTitle());
                counselorInfo.put("specialization", counselor.getSpecialization());
                counselorInfo.put("avatar", counselor.getAvatar());
                map.put("counselor", counselorInfo);
            } else {
                map.put("counselor", null);
            }

            TimeSlot slot = timeSlotMapper.selectById(app.getTimeSlotId());
            map.put("timeSlot", slot);

            return map;
        }).toList();

        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> cancelAppointment(Long appointmentId, String cancelReason) {
        log.info("取消预约, appointmentId: {}, cancelReason: {}", appointmentId, cancelReason);
        
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        
        if (!"PENDING".equals(appointment.getStatus()) && !"CONFIRMED".equals(appointment.getStatus())) {
            return Result.error("该预约不可取消");
        }
        
        TimeSlot timeSlot = timeSlotMapper.selectById(appointment.getTimeSlotId());
        if (timeSlot != null) {
            java.time.LocalDateTime slotStartTime = java.time.LocalDateTime.of(timeSlot.getDate(), timeSlot.getStartTime());
            
            SystemConfig cancelDeadlineConfig = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>().eq(SystemConfig::getConfigKey, "cancel_deadline_hours")
            );
            
            int cancelDeadlineHours = 24;
            if (cancelDeadlineConfig != null) {
                cancelDeadlineHours = Integer.parseInt(cancelDeadlineConfig.getConfigValue());
            }
            
            if (java.time.LocalDateTime.now().isAfter(slotStartTime.minusHours(cancelDeadlineHours))) {
                return Result.error("距离预约开始时间不足" + cancelDeadlineHours + "小时，无法取消");
            }
            
            timeSlot.setBookedCount(Math.max(0, timeSlot.getBookedCount() - 1));
            timeSlotMapper.updateById(timeSlot);
        }
        
        appointment.setStatus("CANCELLED");
        appointmentMapper.updateById(appointment);
        
        log.info("预约已取消, appointmentId: {}", appointmentId);
        return Result.success("取消成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> sendAnonymousMessage(AnonymousMessage message) {
        // 从当前登录用户获取 userId
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("用户未登录");
            return Result.error("用户未登录");
        }
        
        Long userId = (Long) authentication.getPrincipal();
        log.info("提交匿名留言, userId: {}", userId);
        
        // 根据 userId 查询 student 表获取 studentId
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(studentWrapper);
        
        if (student == null) {
            log.error("未找到学生信息, userId: {}", userId);
            return Result.error("未找到学生信息，请先完善学生信息");
        }
        
        Long studentId = student.getId();
        log.info("找到学生信息, studentId: {}", studentId);
        
        // 使用正确的 studentId
        message.setStudentId(studentId);
        message.setStatus("PENDING");
        
        int rows = anonymousMessageMapper.insert(message);
        
        if (rows > 0) {
            log.info("匿名留言提交成功, messageId: {}", message.getId());
            return Result.success("留言成功");
        } else {
            log.error("匿名留言提交失败");
            return Result.error("留言失败");
        }
    }

    @Override
    public Result<List<AnonymousMessage>> getMyAnonymousMessages(Long userId) {
        log.info("查询学生匿名留言, userId: {}", userId);
        
        // 先根据 userId 查询 student 表获取 studentId
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getUserId, userId);
        Student student = studentMapper.selectOne(studentWrapper);
        
        if (student == null) {
            log.warn("未找到学生信息, userId: {}", userId);
            return Result.error("未找到学生信息");
        }
        
        Long studentId = student.getId();
        log.info("找到学生信息, studentId: {}", studentId);
        
        // 根据 studentId 查询留言记录
        LambdaQueryWrapper<AnonymousMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AnonymousMessage::getStudentId, studentId)
           .orderByDesc(AnonymousMessage::getCreatedAt);
        List<AnonymousMessage> messages = anonymousMessageMapper.selectList(wrapper);
        
        log.info("查询到 {} 条留言记录", messages.size());
        return Result.success(messages);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> changePassword(Long userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error("新密码长度不能少于6位");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!user.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }

        user.setPassword(newPassword);
        int rows = userMapper.updateById(user);

        return rows > 0 ? Result.success("密码修改成功") : Result.error("密码修改失败");
    }
}

