package server;

import java.sql.SQLException;

public interface AuthService {
    void connect() throws ClassNotFoundException, SQLException;
    void disconnect();
    String getNicknameByLoginAndPassword(String login, String password);
    boolean registration(String login, String password, String nickname);
    void changeNick(ClientHandler ch, String password, String newNick);
}
