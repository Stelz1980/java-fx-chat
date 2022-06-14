package ru.geekbrains.chatfxapp.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InMemoryAuthService implements AuthService {

    private static class UserData {
        private final String nick;
        private final String Login;
        private final String password;

        public UserData(String nick, String login, String password) {
            this.nick = nick;
            Login = login;
            this.password = password;
        }

        public String getNick() {
            return nick;
        }

        public String getLogin() {
            return Login;
        }

        public String getPassword() {
            return password;
        }
    }

    private final List<UserData> users;

    public InMemoryAuthService() {
        users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            users.add(new UserData("nick" + i, "login" + i, "pass" + i));
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        for (UserData user : users) {
            if (login.equals(user.getLogin()) && password.equals(user.getPassword())) {
                return user.getNick();
            }
        }
        return null;
    }

    @Override
    public void close() {
        System.out.println("Сервис аутентификации остановлен");
    }
}
