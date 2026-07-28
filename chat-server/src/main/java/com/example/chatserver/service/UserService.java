package com.example.chatserver.service;

import com.example.chatserver.Entity.User;
import com.example.chatserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // Встроенный инструмент Спринга для общения с Redis
    private final StringRedisTemplate redisTemplate;

    public User loginOrRegister(String username, String password, String ip) {
        // 1. Ищем пользователя в PostgreSQL
        Optional<User> existingUser = userRepository.findByUsername(username);
        User user;
        if (existingUser.isEmpty()) {
            String ipKey = "rate_limit:ip:" + ip;
            String attempStr = redisTemplate.opsForValue().get(ipKey);
            int attemps = (attempStr != null) ?  Integer.parseInt(attempStr) : 0;
            if (attemps > 1) {
                throw new RuntimeException("а больше одного ты не сделаешь другалек \n " +
                        "волосатый ты пенёк");
            }
            redisTemplate.opsForValue().increment(ipKey);
            // Если это первая попытка, ставим таймер на 24 часа
            if (attemps == 0) {
                redisTemplate.expire(ipKey, Duration.ofHours(24));
            }
            // Если нет - создаем нового (Регистрация)
            user = User.builder()
                    .username(username)
                    .password(password) // В реальном проекте пароли шифруют (BCrypt), но мы пока упрощаем
                    .build();
            user = userRepository.save(user);
        } else {
            // Если есть - проверяем пароль
            user = existingUser.get();
            if (!user.getPassword().equals(password)) {
                throw new RuntimeException("Неверный пароль!");
            }
        }
        // 2. Магия Redis: записываем статус "Онлайн" на 5 минут
        // Если юзер не будет подавать признаков жизни 5 минут, Редис сам удалит эту запись
        String redisKey = "user:online:" + user.getUsername();
        redisTemplate.opsForValue().set(redisKey, "true", Duration.ofMinutes(5));
        return user;
    }
}
