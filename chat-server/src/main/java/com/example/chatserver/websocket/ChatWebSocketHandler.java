package com.example.chatserver.websocket;

import com.example.chatserver.Kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j // Аннотация для удобного логирования в консоль
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Та самая "Карта Сессий", о которой мы говорили.
    // Хранит все текущие открытые трубы (вебсокеты) к пользователям.
    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();
    private final KafkaProducer kafkaProducer;

    // Срабатывает, когда кто-то успешно подключился
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Пока мы не сделали авторизацию в вебсокете, будем использовать ID сессии
        String sessionId = session.getId();
        activeSessions.put(sessionId, session);
        log.info("Новое подключение! ID сессии: {}. Всего онлайн: {}", sessionId, activeSessions.size());
    }

    // Срабатывает, когда кто-то закрыл программу или пропал интернет
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        activeSessions.remove(sessionId);
        log.info("Отключение. ID сессии: {}. Всего онлайн: {}", sessionId, activeSessions.size());
    }

    // Срабатывает, когда от клиента прилетает новое сообщение
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Получено сообщение от {}: {}", session.getId(), payload);
        kafkaProducer.sendMessage(payload);
        // Позже здесь мы будем отправлять это сообщение в Kafka!
    }

    public void broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);

        // Пробегаемся по нашей "записной книжке" сессий
        for (WebSocketSession session : activeSessions.values()) {
            if (session.isOpen()) {
                try {
                    // Пихаем сообщение прямо в открытую трубу
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    log.error("Ошибка при отправке сообщения клиенту: {}", session.getId(), e);
                }
            }
        }
    }
}