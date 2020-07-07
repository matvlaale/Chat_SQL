package server;


import sun.rmi.runtime.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Formatter;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;


public class Server {
    private static final Logger logger = Logger.getLogger("");

    private List<ClientHandler> clients;
    private AuthService authService;
    public ExecutorService executorService = Executors.newFixedThreadPool(8);

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();
        ServerSocket server = null;
        Socket socket;

        final int PORT = 8189;

        try {
            Handler fileHandler = new FileHandler("server_log.log",true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.INFO);

            Handler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter());
            consoleHandler.setLevel(Level.ALL);

            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            logger.setLevel(Level.ALL);

            server = new ServerSocket(PORT);
            //System.out.println("Сервер запущен!");
            logger.log(Level.INFO, "Сервер запущен!");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился ");
                logger.log(Level.CONFIG, "Клиент подключился");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "ОШИБКА запуска сервера!");
            e.printStackTrace();
        } finally {
            try {
                executorService.shutdown();
                server.close();
                authService.disconnect();
                logger.log(Level.INFO, "Сервер остановлен!");
            } catch (IOException e) {
                logger.log(Level.WARNING, "ОШИБКА остановки сервера!");
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(String nick, String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(nick + ": " + msg);
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg) {
        String message = String.format("[ %s ] private [ %s ] : %s",
                sender.getNick(), receiver, msg);

        for (ClientHandler c : clients) {
            if (c.getNick().equals(receiver)) {
                c.sendMsg(message);
                if (!sender.getNick().equals(receiver)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }

        sender.sendMsg("not found user: " + receiver);
    }


    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthorized(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    private void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist ");

        for (ClientHandler c : clients) {
            sb.append(c.getNick()).append(" ");
        }
        String msg = sb.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    public void clientStatus(boolean authorized) {
        if (authorized) logger.log(Level.CONFIG, "Клиент авторизирован");
        else logger.log(Level.CONFIG, "Клиент отключился");
    }

    public void clientSentMessage() {
        logger.log(Level.FINE, "Клиент прислал сообщение");
    }

    public void clientSentCommand() {
        logger.log(Level.CONFIG, "Клиент прислал команду");
    }

    public void exceptionCH(String add) {
        logger.log(Level.WARNING, "ОШИБКА ClientHandler: " + add);
    }
}
