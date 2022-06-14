package ru.geekbrains.chatfxapp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private static final String SERVER_TO_TERMINATE = "/end";
    private final AuthService authService;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ChatServer server;
    private String nick;

    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.authService = authService; 
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    readMessages();
                } finally {
                    closeConnection();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeConnection() {
        sendMessage(SERVER_TO_TERMINATE);
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readMessages() {
        try {
            while (true) {
                final String message = in.readUTF();
                if (message.equalsIgnoreCase(SERVER_TO_TERMINATE)) {
                    break;
                }
                server.broadcast(nick + ": " + message);
                out.writeUTF(message);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
