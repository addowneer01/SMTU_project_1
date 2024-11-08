package Main;

public interface Config {
    boolean REFRESH_DATA = false;
    boolean ENCRYPT = false;
    String KEY = "";
    String IP_SERVER = "localhost";
    int PORT_SERVER = 9110;
    int PORT_ID = 9111;
    long PERIOD_UPDATE = 500; //milliseconds
    String pathFdata = "src/Factory/Data";
    String pathFlogs = "src/Factory/Logs";
    String pathBdata = "src/Bureau/Data";
    String pathBlogs = "src/Bureau/Logs";
    String pathSdata = "src/Server/Data";
}
