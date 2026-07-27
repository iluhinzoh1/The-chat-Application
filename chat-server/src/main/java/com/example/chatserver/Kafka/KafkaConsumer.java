package com.example.chatserver.Kafka;

import com.example.chatserver.dto.ChatMessageDto;
import com.example.chatserver.service.MessageService;
import com.example.chatserver.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;

    @KafkaListener(topics = "chat-messages", groupId = "chat-backend-group")
    public void listen(String message) {
        log.info("Кафка поймала сообщение из шины: {}", message);
        try {
            ChatMessageDto messageDto = objectMapper.readValue(message, ChatMessageDto.class);
            messageService.saveMessage(messageDto);
            // Передаем сообщение вебсокету, чтобы он разослал его всем открытым "трубам"
            chatWebSocketHandler.broadcast(message);
        } catch (Exception e) {
            log.error("ошибка при обработке сообщений из кафки", e);
        }
    }
}
