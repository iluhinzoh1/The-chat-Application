package com.example.chatclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ChatApplication extends Application {

    // Метод start - это точка входа в графический интерфейс
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chat Client");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
