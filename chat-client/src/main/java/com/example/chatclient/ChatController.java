package com.example.chatclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ChatController {

    @FXML
    private ListView<String> usersList;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;

    private String myUsername;
    private WebSocket webSocket;

    // Jackson - инструмент для превращения Java-объектов в JSON и обратно
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void initUser(String username) {
        this.myUsername = username;
        chatArea.appendText("Добро пожаловать, " + username + "! Подключение к серверу...\n");

        // 1. Сначала скачиваем историю сообщений из базы данных
        loadMessageHistory();

        // 2. Затем подключаемся к WebSocket для получения НОВЫХ сообщений в реальном времени
        connectToWebSocket();

        // 3. Загружаем список пользователей сразу при входе
        loadUserList();

        // 4. Запускаем фоновый таймер! Он будет дергать loadUserList() каждые 5 секунд
        Timeline timeline = new Timeline(new KeyFrame(
                Duration.seconds(5),
                event -> loadUserList()
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void loadUserList() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/users/online"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            List<UserStatusDto> users = objectMapper.readValue(
                                    response.body(), new TypeReference<>() {
                                    }
                            );
                            Platform.runLater(() -> {
                                // Запоминаем, кто был выделен мышкой, чтобы не сбрасывать выделение при обновлении
                                int selectedIndex = usersList.getSelectionModel().getSelectedIndex();

                                usersList.getItems().clear();
                                for (UserStatusDto u : users) {
                                    String status;
                                    if (u.getUsername().equals(myUsername)) {
                                        status = " (Вы)";
                                    } else {
                                        status = u.isOnline() ? " (Онлайн)" : " (Офлайн)";
                                    }
                                    usersList.getItems().add(u.getUsername() + status);
                                }

                                // Восстанавливаем выделение
                                if (selectedIndex >= 0 && selectedIndex < usersList.getItems().size()) {
                                    usersList.getSelectionModel().select(selectedIndex);
                                }
                            });
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void loadMessageHistory() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/messages/history"))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            // Распаковываем JSON-массив из сервера в список Java-объектов
                            List<ChatMessageDto> history = objectMapper.readValue(
                                    response.body(), new TypeReference<>() {
                                    }
                            );

                            // Обновляем экран (ОБЯЗАТЕЛЬНО через Platform.runLater!)
                            Platform.runLater(() -> {
                                for (ChatMessageDto msg : history) {
                                    chatArea.appendText(msg.getSender() + ": " + msg.getContent() + "\n");
                                }
                            });
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void connectToWebSocket() {
        httpClient.newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:8081/ws/chat"), new WebSocket.Listener() {

                    StringBuilder messageBuffer = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        Platform.runLater(() -> chatArea.appendText("[СИСТЕМА]: Соединение установлено!\n"));
                        WebSocket.Listener.super.onOpen(webSocket);
                    }

                    // Этот метод срабатывает АВТОМАТИЧЕСКИ, когда сервер (Кафка) присылает сообщение
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        messageBuffer.append(data);

                        if (last) {
                            String jsonMessage = messageBuffer.toString();
                            messageBuffer.setLength(0); // Очищаем буфер для следующего сообщения

                            try {
                                // Распаковываем прилетевший JSON
                                ChatMessageDto msg = objectMapper.readValue(jsonMessage, ChatMessageDto.class);

                                // Выводим на экран
                                Platform.runLater(() -> {
                                    chatArea.appendText(msg.getSender() + ": " + msg.getContent() + "\n");
                                });
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }
                })
                .thenAccept(ws -> this.webSocket = ws); // Сохраняем "трубу", чтобы потом в неё писать
    }

    @FXML
    protected void onSendMessage() {
        String text = messageField.getText();
        if (text.isEmpty() || webSocket == null) return;

        try {
            // 1. Создаем коробку с сообщением
            ChatMessageDto msg = new ChatMessageDto(myUsername, text, null);

            // 2. Превращаем коробку в JSON-строку
            String jsonMessage = objectMapper.writeValueAsString(msg);

            // 3. Кидаем JSON в трубу вебсокета (на сервер)
            webSocket.sendText(jsonMessage, true);

            // 4. Очищаем поле ввода
            messageField.clear();

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}