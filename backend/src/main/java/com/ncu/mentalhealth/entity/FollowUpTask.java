package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("follow_up_tasks")
public class FollowUpTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    
    private Long advisorId;
    
    private String status; // PENDING/COMPLETED
    
    private String feedback;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
