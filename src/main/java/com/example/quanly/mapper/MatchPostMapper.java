package com.example.quanly.mapper;

import com.example.quanly.domain.MatchPost;
import com.example.quanly.domain.dto.MatchPostResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MatchPostMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.fullName")
    @Mapping(target = "playDateStr", expression = "java(post.getPlayDate() != null ? post.getPlayDate().format(java.time.format.DateTimeFormatter.ofPattern(\"dd/MM/yyyy\")) : \"\")")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "joined", ignore = true)
    MatchPostResponseDTO toDTO(MatchPost post);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "messages", ignore = true)
    MatchPost toEntity(MatchPostResponseDTO dto);
}
