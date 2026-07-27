package com.example.chatserver.controller;

import com.example.chatserver.Entity.User;
import com.example.chatserver.dto.AuthRequest;
import com.example.chatserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    // Этот метод будет принимать POST-запросы на адрес http://localhost:8081/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // Передаем данные из JSON в наш сервис
            User user = userService.loginOrRegister(request.getUsername(), request.getPassword());
            // Возвращаем данные пользователя в ответ (HTTP 200 OK)
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // Если пароль неверный - возвращаем ошибку (HTTP 400 Bad Request)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}