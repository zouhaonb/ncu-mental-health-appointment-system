package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("counselors")
public class Counselor {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String specialization;

    private String description;

    private String avatar;

    private Integer isActive;

    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
