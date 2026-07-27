package com.example.chatserver.dto;

import lombok.Data;

// DTO (Data Transfer Object) - это простая "коробка",
// в которую Spring Boot положит JSON, присланный пользователем.
@Data
public class AuthRequest {
    private String username;
    private String password;
}
