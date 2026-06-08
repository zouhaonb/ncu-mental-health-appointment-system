package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@TableName("time_slots")
public class TimeSlot {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long counselorId;
    
    private LocalDate date;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private Integer maxAppointments;
    
    private Integer bookedCount;
    
    private String status; // ACTIVE/INACTIVE
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
