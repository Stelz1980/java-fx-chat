package ru.geekbrains.chatfxapp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ChatClient {
    private static final String SERVER_ADDR = "localhost";
    private static final int SERVER_PORT = 8189;
    private static final String SERVER_TO_TERMINATE = "/end";
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final ChatController controller;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket(SERVER_ADDR, SERVER_PORT);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                waitAuth();
                readMessage();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }

        }).start();
    }

    private void waitAuth() {
        while (true) {

            final String message;
            try {
                message = in.readUTF();
                if (message.startsWith("/authok")) {
                    final String[] split = message.split("\\p{Blank}+");
                    final String nick = split[1];
                    controller.addMessage("Успешная авторизация под ником " + nick);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            final String message = in.readUTF();
            if (message.equalsIgnoreCase(SERVER_TO_TERMINATE)) {
                break;
            }
            controller.addMessage(message);
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

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
