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

public abstract class Back implements Config, TypesMessage {
    public static Gson gson = new Gson();
    private Socket socket;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private BufferedReader reader;
    private OutputStream output;
    public JsonObject dataJson;
    static int idMsg;
    static FileWriter logsWriter;
    protected void start(){
        addLog("\n"+"Новый запуск");
        if (REFRESH_DATA){
            dataJson = new JsonObject();
            dataJson.add("msg",new JsonObject());
            dataJson.add("projects",new JsonObject());
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
    public void flushData(){
        try {
            FileWriter fileWriter = new FileWriter(getDataPath());
            fileWriter.write(gson.toJson(dataJson));
            fileWriter.close();
        } catch (IOException e) {
            addLog("Ошибка записи");
            throw new RuntimeException(e);
        }
    }
    public void stop(){
        flushData();
    }

    public boolean run(Runnable runnable){
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(Config.IP_SERVER, Config.PORT_SERVER), 5000);
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
    public JsonObject getProject(String name){
        return dataJson.getAsJsonObject("projects").getAsJsonObject(name);
    }
    public JsonObject getDocument(String nameProject, String id){
        return getProject(nameProject).getAsJsonObject(id);
    }
    public abstract void addLog(String text, boolean silence);
    public abstract void addLog(String text);
    public abstract String getDataPath();
    public boolean handler(JsonObject object) {
        try {
            switch (object.get("type").getAsInt()){
                case TYPE_ANSWER -> {
                    JsonObject report = getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString())
                            .get("reports").getAsJsonObject().get(object.get("p1").getAsString()).getAsJsonObject();
                    report.addProperty("status",REPORT_CLOSE);
                    report.addProperty("ms2",object.get("p1").getAsString());
                }
                case TYPE_RELEASE -> {
                    JsonObject project = dataJson.getAsJsonObject("projects").getAsJsonObject(object.get("project").getAsString());
                    JsonObject document = new JsonObject();
                    document.addProperty("file",object.get("p1").getAsString());
                    document.addProperty("status",STATUS_WAITING);
                    document.add("reports",new JsonObject());
                    project.add(object.get("idDocument").getAsString(),document);
                }
                case TYPE_CORRECTION -> {
                    JsonObject document = getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString());
                    document.addProperty("file",object.get("p1").getAsString());
                    document.addProperty("status",STATUS_WAITING);
                }
                case TYPE_NEW_PROJECT -> {
                    dataJson.getAsJsonObject("projects").add(object.get("project").getAsString(),new JsonObject());
                }
                //////////////////
                case TYPE_CONFIRM -> {
                    if (object.get("p1").getAsBoolean()) getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString())
                            .addProperty("status",STATUS_APPROVED);
                }
                case TYPE_START_WORK -> {
                    getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString())
                            .addProperty("status",STATUS_AT_WORK);
                }
                case TYPE_REPORT -> {
                    JsonObject report = new JsonObject();
                    report.addProperty("ms1",object.get("p2").getAsString());
                    report.addProperty("status",REPORT_OPEN);
                    getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString())
                            .get("reports").getAsJsonObject().add(object.get("p1").getAsString(),report);
                }
                default -> addLog("handler -> неккоректный тип");
            }
            flushData();
            //send(object);
            return true;
        }catch (Exception e){
            addLog(e.getMessage());
            return false;
        }
    }
}

