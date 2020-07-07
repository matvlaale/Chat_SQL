package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nick;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            server.executorService.execute(new Thread(() -> {
                try {
                    //Если в течении 5 секунд не будет сообщений по сокету то вызовится исключение
                    socket.setSoTimeout(3000);

                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/reg ")) {
                            String[] token = str.split(" ");

                            if (token.length < 4) {
                                continue;
                            }

                            boolean succeed = server
                                    .getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (succeed) {
                                sendMsg("Регистрация прошла успешно");
                            } else {
                                sendMsg("Регистрация  не удалась. \n" +
                                        "Возможно логин уже занят, или данные содержат пробел");
                            }
                        }

                        if (str.startsWith("/auth ")) {
                            String[] token = str.split(" ");

                            if (token.length < 3) {
                                continue;
                            }

                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);

                            login = token[1];

                            if (newNick != null) {
                                if (!server.isLoginAuthorized(login)) {
                                    sendMsg("/authok " + newNick);
                                    nick = newNick;
                                    server.subscribe(this);
                                    System.out.println("Клиент: " + nick + " подключился" + socket.getRemoteSocketAddress());
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMsg("С этим логином уже прошли аутентификацию");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }

                    server.clientStatus(true);

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            server.clientSentCommand();
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if (str.startsWith("/w ")) {
                                String[] token = str.split(" ", 3);

                                if (token.length < 3) {
                                    continue;
                                }

                                server.privateMsg(this, token[1], token[2]);
                            }
                            if (str.startsWith("/cn")) {
                                String[] token = str.split(" ", 3);
                                server.getAuthService().changeNick(this, token[2], token[1]);
                            }
                        } else {
                            server.clientSentMessage();
                            server.broadcastMsg(nick, str);
                        }
                    }
                } catch (SocketTimeoutException e) {
                    sendMsg("/end");
                }
                ///////
                catch (IOException e) {
                    server.exceptionCH("IOException во время работы!");
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    server.clientStatus(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        server.exceptionCH("IOException во время закрытия соединения!");
                        e.printStackTrace();
                    }
                }
            }));


        } catch (IOException e) {
            server.exceptionCH("IOException во время работы!");
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            server.exceptionCH("IOException во время отправки сообщения!");
            e.printStackTrace();
        }
    }

    public void changeNick(String newNick) {
        nick = newNick;
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }
}
