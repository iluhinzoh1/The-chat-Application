package com.example.chatclient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    public void onLoginButtonClick() throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Пароль или логин не могут быть пустыми чипинён");
            return;
        }
        String jsonPayload = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // 3. Отправляем запрос асинхронно (чтобы окно программы не зависло во время ожидания)
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    // Platform.runLater говорит JavaFX: "Обнови интерфейс, когда будет время"
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            errorLabel.setStyle("-fx-text-fill: green;");
                            errorLabel.setText("Успешный вход! (Ответ: " + response.statusCode() + ")");
                            // ВАЖНО: Добавляем вызов метода смены окна!
                            openChatWindow(username);
                        } else {
                            errorLabel.setStyle("-fx-text-fill: red;");
                            errorLabel.setText("Ошибка: " + response.statusCode());
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> errorLabel.setText("Нет связи с сервером!"));
                    return null;
                });
    }
    private void openChatWindow(String username) {
        try {
            // 1. Загружаем чертеж нового окна
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chat.fxml"));
            Parent root = loader.load();

            // 2. Берем мозг нового окна (ChatController) и передаем ему наш логин
            ChatController chatController = loader.getController();
            chatController.initUser(username);

            // 3. Достаем текущее окно (Stage), в котором находится наша кнопка
            Stage stage = (Stage) errorLabel.getScene().getWindow();

            // 4. Меняем в окне старую сцену (Логин) на новую (Чат)
            stage.setScene(new Scene(root, 700, 500));
            stage.setTitle("Супер Чат - " + username);

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Ошибка загрузки окна чата!");
        }
    }
}
