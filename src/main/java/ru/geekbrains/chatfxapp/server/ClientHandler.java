package ru.geekbrains.chatfxapp.server;

import ru.geekbrains.chatfxapp.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class ClientHandler {
    private AuthService authService;
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
                    if (authenticate()) {
                        readMessages();
                    }
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean authenticate() {
        while (true) {
            try {
                final String message = in.readUTF();
                final Command command = Command.getCommand(message);
                if (command == Command.AUTH) {
                    final String[] params = command.parse(message);
                    final String login = params[0];
                    final String password = params[1];
                    final String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage(Command.ERROR, "Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage(Command.AUTHOK, nick);
                        this.nick = nick;
                        server.broadcast(Command.MESSAGE, "Пользователь " + nick + " зашел в чат");
                        server.subscribe(this);
                        return true;
                    } else {
                        sendMessage(Command.ERROR, "Неверные логин и пароль");
                    }
                } else if (command == Command.END) {
                         return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    public void closeConnection() {
        sendMessage(Command.END);
        try {
            sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() {
        try {
            while (true) {
                final String message = in.readUTF();
                Command command = Command.getCommand(message);
                if (command == Command.END) {
                    break;
                }
                if (command == Command.PRIVATE_MESSAGE) {
                    final String[] params = command.parse(message);
                    server.sendPrivateMessage(this, params[0], params[1]);
                    continue;
                }
                if (command == Command.NEW_NICK) {
                    final String[] params = command.parse(message);
                    if (!server.isNickBusy(params[0])) {
                        String oldNick = this.nick;
                        this.nick = params[0];
                        server.UpdateNickMessage(this, oldNick);
                    } else {
                        sendMessage(Command.ERROR, "Такой ник уже занят. Выберите другой");
                    }
                    continue;
                }
                server.broadcast(Command.MESSAGE, nick + ": " + command.parse(message)[0]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public AuthService getAuthService() {
        return authService;
    }
}
