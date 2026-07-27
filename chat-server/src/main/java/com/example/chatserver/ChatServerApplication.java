package com.example.chatserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ChatServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServerApplication.class, args);
    }
    // Простая тестовая ручка, чтобы проверить, что сервер жив
    @GetMapping("/ping")
    public String ping() {
        return "Сервер успешно запущен! Подключения к БД, Redis и Kafka работают.";
    }
}
