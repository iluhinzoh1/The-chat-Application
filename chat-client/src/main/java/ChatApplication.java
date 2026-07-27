import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ChatApplication extends Application {

    // Метод start - это точка входа в графический интерфейс
    @Override
    public void start(Stage primaryStage) {
        // Создаем простую текстовую надпись
        Label label = new Label("Привет! Я будущий мессенджер на JavaFX.");

        // Кладем её в базовый контейнер (StackPane центрирует элементы)
        StackPane root = new StackPane();
        root.getChildren().add(label);

        // Создаем сцену (содержимое окна) размером 400x300 пикселей
        Scene scene = new Scene(root, 400, 300);

        // Настраиваем саму "рамку" окна (Stage)
        primaryStage.setTitle("Децентрализованный Чат");
        primaryStage.setScene(scene);

        // Показываем окно на экране!
        primaryStage.show();
    }
}
