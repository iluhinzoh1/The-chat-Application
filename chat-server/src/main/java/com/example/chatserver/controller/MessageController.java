package com.example.chatserver.controller;

import com.example.chatserver.dto.ChatMessageDto;
import com.example.chatserver.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Ручка для получения истории. Доступна по адресу GET /api/messages/history
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDto>> getHistory() {
        List<ChatMessageDto> history = messageService.getMessageHistory();
        return ResponseEntity.ok(history);
    }
}
