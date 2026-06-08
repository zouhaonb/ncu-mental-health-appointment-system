package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("key_concern_students")
public class KeyConcernStudent {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long advisorId;
    
    private Long studentId;
    
    private String notes;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
