package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("self_assessments")
public class SelfAssessment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    
    private String scaleType; // DEPRESSION/ANXIETY
    
    private Integer totalScore;
    
    private String riskLevel; // LOW/MEDIUM/HIGH
    
    private String resultJson;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
