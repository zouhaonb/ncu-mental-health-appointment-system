package com.ncu.mentalhealth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncu.mentalhealth.entity.Appointment;
import com.ncu.mentalhealth.entity.TimeSlot;
import com.ncu.mentalhealth.mapper.AppointmentMapper;
import com.ncu.mentalhealth.mapper.TimeSlotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约自动确认定时任务
 * 用于处理未及时处理的预约申请
 *
 * @author System
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentAutoConfirmTask {

    private final AppointmentMapper appointmentMapper;
    private final TimeSlotMapper timeSlotMapper;

    @Scheduled(cron = "0 * * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmPendingAppointments() {
        log.info("开始执行预约自动确认任务");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeHoursLater = now.plusHours(3);

        // 查询所有PENDING状态的预约
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getStatus, "PENDING");

        List<Appointment> pendingAppointments = appointmentMapper.selectList(wrapper);

        int autoConfirmedCount = 0;

        for (Appointment appointment : pendingAppointments) {
            // 获取对应的时段信息
            TimeSlot timeSlot = timeSlotMapper.selectById(appointment.getTimeSlotId());

            if (timeSlot != null) {
                // 计算时段的开始时间（日期+时间）
                LocalDateTime slotStartTime = LocalDateTime.of(timeSlot.getDate(), timeSlot.getStartTime());

                // 如果当前时间已经超过时段开始前3小时，则自动确认
                if (now.isAfter(slotStartTime.minusHours(3))) {
                    appointment.setStatus("CONFIRMED");
                    appointmentMapper.updateById(appointment);

                    log.info("自动确认预约 ID: {}, 时段: {} {}",
                            appointment.getId(), timeSlot.getDate(), timeSlot.getStartTime());

                    autoConfirmedCount++;
                }
            }
        }

        log.info("预约自动确认任务完成，共自动确认 {} 个预约", autoConfirmedCount);
    }
}
