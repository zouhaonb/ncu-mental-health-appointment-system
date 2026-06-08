package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("anonymous_messages")
public class AnonymousMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long studentId;
    
    private String content;
    
    private String reply;
    
    private Long counselorId;
    
    private String status; // PENDING/REPLIED
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
