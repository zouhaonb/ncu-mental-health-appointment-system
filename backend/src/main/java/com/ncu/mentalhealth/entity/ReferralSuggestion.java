package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("referral_suggestions")
public class ReferralSuggestion {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long advisorId;
    
    private Long studentId;
    
    private Long assignedCounselorId;
    
    private String reason;
    
    private String handleStatus; // PENDING/ACCEPTED/REJECTED
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
