package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("consultation_records")
public class ConsultationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long appointmentId;
    
    private Long studentId;
    
    private Long counselorId;
    
    private String content;
    
    private String assessment;
    
    private String riskLevel; // LOW/MEDIUM/HIGH
    
    private Integer isClosed;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
