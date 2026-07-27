package com.example.chatserver.service;

import com.example.chatserver.Entity.Message;
import com.example.chatserver.Entity.User;
import com.example.chatserver.dto.ChatMessageDto;
import com.example.chatserver.repository.MessageRepository;
import com.example.chatserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    // Метод сохранения нового сообщения в БД
    public void saveMessage(ChatMessageDto dto) {
        // 1. Ищем пользователя по логину
        User sender = userRepository.findByUsername(dto.getSender())
                .orElseThrow(() -> new RuntimeException("Пользователь " + dto.getSender() + " не найден!"));

        // 2. Создаем сущность сообщения
        Message message = Message.builder()
                .sender(sender)
                .content(dto.getContent())
                // timestamp сгенерируется сам благодаря @PrePersist
                .build();

        // 3. Сохраняем в PostgreSQL
        messageRepository.save(message);
        log.info("Сообщение от {} успешно сохранено в БД", dto.getSender());
    }

    // Метод получения истории (50 последних)
    public List<ChatMessageDto> getMessageHistory() {
        return messageRepository.findTop200ByOrderByTimestampAsc().stream()
                .map(msg -> new ChatMessageDto(
                        msg.getSender().getUsername(),
                        msg.getContent(),
                        msg.getTimestamp().toString() // Превращаем время в строку
                ))
                .collect(Collectors.toList());
    }
}