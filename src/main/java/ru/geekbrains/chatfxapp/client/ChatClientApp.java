package ru.geekbrains.chatfxapp.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.geekbrains.chatfxapp.Command;

import java.io.IOException;

public class ChatClientApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClientApp.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("GB Chat Client");
        stage.setScene(scene);
        stage.show();
        ChatController controller = fxmlLoader.getController();
        stage.setOnCloseRequest(evetn -> controller.getClient().sendMessage(Command.END));
    }

    public static void main(String[] args) {
        launch();
    }
}