package com.campusmarket.faq.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("faq")
public class Faq {

    @TableId(type = IdType.AUTO)
    private Long id;

    @NotBlank(message = "问题不能为空")
    private String question;

    @NotBlank(message = "回答不能为空")
    private String answer;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
