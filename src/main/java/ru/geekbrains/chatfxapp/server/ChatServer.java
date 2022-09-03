package ru.geekbrains.chatfxapp.server;

import lombok.extern.slf4j.*;
import ru.geekbrains.chatfxapp.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ChatServer {
    private static final int PORT = 8189;
    private final Map<String, ClientHandler> clients;

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             AuthService authService = new DatabaseAuthService()) {
            while (true) {
                log.info("Сервер запущен. Ждем подключения клиента ");
                final Socket socket;
                socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                log.info("Клиент подключился");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPrivateMessage(ClientHandler from, String nickTo, String message) {
        final ClientHandler clientTo = clients.get(nickTo);
        if (clientTo == null) {
            from.sendMessage(Command.ERROR, "Пользователь не авторизован");
            return;
        }
        clientTo.sendMessage(Command.MESSAGE, "От " + from.getNick() + ": " + message);
        from.sendMessage(Command.MESSAGE, "Участнику " + nickTo + ": " + message);
    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientList();
    }

    public boolean isNickBusy(String nick) {
        return clients.get(nick) != null;
    }

    public void unsubscribe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientList();
    }

    private void broadcastClientList() {
        String nicks = clients.values().stream().map(ClientHandler::getNick).collect(Collectors.joining(" "));
        broadcast(Command.CLIENTS, nicks);
    }

    public void broadcast(Command command, String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command, message);
        }
    }

    public void UpdateNickMessage(ClientHandler client, String oldNick) {
        String newNick = client.getNick();
        client.getAuthService().updateNick(newNick, oldNick);
        clients.remove(oldNick);
        clients.put(newNick, client);
        broadcastClientList();
        broadcast(Command.MESSAGE, "Пользователь под ником " + oldNick + " сменил свой ник на новый- " + client.getNick());
    }
}
