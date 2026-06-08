package com.ncu.mentalhealth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncu.mentalhealth.entity.*;
import com.ncu.mentalhealth.mapper.*;
import com.ncu.mentalhealth.service.CounselorService;
import com.ncu.mentalhealth.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CounselorServiceImpl implements CounselorService {

    private final CounselorMapper counselorMapper;
    private final UserMapper userMapper;
    private final TimeSlotMapper timeSlotMapper;
    private final AppointmentMapper appointmentMapper;
    private final ConsultationRecordMapper consultationRecordMapper;
    private final AnonymousMessageMapper anonymousMessageMapper;
    private final ReferralSuggestionMapper referralSuggestionMapper;
    private final StudentMapper studentMapper;
    private final AdvisorMapper advisorMapper;
    private final RiskAlertMapper riskAlertMapper;
    private final SystemConfigMapper systemConfigMapper;

    @Override
    public Result<Map<String, Object>> getCounselorInfo(Long userId) {
        log.info("查询咨询师信息, userId: {}", userId);
        
        // 查询 Counselor 信息
        LambdaQueryWrapper<Counselor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Counselor::getUserId, userId);
        Counselor counselor = counselorMapper.selectOne(wrapper);
        
        if (counselor == null) {
            log.warn("咨询师信息不存在, userId: {}", userId);
            return Result.error("咨询师信息不存在");
        }
        
        // 查询 User 信息获取姓名、手机号、邮箱
        User user = userMapper.selectById(counselor.getUserId());
        
        // 组装完整信息
        Map<String, Object> result = new HashMap<>();
        result.put("id", counselor.getId());
        result.put("userId", counselor.getUserId());
        result.put("name", user != null ? user.getName() : "");
        result.put("title", counselor.getTitle());
        result.put("specialization", counselor.getSpecialization());
        result.put("description", counselor.getDescription());
        result.put("avatar", counselor.getAvatar());
        result.put("phone", user != null ? user.getPhone() : "");
        result.put("email", user != null ? user.getEmail() : "");
        
        log.info("查询咨询师信息成功, counselorId: {}", counselor.getId());
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateCounselorInfo(Map<String, Object> updateData) {
        log.info("更新咨询师信息");
        
        // 获取 userId
        Long userId = (Long) updateData.get("userId");
        if (userId == null) {
            return Result.error("用户ID不能为空");
        }
        
        // 更新 User 表信息（name、phone、email）
        User user = userMapper.selectById(userId);
        if (user != null) {
            if (updateData.containsKey("name")) {
                user.setName((String) updateData.get("name"));
            }
            if (updateData.containsKey("phone")) {
                user.setPhone((String) updateData.get("phone"));
            }
            if (updateData.containsKey("email")) {
                user.setEmail((String) updateData.get("email"));
            }
            userMapper.updateById(user);
            log.info("更新用户信息成功, userId: {}", userId);
        }
        
        // 更新 Counselor 表信息
        LambdaQueryWrapper<Counselor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Counselor::getUserId, userId);
        Counselor counselor = counselorMapper.selectOne(wrapper);
        
        if (counselor != null) {
            if (updateData.containsKey("title")) {
                counselor.setTitle((String) updateData.get("title"));
            }
            if (updateData.containsKey("specialization")) {
                counselor.setSpecialization((String) updateData.get("specialization"));
            }
            if (updateData.containsKey("description")) {
                counselor.setDescription((String) updateData.get("description"));
            }
            if (updateData.containsKey("avatar")) {
                counselor.setAvatar((String) updateData.get("avatar"));
            }
            counselorMapper.updateById(counselor);
            log.info("更新咨询师信息成功, counselorId: {}", counselor.getId());
        }
        
        return Result.success("更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> addTimeSlot(TimeSlot slot) {
        log.info("添加时段, counselorId: {}, date: {}", slot.getCounselorId(), slot.getDate());
        
        if (slot.getDate().isBefore(LocalDate.now())) {
            return Result.error("不能设置过去的日期");
        }
        
        // 检查是否超出最大可预约天数
        int maxFutureDays = getMaxFutureDaysFromConfig();
        LocalDate maxDate = LocalDate.now().plusDays(maxFutureDays);
        if (slot.getDate().isAfter(maxDate)) {
            return Result.error("只能创建未来" + maxFutureDays + "天内的时段");
        }
        
        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeSlot::getCounselorId, slot.getCounselorId())
               .eq(TimeSlot::getDate, slot.getDate())
               .eq(TimeSlot::getStatus, "ACTIVE");
        List<TimeSlot> existingSlots = timeSlotMapper.selectList(wrapper);
        
        for (TimeSlot existing : existingSlots) {
            if (slot.getStartTime().isBefore(existing.getEndTime()) && 
                slot.getEndTime().isAfter(existing.getStartTime())) {
                return Result.error("时段与现有时段重叠: " + 
                    existing.getStartTime() + "-" + existing.getEndTime());
            }
        }
        
        slot.setBookedCount(0);
        slot.setStatus("ACTIVE");
        updateSlotStatusByBooking(slot);
        int rows = timeSlotMapper.insert(slot);
        return rows > 0 ? Result.success("添加成功") : Result.error("添加失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchAddTimeSlots(List<TimeSlot> slots) {
        log.info("批量添加时段, 数量: {}", slots.size());
        
        if (slots == null || slots.isEmpty()) {
            return Result.error("时段列表不能为空");
        }
        
        // 检查是否超出最大可预约天数
        int maxFutureDays = getMaxFutureDaysFromConfig();
        LocalDate maxDate = LocalDate.now().plusDays(maxFutureDays);
        
        for (TimeSlot slot : slots) {
            if (slot.getDate().isBefore(LocalDate.now())) {
                return Result.error("不能设置过去的日期: " + slot.getDate());
            }
            
            if (slot.getDate().isAfter(maxDate)) {
                return Result.error("只能创建未来" + maxFutureDays + "天内的时段，日期 " + slot.getDate() + " 超出范围");
            }
            
            LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TimeSlot::getCounselorId, slot.getCounselorId())
                   .eq(TimeSlot::getDate, slot.getDate())
                   .eq(TimeSlot::getStatus, "ACTIVE");
            List<TimeSlot> existingSlots = timeSlotMapper.selectList(wrapper);
            
            for (TimeSlot existing : existingSlots) {
                if (slot.getStartTime().isBefore(existing.getEndTime()) && 
                    slot.getEndTime().isAfter(existing.getStartTime())) {
                    return Result.error("时段与现有时段重叠: " + 
                        existing.getStartTime() + "-" + existing.getEndTime());
                }
            }
            
            // 检查列表内部是否有冲突
            for (TimeSlot other : slots) {
                if (other != slot && 
                    other.getDate().equals(slot.getDate()) &&
                    slot.getStartTime().isBefore(other.getEndTime()) && 
                    slot.getEndTime().isAfter(other.getStartTime())) {
                    return Result.error("时段列表内部存在时间重叠");
                }
            }
            
            slot.setBookedCount(0);
            slot.setStatus("ACTIVE");
            updateSlotStatusByBooking(slot);
            int rows = timeSlotMapper.insert(slot);
            if (rows == 0) {
                return Result.error("批量添加失败");
            }
        }
        
        log.info("批量添加时段成功, 数量: {}", slots.size());
        return Result.success("批量添加成功");
    }

    private void updateSlotStatusByBooking(TimeSlot slot) {
        // 如果已经是停用状态，不自动修改为ACTIVE
        if ("INACTIVE".equals(slot.getStatus())) {
            return;
        }
        
        if (slot.getBookedCount() >= slot.getMaxAppointments()) {
            slot.setStatus("FULL");
        } else if (slot.getBookedCount() > 0) {
            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Appointment::getTimeSlotId, slot.getId());
            List<Appointment> appointments = appointmentMapper.selectList(wrapper);
            
            boolean hasPendingAppointment = appointments.stream()
                .anyMatch(app -> "PENDING".equals(app.getStatus()) || "CONFIRMED".equals(app.getStatus()));
            
            if (hasPendingAppointment) {
                slot.setStatus("BOOKED");
            } else {
                slot.setStatus("ACTIVE");
            }
        } else {
            slot.setStatus("ACTIVE");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> deleteTimeSlot(Long slotId) {
        TimeSlot slot = timeSlotMapper.selectById(slotId);
        if (slot == null) {
            return Result.error("时段不存在");
        }
        
        if (slot.getBookedCount() > 0) {
            return Result.error("该时段已有预约，无法删除");
        }
        
        int rows = timeSlotMapper.deleteById(slotId);
        return rows > 0 ? Result.success("删除成功") : Result.error("删除失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> updateTimeSlot(Long slotId, TimeSlot updateSlot) {
        log.info("更新时段信息, slotId: {}", slotId);
        
        TimeSlot existingSlot = timeSlotMapper.selectById(slotId);
        if (existingSlot == null) {
            log.warn("时段不存在, slotId: {}", slotId);
            return Result.error("时段不存在");
        }
        
        if (existingSlot.getDate().isBefore(LocalDate.now())) {
            log.warn("已过期的时段不可编辑, slotId: {}", slotId);
            return Result.error("已过期的时段不可编辑");
        }
        
        boolean hasActiveBooking = existingSlot.getBookedCount() > 0;
        boolean isInactive = "INACTIVE".equals(existingSlot.getStatus());
        
        if (!isInactive && hasActiveBooking) {
            log.warn("有活跃预约的时段不可编辑, slotId: {}", slotId);
            return Result.error("该时段有活跃预约，无法编辑");
        }
        
        if (updateSlot.getDate() != null && !updateSlot.getDate().equals(existingSlot.getDate())) {
            if (updateSlot.getDate().isBefore(LocalDate.now())) {
                return Result.error("不能修改为过去的日期");
            }
            
            LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TimeSlot::getCounselorId, existingSlot.getCounselorId())
                   .eq(TimeSlot::getDate, updateSlot.getDate())
                   .eq(TimeSlot::getStatus, "ACTIVE");
            List<TimeSlot> existingSlots = timeSlotMapper.selectList(wrapper);
            
            for (TimeSlot slot : existingSlots) {
                if (!slot.getId().equals(slotId)) {
                    if (updateSlot.getStartTime().isBefore(slot.getEndTime()) && 
                        updateSlot.getEndTime().isAfter(slot.getStartTime())) {
                        return Result.error("修改后的时段与现有时段重叠: " + 
                            slot.getStartTime() + "-" + slot.getEndTime());
                    }
                }
            }
            
            existingSlot.setDate(updateSlot.getDate());
        }
        
        if (updateSlot.getStartTime() != null && updateSlot.getEndTime() != null) {
            if (!updateSlot.getStartTime().isBefore(updateSlot.getEndTime())) {
                return Result.error("开始时间必须早于结束时间");
            }
            
            if (!updateSlot.getStartTime().equals(existingSlot.getStartTime()) || 
                !updateSlot.getEndTime().equals(existingSlot.getEndTime())) {
                
                LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TimeSlot::getCounselorId, existingSlot.getCounselorId())
                       .eq(TimeSlot::getDate, existingSlot.getDate())
                       .eq(TimeSlot::getStatus, "ACTIVE");
                List<TimeSlot> existingSlots = timeSlotMapper.selectList(wrapper);
                
                for (TimeSlot slot : existingSlots) {
                    if (!slot.getId().equals(slotId)) {
                        if (updateSlot.getStartTime().isBefore(slot.getEndTime()) && 
                            updateSlot.getEndTime().isAfter(slot.getStartTime())) {
                            return Result.error("修改后的时段与现有时段重叠: " + 
                                slot.getStartTime() + "-" + slot.getEndTime());
                        }
                    }
                }
                
                existingSlot.setStartTime(updateSlot.getStartTime());
                existingSlot.setEndTime(updateSlot.getEndTime());
            }
        }
        
        if (updateSlot.getMaxAppointments() != null) {
            if (updateSlot.getMaxAppointments() < existingSlot.getBookedCount()) {
                return Result.error("最大预约数不能小于当前已预约数: " + existingSlot.getBookedCount());
            }
            existingSlot.setMaxAppointments(updateSlot.getMaxAppointments());
        }
        
        if (!isInactive) {
            updateSlotStatusByBooking(existingSlot);
        }
        
        int rows = timeSlotMapper.updateById(existingSlot);
        log.info("更新时段完成, slotId: {}, rows: {}", slotId, rows);
        return rows > 0 ? Result.success("更新成功") : Result.error("更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> toggleTimeSlotStatus(Long slotId) {
        TimeSlot slot = timeSlotMapper.selectById(slotId);
        if (slot == null) {
            return Result.error("时段不存在");
        }
        
        if (slot.getDate().isBefore(LocalDate.now())) {
            return Result.error("已过期的时段不可操作");
        }
        
        if ("FULL".equals(slot.getStatus())) {
            return Result.error("已满的时段不可停用");
        }
        
        if ("ACTIVE".equals(slot.getStatus()) || "BOOKED".equals(slot.getStatus())) {
            LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Appointment::getTimeSlotId, slotId);
            List<Appointment> appointments = appointmentMapper.selectList(wrapper);
            
            boolean hasPendingAppointment = appointments.stream()
                .anyMatch(app -> "PENDING".equals(app.getStatus()) || "CONFIRMED".equals(app.getStatus()));
            
            if (hasPendingAppointment) {
                return Result.error("该时段有未完成预约，无法停用");
            }
            
            slot.setStatus("INACTIVE");
        } else if ("INACTIVE".equals(slot.getStatus())) {
            slot.setStatus("ACTIVE");
            updateSlotStatusByBooking(slot);
        }
        
        int rows = timeSlotMapper.updateById(slot);
        return rows > 0 ? Result.success("操作成功") : Result.error("操作失败");
    }

    @Override
    public Result<List<TimeSlot>> getMyTimeSlots(Long counselorId) {
        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeSlot::getCounselorId, counselorId)
               .orderByAsc(TimeSlot::getDate, TimeSlot::getStartTime);
        List<TimeSlot> slots = timeSlotMapper.selectList(wrapper);
        
        LocalDate today = LocalDate.now();
        for (TimeSlot slot : slots) {
            if (slot.getDate().isBefore(today)) {
                if (!"INACTIVE".equals(slot.getStatus())) {
                    slot.setStatus("INACTIVE");
                    timeSlotMapper.updateById(slot);
                }
            } else {
                updateSlotStatusByBooking(slot);
                timeSlotMapper.updateById(slot);
            }
        }
        
        return Result.success(slots);
    }

    @Override
    public Result<List<Map<String, Object>>> getAppointments(Long counselorId, String status) {
        log.info("查询咨询师预约列表, counselorId: {}, status: {}", counselorId, status);
        
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getCounselorId, counselorId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.orderByDesc(Appointment::getCreatedAt);
        
        List<Appointment> appointments = appointmentMapper.selectList(wrapper);
        log.info("查询到 {} 条预约记录", appointments.size());
        
        List<Map<String, Object>> result = appointments.stream().map(app -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("appointment", app);
            
            Student student = studentMapper.selectById(app.getStudentId());
            map.put("student", student);
            
            if (student != null) {
                User user = userMapper.selectById(student.getUserId());
                map.put("user", user);
                log.info("预约ID: {}, 学生ID: {}, 学号: {}, 姓名: {}", 
                    app.getId(), app.getStudentId(), 
                    student.getStudentNo(), 
                    user != null ? user.getName() : "null");
            } else {
                log.warn("预约ID: {}, 学生不存在, studentId: {}", app.getId(), app.getStudentId());
            }
            
            TimeSlot slot = timeSlotMapper.selectById(app.getTimeSlotId());
            map.put("timeSlot", slot);
            
            // 检查是否已有咨询记录
            LambdaQueryWrapper<ConsultationRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ConsultationRecord::getAppointmentId, app.getId());
            long recordCount = consultationRecordMapper.selectCount(recordWrapper);
            map.put("hasConsultationRecord", recordCount > 0);
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> confirmAppointment(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        
        if (!"PENDING".equals(appointment.getStatus())) {
            return Result.error("该预约无法确认");
        }
        
        appointment.setStatus("CONFIRMED");
        int rows = appointmentMapper.updateById(appointment);
        
        if (rows > 0) {
            TimeSlot slot = timeSlotMapper.selectById(appointment.getTimeSlotId());
            if (slot != null) {
                updateSlotStatusByBooking(slot);
                timeSlotMapper.updateById(slot);
            }
        }
        
        return rows > 0 ? Result.success("确认成功") : Result.error("确认失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> rejectAppointment(Long appointmentId, String reason) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        
        if (!"PENDING".equals(appointment.getStatus())) {
            return Result.error("该预约无法拒绝");
        }
        
        appointment.setStatus("CANCELLED");
        appointment.setCancelReason(reason);
        appointmentMapper.updateById(appointment);
        
        TimeSlot slot = timeSlotMapper.selectById(appointment.getTimeSlotId());
        if (slot != null) {
            slot.setBookedCount(Math.max(0, slot.getBookedCount() - 1));
            updateSlotStatusByBooking(slot);
            timeSlotMapper.updateById(slot);
        }
        
        return Result.success("已拒绝");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            return Result.error("预约不存在");
        }
        
        appointment.setStatus("COMPLETED");
        int rows = appointmentMapper.updateById(appointment);
        
        if (rows > 0) {
            TimeSlot slot = timeSlotMapper.selectById(appointment.getTimeSlotId());
            if (slot != null) {
                updateSlotStatusByBooking(slot);
                timeSlotMapper.updateById(slot);
            }
        }
        
        return rows > 0 ? Result.success("已完成") : Result.error("操作失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> saveConsultationRecord(ConsultationRecord record) {
        log.info("保存咨询记录, appointmentId: {}", record.getAppointmentId());
        
        // 根据 appointmentId 查询预约信息，获取 studentId
        if (record.getStudentId() == null && record.getAppointmentId() != null) {
            Appointment appointment = appointmentMapper.selectById(record.getAppointmentId());
            if (appointment != null) {
                record.setStudentId(appointment.getStudentId());
                log.info("从预约记录中获取 studentId: {}", record.getStudentId());
            } else {
                log.error("预约记录不存在, appointmentId: {}", record.getAppointmentId());
                return Result.error("预约记录不存在");
            }
        }
        
        if (record.getStudentId() == null) {
            log.error("studentId 为空");
            return Result.error("学生ID不能为空");
        }
        
        int rows = consultationRecordMapper.insert(record);
        if (rows <= 0) {
            log.error("保存咨询记录失败");
            return Result.error("保存失败");
        }
        
        log.info("咨询记录保存成功, recordId: {}", record.getId());
        
        if ("MEDIUM".equals(record.getRiskLevel()) || "HIGH".equals(record.getRiskLevel())) {
            Student student = studentMapper.selectById(record.getStudentId());
            if (student != null && student.getAdvisorId() != null) {
                // 检查是否已存在相同的预警记录（同一学生、同一来源、同一类型）
                LambdaQueryWrapper<RiskAlert> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(RiskAlert::getStudentId, record.getStudentId())
                           .eq(RiskAlert::getAdvisorId, student.getAdvisorId())
                           .eq(RiskAlert::getAlertSource, "COUNSELOR")
                           .eq(RiskAlert::getAlertType, "CONSULTATION");
                
                if (riskAlertMapper.selectCount(checkWrapper) == 0) {
                    RiskAlert alert = new RiskAlert();
                    alert.setStudentId(record.getStudentId());
                    alert.setAdvisorId(student.getAdvisorId());
                    alert.setAlertSource("COUNSELOR");
                    alert.setAlertType("CONSULTATION");
                    alert.setRiskLevel(record.getRiskLevel());
                    alert.setIsRead(0);
                    riskAlertMapper.insert(alert);
                    log.info("已创建咨询师评估风险预警, studentId: {}, level: {}", 
                        record.getStudentId(), record.getRiskLevel());
                } else {
                    log.info("该学生已有咨询师评估预警，跳过创建, studentId: {}", record.getStudentId());
                }
            }
        }
        
        return Result.success("保存成功");
    }

    @Override
    public Result<List<Map<String, Object>>> getConsultationRecords(Long counselorId) {
        log.info("查询咨询师咨询记录, counselorId: {}", counselorId);
        
        LambdaQueryWrapper<ConsultationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConsultationRecord::getCounselorId, counselorId)
               .orderByDesc(ConsultationRecord::getCreatedAt);
        
        List<ConsultationRecord> records = consultationRecordMapper.selectList(wrapper);
        log.info("查询到 {} 条咨询记录", records.size());
        
        List<Map<String, Object>> result = records.stream().map(record -> {
            Map<String, Object> map = new java.util.HashMap<>();
            
            // 咨询记录基本信息
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
            
            // 查询学生信息
            Student student = studentMapper.selectById(record.getStudentId());
            if (student != null) {
                map.put("studentId", student.getId());
                User user = userMapper.selectById(student.getUserId());
                if (user != null) {
                    map.put("studentName", user.getName());
                    log.info("咨询记录ID: {}, 学生ID: {}, 姓名: {}", 
                        record.getId(), student.getId(), user.getName());
                } else {
                    log.warn("咨询记录ID: {}, 学生用户不存在, userId: {}", 
                        record.getId(), student.getUserId());
                }
            } else {
                log.warn("咨询记录ID: {}, 学生不存在, studentId: {}", 
                    record.getId(), record.getStudentId());
            }
            
            // 查询预约状态
            if (record.getAppointmentId() != null) {
                Appointment appointment = appointmentMapper.selectById(record.getAppointmentId());
                if (appointment != null) {
                    map.put("appointmentStatus", appointment.getStatus());
                    log.info("咨询记录ID: {}, 预约ID: {}, 状态: {}", 
                        record.getId(), appointment.getId(), appointment.getStatus());
                } else {
                    log.warn("咨询记录ID: {}, 预约不存在, appointmentId: {}", 
                        record.getId(), record.getAppointmentId());
                }
            }
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    public Result<List<AnonymousMessage>> getAnonymousMessages() {
        LambdaQueryWrapper<AnonymousMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(AnonymousMessage::getStatus)
               .orderByDesc(AnonymousMessage::getCreatedAt);
        return Result.success(anonymousMessageMapper.selectList(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> replyAnonymousMessage(Long id, String reply, Long userId) {
        AnonymousMessage message = anonymousMessageMapper.selectById(id);
        if (message == null) {
            return Result.error("留言不存在");
        }
        
        message.setReply(reply);
        message.setStatus("REPLIED");
        
        int rows = anonymousMessageMapper.updateById(message);
        return rows > 0 ? Result.success("回复成功") : Result.error("回复失败");
    }

    @Override
    public Result<List<Map<String, Object>>> getReferralSuggestionsForCounselor(Long counselorId) {
        // 查询指派给该咨询师的转介建议
        LambdaQueryWrapper<ReferralSuggestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReferralSuggestion::getAssignedCounselorId, counselorId)
               .orderByDesc(ReferralSuggestion::getCreatedAt);
        
        List<ReferralSuggestion> suggestions = referralSuggestionMapper.selectList(wrapper);
        
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
            
            // 查询辅导员信息
            Advisor advisor = advisorMapper.selectById(suggestion.getAdvisorId());
            if (advisor != null) {
                User advisorUser = userMapper.selectById(advisor.getUserId());
                map.put("advisor", advisor);
                map.put("advisorUser", advisorUser);
            } else {
                map.put("advisor", null);
                map.put("advisorUser", null);
            }
            
            return map;
        }).toList();
        
        return Result.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> handleReferralSuggestion(Long suggestionId, String action) {
        ReferralSuggestion suggestion = referralSuggestionMapper.selectById(suggestionId);
        if (suggestion == null) {
            return Result.error("转介建议不存在");
        }
        
        if ("ACCEPT".equals(action)) {
            suggestion.setHandleStatus("ACCEPTED");
            int rows = referralSuggestionMapper.updateById(suggestion);
            return rows > 0 ? Result.success("转介建议已接受") : Result.error("操作失败");
        } else if ("REJECT".equals(action)) {
            suggestion.setHandleStatus("REJECTED");
            int rows = referralSuggestionMapper.updateById(suggestion);
            return rows > 0 ? Result.success("已拒绝转介建议") : Result.error("操作失败");
        } else {
            return Result.error("无效的操作");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> acceptReferralWithTimeSlot(Long suggestionId, Long timeSlotId) {
        log.info("接受转介建议并创建预约, suggestionId: {}, timeSlotId: {}", suggestionId, timeSlotId);
        
        ReferralSuggestion suggestion = referralSuggestionMapper.selectById(suggestionId);
        if (suggestion == null) {
            log.error("转介建议不存在, suggestionId: {}", suggestionId);
            return Result.error("转介建议不存在");
        }
        
        if (!"PENDING".equals(suggestion.getHandleStatus())) {
            log.error("转介建议状态不是待处理, status: {}", suggestion.getHandleStatus());
            return Result.error("该转介建议已处理");
        }
        
        TimeSlot timeSlot = timeSlotMapper.selectById(timeSlotId);
        if (timeSlot == null) {
            log.error("时间段不存在, timeSlotId: {}", timeSlotId);
            return Result.error("时间段不存在");
        }
        
        if (!"ACTIVE".equals(timeSlot.getStatus())) {
            log.error("时间段不可用, status: {}", timeSlot.getStatus());
            return Result.error("该时间段不可用");
        }
        
        if (!timeSlot.getCounselorId().equals(suggestion.getAssignedCounselorId())) {
            log.error("时间段不属于该咨询师, counselorId: {}, slotCounselorId: {}", 
                suggestion.getAssignedCounselorId(), timeSlot.getCounselorId());
            return Result.error("该时间段不属于您");
        }
        
        suggestion.setHandleStatus("ACCEPTED");
        int suggestionRows = referralSuggestionMapper.updateById(suggestion);
        if (suggestionRows == 0) {
            log.error("更新转介建议状态失败");
            return Result.error("操作失败");
        }
        
        log.info("转介建议状态已更新为ACCEPTED");
        
        Appointment appointment = new Appointment();
        appointment.setStudentId(suggestion.getStudentId());
        appointment.setCounselorId(suggestion.getAssignedCounselorId());
        appointment.setTimeSlotId(timeSlotId);
        appointment.setStatus("CONFIRMED");
        appointment.setReason("辅导员转介：" + suggestion.getReason());
        
        timeSlot.setBookedCount(timeSlot.getBookedCount() + 1);
        updateSlotStatusByBooking(timeSlot);
        timeSlotMapper.updateById(timeSlot);
        
        log.info("更新时间段, timeSlotId: {}, bookedCount: {}, status: {}", 
            timeSlot.getId(), timeSlot.getBookedCount(), timeSlot.getStatus());
        
        int appointmentRows = appointmentMapper.insert(appointment);
        if (appointmentRows == 0) {
            log.error("预约记录创建失败");
            return Result.error("预约记录创建失败");
        }
        
        log.info("预约记录创建成功, appointmentId: {}, timeSlotId: {}, date: {}", 
            appointment.getId(), timeSlot.getId(), timeSlot.getDate());
        
        return Result.success("转介建议已接受，已为学生创建预约记录（" + 
            timeSlot.getDate() + " " + timeSlot.getStartTime() + "）");
    }

    /**
     * 从系统配置中获取最大可预约天数
     * @return 最大可预约天数，默认14天
     */
    private int getMaxFutureDaysFromConfig() {
        try {
            SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                    .eq(SystemConfig::getConfigKey, "max_future_days")
            );
            if (config != null) {
                return Integer.parseInt(config.getConfigValue());
            }
        } catch (Exception e) {
            log.warn("读取max_future_days配置失败，使用默认值14", e);
        }
        return 14;
    }
}
