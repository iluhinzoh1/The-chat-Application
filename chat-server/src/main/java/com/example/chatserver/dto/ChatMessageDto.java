package com.example.chatserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    private String sender;   // Логин отправителя (например, "vasya")
    private String content;  // Текст сообщения
    private String timestamp; // Время (для истории)
}