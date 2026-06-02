package com.campusmarket.community.dto;

import lombok.Data;

@Data
public class TopicVO {
    private Long id;
    private String name;
    private String icon;
    private Integer sortOrder;
    private Long postCount;
}
