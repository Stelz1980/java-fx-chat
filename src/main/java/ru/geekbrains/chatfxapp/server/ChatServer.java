package ru.geekbrains.chatfxapp.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static final int PORT = 8189;
    private final List<ClientHandler> clients;

    public ChatServer()
    {
        this.clients = new ArrayList<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             AuthService authService = new InMemoryAuthService()) {
            while (true) {
                System.out.println("Сервер запущен. Ждем подключения клиента ");
                final Socket socket;
                socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключился");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void sendPrivateMessage(String message, ClientHandler clientHandler) {
        try {
            final String[] split = message.split("\\p{Blank}+");
            final String nickFrom = split[0];
            final String nickTo = split[2];
            final String messageText = split[3];
            for (ClientHandler client : clients) {
                if (client.getNick().equalsIgnoreCase(nickTo)) {
                    client.sendMessage(nickFrom + ": " + messageText);
                }
            }
        }
        catch (Exception e) {
            clientHandler.sendMessage("Не смог отправить сообщение. Проверьте формат еще раз");
        }
    }

    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }
}
