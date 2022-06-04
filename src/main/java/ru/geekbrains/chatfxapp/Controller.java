package ru.geekbrains.chatfxapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    private  TextArea historyArea;
    @FXML
    private  TextField userAnswer;

    public void clickSendButton(ActionEvent actionEvent) {
        historyArea.appendText(userAnswer.getText() + "\n");
        userAnswer.clear();
    }
}