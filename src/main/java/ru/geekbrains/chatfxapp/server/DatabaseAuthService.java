package ru.geekbrains.chatfxapp.server;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class DatabaseAuthService implements AuthService {
    private static Connection connection;
    private static PreparedStatement ps;

    public DatabaseAuthService() {
        try {
            connection  = DriverManager.getConnection("jdbc:sqlite:Users.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        String sql =  "SELECT login, password, nick FROM users WHERE login = ? AND password = ?";
        try {
            ps = connection.prepareStatement(sql);
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getString("nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateNick(String newNick, String oldNick) {
        String sql =  "UPDATE users SET nick = ? WHERE nick = ?";
        try {
            ps = connection.  prepareStatement(sql);
            ps.setString(1, newNick);
            ps.setString(2, oldNick);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Сервис аутентификации остановлен");
    }
}
