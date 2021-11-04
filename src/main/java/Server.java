import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {
    public static final int SERVER_PORT = 8511;
    public static List<LoggedUser> loggedUsers = new ArrayList<>();
    public static final String SIGN_IN = "{\"message\":\"Добро пожаловать\"}";
    public static final String SIGN_OUT = "{\"message\":\"Покинул этот бренный мир\"}";
    public static final String OUT = "{\"message\":\"/qqq\"}";
    public static LoggedUser now;
    public static List<LoggedUser> unLog = new ArrayList<>();
    public static Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(SERVER_PORT);
        while (true) {
            Socket socket = server.accept();
            Thread thread = new Thread(() -> work(socket));
            thread.start();
        }
    }

    private static void work(Socket socket) {
        try {
            boolean connect = true;
            InputStream inputStream = socket.getInputStream();
            byte[] bytes = new byte[12];
            inputStream.read(bytes, 0, 12);
            Helper.readHeader(bytes);
            int size = Helper.readMessageSizeHeader(bytes);
            byte[] utc = new byte[size];
            inputStream.read(utc);
            String utcString = new String(utc);

            byte[] bytesName = new byte[12];
            inputStream.read(bytesName, 0, 12);
            Helper.readHeader(bytesName);
            int sizeName = Helper.readMessageSizeHeader(bytesName);
            byte[] name = new byte[sizeName];
            inputStream.read(name);
            String nameString = new String(name);

            Message utcJson = gson.fromJson(utcString, Message.class);
            Message nameJson = gson.fromJson(nameString, Message.class);
            utcString = utcJson.getMessage();
            nameString = nameJson.getMessage();
            LoggedUser loggedUser = new LoggedUser(nameString, socket, ZoneId.of(utcString));
            loggedUsers.add(loggedUser);
            receiveToAll(nameString, SIGN_IN, new byte[0]);
            while (connect) {
                byte[] bytesMessage = new byte[12];
                inputStream.read(bytesMessage, 0, 12);
                size = Helper.readHeader(bytesMessage);
                int messageSize = Helper.readMessageSizeHeader(bytesMessage);
                byte[] fileInBytes = new byte[size];
                inputStream.read(fileInBytes);
                byte[] bufferMessage = new byte[messageSize];
                inputStream.read(bufferMessage);
                String message = new String(bufferMessage);
                if (message.equals(OUT)) {
                    loggedUsers.remove(loggedUser);
                    receiveToAll(nameString, SIGN_OUT, new byte[0]);
                    System.out.println("close");
                    socket.close();
                    return;
                } else {
                    receiveToAll(nameString, message, fileInBytes);
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void receiveToAll(String name, String message, byte[] file) throws IOException{
        unLog = new ArrayList<>();
        for (LoggedUser loggedUser : loggedUsers) {
            OutputStream outputStream = loggedUser.getSocket().getOutputStream();
            Message messageObject = gson.fromJson(message, Message.class);
            messageObject.setTime(LocalTime.now(loggedUser.getZoneId()).toString());
            messageObject.setName(name);
            now = loggedUser;
            try {
                outputStream.write(
                        Helper.concat(Helper.concat(Helper.createHeader(
                                gson.toJson(messageObject), false, false, false,
                                messageObject.getFile() != null, file.length), file), gson.toJson(messageObject).getBytes()));
            }
            catch (SocketException e){
                unLog.add(now);
            }
        }
        unLog.forEach((lu) -> loggedUsers.remove(lu));
    }

}
