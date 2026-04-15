package com.example.quanly.mapper;

import com.example.quanly.domain.ChatMessage;
import com.example.quanly.domain.dto.ChatMessageDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName", source = "sender.fullName")
    @Mapping(target = "sentAt", expression = "java(message.getSentAt() != null ? message.getSentAt().format(java.time.format.DateTimeFormatter.ofPattern(\"HH:mm dd/MM/yyyy\")) : \"\")")
    ChatMessageDto toDTO(ChatMessage message);
}
