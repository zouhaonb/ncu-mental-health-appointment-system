package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("appointments")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    
    private Long counselorId;
    
    private Long timeSlotId;
    
    private String status; // PENDING/CONFIRMED/COMPLETED/CANCELLED
    
    private String reason;
    
    private String cancelReason;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
