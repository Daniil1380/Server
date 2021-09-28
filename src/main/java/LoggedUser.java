import java.net.Socket;
import java.time.ZoneId;
import java.util.TimeZone;

public class LoggedUser {
    private String name;
    private Socket socket;
    private ZoneId zoneId;

    public String getName() {
        return name;
    }

    public Socket getSocket() {
        return socket;
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public LoggedUser(String name, Socket socket, ZoneId zoneId) {
        this.name = name;
        this.socket = socket;
        this.zoneId = zoneId;
    }
}
