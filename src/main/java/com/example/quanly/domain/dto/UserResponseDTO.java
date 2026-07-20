package com.example.quanly.domain.dto;

import lombok.Data;

@Data
public class UserResponseDTO {
    private long id;
    private String email;
    private String fullName;
    private String address;
    private String phone;
    private String avatar;
    private String memberLevel;
    private String roleName;
}
