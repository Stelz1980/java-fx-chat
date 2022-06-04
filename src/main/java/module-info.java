module ru.geekbrains.chatfxapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.geekbrains.chatfxapp to javafx.fxml;
    exports ru.geekbrains.chatfxapp;
}