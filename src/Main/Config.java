package Main;

public interface Config {
    boolean REFRESH_DATA = false;
    boolean ENCRYPT = false;
    String KEY = "";
    String IP_SERVER = "localhost";
    int PORT_SERVER = 2709;
    String pathFdata = "src/Factory/Data";
    String pathFlogs = "src/Factory/Logs";
    String pathBdata = "src/Bureau/Data";
    String pathBlogs = "src/Bureau/Logs";
}
