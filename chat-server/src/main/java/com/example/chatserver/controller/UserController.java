package com.example.chatserver.controller;

import com.example.chatserver.dto.UserStatusDto;
import com.example.chatserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/online")
    public List<UserStatusDto> getOnlineUsers() { // ВАЖНО: Сделали метод public!
        return userRepository.findAll().stream().map(user -> {
            // ВАЖНО: Добавили двоеточие в конце строки!
            String redis = "user:online:" + user.getUsername();
            boolean isOnline = Boolean.TRUE.equals(stringRedisTemplate.hasKey(redis));
            return new UserStatusDto(user.getUsername(), isOnline);
        }).collect(Collectors.toList());
    }
}