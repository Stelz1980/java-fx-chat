module ru.geekbrains.chatfxapp {
    requires javafx.controls;
    requires javafx.fxml;


    exports ru.geekbrains.chatfxapp.client;
    opens ru.geekbrains.chatfxapp.client to javafx.fxml;
}