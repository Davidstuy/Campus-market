package com.campusmarket.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多200字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "请选择主题")
    private Long topicId;

    /** 已上传的媒体 URL 列表 */
    private List<String> media;
}
