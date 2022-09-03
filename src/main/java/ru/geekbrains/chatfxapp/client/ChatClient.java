package ru.geekbrains.chatfxapp.client;

import javafx.application.Platform;
import ru.geekbrains.chatfxapp.Command;

import java.io.*;
import java.net.Socket;

import static java.lang.Thread.sleep;
import static ru.geekbrains.chatfxapp.Command.AUTHOK;
import static ru.geekbrains.chatfxapp.Command.END;


public class ChatClient {
    private static final String SERVER_ADDR = "localhost";
    private static final int SERVER_PORT = 8189;
    private static final int TIME_OUT = 60000;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final ChatController controller;
    private volatile String nick;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                if (waitAuth()) {
                    readMessage();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }).start();
        new Thread(() -> {
            try {
                sleep(TIME_OUT);
                if (nick == null) {
                    Platform.runLater(() -> controller.showError("Вы не смогли законнектиться за " + TIME_OUT/1000 + " секунд. Поэтому отключаемся"));
                    sendMessage(END);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private boolean waitAuth() {
        while (true) {
            final String message;
            try {
                message = in.readUTF();
                System.out.println(message);
                final Command command = Command.getCommand(message);
                final String[] params = command.parse(message);
                if (command == AUTHOK) {
                    nick = params[0];
                    controller.sendAuth(true);
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    return true;
                }
                if (command == Command.ERROR) {
                    Platform.runLater(() -> controller.showError(params[0]));
                    continue;
                }
                if (command == Command.END) {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            final String message = in.readUTF();
            final Command command = Command.getCommand(message);
            if (command == END) {

                controller.sendAuth(false);
                break;
            }
            final String[] params = command.parse(message);
            if (command == Command.ERROR) {
                String messageError = command.parse(message)[0];
                Platform.runLater(() -> controller.showError(messageError));
                continue;
            }
            if (command == Command.MESSAGE) {
                Platform.runLater(() -> controller.addMessage(command.parse(message)[0]));
            }
            if (command == Command.CLIENTS) {
                Platform.runLater(() -> controller.updateClientList(params));
            }
            if (command == Command.RESTORE_HISTORY) {
                Platform.runLater(() -> controller.downloadHistoryToMessageArea(command.parse(message)[0]));
            }
        }
    }

    private void closeConnection() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}
