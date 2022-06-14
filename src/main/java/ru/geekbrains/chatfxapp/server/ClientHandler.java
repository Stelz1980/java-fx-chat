package ru.geekbrains.chatfxapp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private static final String SERVER_TO_TERMINATE = "/end";
    private  AuthService authService;
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
                    authenticate();
                    readMessages();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authenticate() {
        while (true) {
            try {
                final String message = in.readUTF();
                  if (message.startsWith("/auth")) {
                    final String[] split = message.split("\\p{Blank}+");
                    final String login = split[1];
                    final String password = split[2];
                    final String nick = authService.getNickByLoginAndPassword(login, password);
                      System.out.println("Ник " + nick);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nick);
                        this.nick = nick;
                        server.broadcast("Пользователь" + nick + " зашел в чат");
                        server.subscribe(this);
                        break;
                    } else {
                        sendMessage("Неверные логин и пароль");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage(SERVER_TO_TERMINATE);
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
            server.unsubscribe(this);
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

    public String getNick() {
        return nick;
    }
}
