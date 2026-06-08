package com.ncu.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("articles")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String content;
    
    private String type; // ARTICLE/NOTICE/ACTIVITY
    
    private String college;
    
    private Long authorId;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
