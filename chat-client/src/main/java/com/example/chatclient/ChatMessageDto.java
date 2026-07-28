package com.example.chatclient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Эта "коробка" должна точь-в-точь совпадать с той, что лежит на сервере!
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {
    private String sender;
    private String content;
    private String timestamp;
}