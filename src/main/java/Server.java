import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Server {
    public static final int SERVER_PORT = 50001;
    public static List<LoggedUser> loggedUsers = new ArrayList<>();
    public static final String HELLO = "Зарегистрируйтесь в системе:";
    public static final String YOUR_NAME = "Теперь тебя зовут: ";
    public static final String SIGN_IN = "{\"message\":\"Добро пожаловать\"}";
    public static final String SIGN_OUT = "{\"message\":\"Покинул этот бренный мир\"}";
    public static final String OUT = "{\"message\":\"/qqq\"}";
    public static final String FILE = "Файл передан:";

    public static void main (String[] args) throws IOException {
        ServerSocket server = new ServerSocket(SERVER_PORT);
        while (true){
            Socket socket = server.accept();
            Thread thread = new Thread(() -> work(socket));
            thread.start();
        }
    }

    private static void work(Socket socket){
        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter.println(HELLO);
            String name = bufferedReader.readLine();
            String utc = bufferedReader.readLine();
            printWriter.println(YOUR_NAME + name);
            receiveToAll(name, SIGN_IN);
            LoggedUser loggedUser = new LoggedUser(name, socket, ZoneId.of(utc));
            loggedUsers.add(loggedUser);
            while (true){
                String message = bufferedReader.readLine();
                if (message.equals(OUT)){
                    loggedUsers.remove(loggedUser);
                    receiveToAll(name, SIGN_OUT);
                    socket.close();
                    return;
                }
                else {
                    receiveToAll(name, message);
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void receiveToAll(String name, String message) throws IOException {
        Gson gson = new Gson();
        for (LoggedUser loggedUser : loggedUsers) {
                PrintWriter writer = new PrintWriter(loggedUser.getSocket().getOutputStream(), true);
                Message messageObject = gson.fromJson(message, Message.class);
                messageObject.setTime(LocalTime.now(loggedUser.getZoneId()).toString().substring(0,5));
                messageObject.setName(name);
                writer.println(gson.toJson(messageObject));
        }
    }
}
