package com.campusmarket.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusmarket.chat.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {
}
