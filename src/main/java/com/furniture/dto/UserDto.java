package com.furniture.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private Boolean enabled = true;
}