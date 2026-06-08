package com.ncu.mentalhealth.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ncu.mentalhealth.entity.TimeSlot;
import com.ncu.mentalhealth.mapper.TimeSlotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TimeSlotScheduledTask {

    private final TimeSlotMapper timeSlotMapper;

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void markExpiredSlotsAsInactive() {
        log.info("开始执行过期时段标记任务");

        LocalDate today = LocalDate.now();

        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(TimeSlot::getDate, today)
               .ne(TimeSlot::getStatus, "INACTIVE");

        List<TimeSlot> expiredSlots = timeSlotMapper.selectList(wrapper);

        log.info("找到 {} 个需要标记为过期的时段", expiredSlots.size());

        for (TimeSlot slot : expiredSlots) {
            slot.setStatus("INACTIVE");
            timeSlotMapper.updateById(slot);
        }

        log.info("过期时段标记任务完成，共标记 {} 个时段", expiredSlots.size());
    }
}