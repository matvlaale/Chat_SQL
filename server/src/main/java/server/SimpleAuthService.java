package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private static Connection connection;
    public static Statement stmt;

    @Override
    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:server.db");
        stmt = connection.createStatement();
    }
    @Override
    public void disconnect(){
        try{
            stmt.close();
            connection.close();
            System.out.println("DataBase closed");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private class UserData{
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;

    private void loadData(){
        try {
            ResultSet data = stmt.executeQuery("SELECT * FROM users");
            while(data.next()){
                UserData newUD = new UserData(data.getString("login"),
                        data.getString("password"), data.getString("nick"));
                users.add(newUD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SimpleAuthService() {
        this.users = new ArrayList<>();

        try {
            connect();
            System.out.println("Connected");
            loadData();
            System.out.println("Loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 1; i <= 10 ; i++) {
            users.add(new UserData("login"+i, "pass"+i, "nick"+i));
        }

        for (int i = 1; i <= 3 ; i++) {
            users.add(new UserData(""+i, ""+i, "simple_nick"+i));
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (UserData o:users ) {
            if(o.login.equals(login) && o.password.equals(password)){
                return o.nickname;
            }
        }

        return null;
    }
    @Override
    public void changeNick(ClientHandler ch, String password, String newNick){
        String login = ch.getLogin();
        for (UserData o:users ) {
            if(o.login.equals(login) && o.password.equals(password)){
                o.nickname = newNick;
            }
        }
        try {
            stmt.executeUpdate(String.format("UPDATE users SET nick = '%s' " +
                    "WHERE login = '%s' AND password = '%s';", newNick, login, password));
            System.out.println("Nick changed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (UserData o:users ) {
            if(o.login.equals(login)) {
                return false;
            }
        }
        users.add(new UserData(login, password, nickname));
        try {
            stmt.executeUpdate(String.format("INSERT INTO users (login, password, nick) " +
                    "VALUES ('%s', '%s', '%s');", login, password, nickname));
            System.out.println("Registered");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }
}
