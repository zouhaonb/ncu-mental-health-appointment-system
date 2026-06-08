package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("risk_alerts")
public class RiskAlert {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    
    private Long advisorId;
    
    private String alertSource; // SELF_ASSESSMENT/COUNSELOR
    
    private String alertType;
    
    private String riskLevel; // LOW/MEDIUM/HIGH
    
    private Integer isRead;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
