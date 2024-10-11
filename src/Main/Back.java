package Main;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Back implements Settings {
    public static Gson gson = new Gson();
    private Socket socket;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private BufferedReader reader;
    private OutputStream output;
    public JsonObject dataJson;
    static int idMsg;
    static FileWriter logsWriter;
    protected void start(){
        if (REFRESH_DATA){
            dataJson = new JsonObject();
            dataJson.add("msg",new JsonArray());
            dataJson.add("projects",new JsonArray());
        }
        else {
            try {
                Scanner scan = new Scanner(new File(getDataPath()));
                dataJson = gson.fromJson(scan.nextLine(),JsonObject.class);
            } catch (FileNotFoundException e) {
                addLog("Неправильный патч файла");
                throw new RuntimeException(e);
            }
        }
    }
    protected void stop(){
        try {
            FileWriter fileWriter = new FileWriter(getDataPath());
            fileWriter.write(gson.toJson(dataJson));
        } catch (IOException e) {
            addLog("Ошибка записи");
            throw new RuntimeException(e);
        }
    }

    public boolean run(Runnable runnable){
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(Settings.IP_SERVER, Settings.PORT_SERVER), 5000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = socket.getOutputStream();
            runnable.run();
            socket.close();
            socket = null;
            return true;
        }catch (Exception e){
            addLog(e.getMessage());
            return false;
        }
    }
    public boolean connectPing(){
        return run(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
    public void send(JsonObject json){
        send(gson.toJson(json));
    }
    public void send(String ms){
        try {
            if (ENCRYPT) {
                try {
                    //ms = new String(AESUtil.encrypt(ms, key));
                } catch (Exception e) {
                    addLog("encrypt error");
                }
            }
            output.write(ms.getBytes());
            output.write('\n');
            output.flush();
        }catch (IOException e){
            addLog(e.getMessage());
        }
    }
    public boolean check(String nameProject, String  idDocument){
        if (!dataJson.has(nameProject)) return false;
        if (!dataJson.getAsJsonObject(nameProject).has(idDocument)) return false;
        return true;
    }
    public int getIdMsg(){
        run(new Runnable() {
            @Override
            public void run() {
                try {
                    send("id");
                    while (!reader.ready());
                    idMsg = Integer.parseInt(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return idMsg;
    }
    public abstract void addLog(String text);
    public abstract String getDataPath();
}

