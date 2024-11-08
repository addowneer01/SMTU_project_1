package Main;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class Back implements Config, TypesMessage {
    public static Gson gson = new Gson();
    private Socket socket;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private BufferedReader reader;
    private OutputStream output;
    public JsonObject dataJson;
    static int idMsg;
    private final SocketHandler socketHandler = new SocketHandler();

    protected void start(){
        addLog("\n"+"Новый запуск",true);
        if (REFRESH_DATA){
            dataJson = new JsonObject();
            dataJson.add("msg",new JsonArray());
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
        executorService.scheduleAtFixedRate(socketHandler,0,500, TimeUnit.MILLISECONDS);
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
        executorService.shutdown();
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
                JsonObject json = new JsonObject();
                json.addProperty("t","P");
                send(json);
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
            //System.out.println("Отправленно: "+ms);
        }catch (IOException e){
            addLog("3"+e.getMessage());
        }
    }
    public boolean check(String nameProject, String  idDocument){
        if (!dataJson.has(nameProject)) return false;
        if (!dataJson.getAsJsonObject(nameProject).has(idDocument)) return false;
        return true;
    }

    public int getIdMsg(){
        try {
            Socket socketID = new Socket(IP_SERVER, PORT_ID);
            BufferedReader readerID = new BufferedReader(new InputStreamReader(socketID.getInputStream()));
            idMsg = Integer.parseInt(readerID.readLine());
        } catch (IOException e) {
            addLog(e.getMessage());
        }
        return idMsg;
    }
    public JsonObject getProject(String name){
        return dataJson.getAsJsonObject("projects").getAsJsonObject(name);
    }
    public JsonObject getDocument(String nameProject, String id){
        return getProject(nameProject).getAsJsonObject(id);
    }
    public JsonArray getDataMs(){
        return dataJson.get("msg").getAsJsonArray();
    }
    public abstract void addLog(String text, boolean silence);
    public abstract void addLog(String text);
    public abstract String getDataPath();
    public boolean handler(JsonObject object, boolean send) {
        //if (!send) System.out.println("handler");
        try {
            switch (object.get("type").getAsInt()){
                case TYPE_ANSWER -> {
                    JsonObject report = getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString())
                            .get("reports").getAsJsonObject().get(object.get("p1").getAsString()).getAsJsonObject();
                    report.addProperty("status",REPORT_CLOSE);
                    report.addProperty("ms2",object.get("p2").getAsString());
                    if (send) addMsg("Ответ на репорт №"+object.get("p1").getAsString(),object.get("p2").getAsString());
                }
                case TYPE_RELEASE -> {
                    JsonObject project = dataJson.getAsJsonObject("projects").getAsJsonObject(object.get("project").getAsString());
                    JsonObject document = new JsonObject();
                    document.addProperty("file",object.get("p1").getAsString());
                    document.addProperty("status",STATUS_WAITING);
                    document.add("reports",new JsonObject());
                    project.add(object.get("idDocument").getAsString(),document);
                    if (send) addMsg("Документ '"+object.get("idDocument").getAsString()+
                            "' проекта '"+object.get("project").getAsString()+"' закончен и ожидает подтверждения",
                            object.get("p2").getAsString());
                }
                case TYPE_CORRECTION -> {
                    JsonObject document = getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString());
                    document.addProperty("file",object.get("p1").getAsString());
                    document.addProperty("status",STATUS_WAITING);
                    if (send) addMsg("Документ '"+object.get("idDocument").getAsString()+
                                    "' проекта '"+object.get("project").getAsString()+"' не принят и ожидает корректировок",
                            object.get("p2").getAsString());
                }
                case TYPE_NEW_PROJECT -> {
                    dataJson.getAsJsonObject("projects").add(object.get("project").getAsString(),new JsonObject());
                    if (send) addMsg("Создан новый проект '"+object.get("project").getAsString()+"'",object.get("p1").getAsString());
                }
                //////////////////
                case TYPE_CONFIRM -> {
                    if (object.get("p1").getAsBoolean())
                        getDocument(object.get("project").getAsString(),
                                object.get("idDocument").getAsString())
                            .addProperty("status",STATUS_APPROVED);
                    if (send) addMsg("Документ '"+object.get("idDocument").getAsString()+
                                    "' проекта '"+object.get("project").getAsString()+"' принят и ожидает начала работы",
                            object.get("p2").getAsString());
                }
                case TYPE_START_WORK -> {
                    getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString())
                            .addProperty("status",STATUS_AT_WORK);
                    if (send) addMsg("Документ '"+object.get("idDocument").getAsString()+
                                    "' проекта '"+object.get("project").getAsString()+"' принят в работу",
                            object.get("p1").getAsString());
                }
                case TYPE_REPORT -> {
                    JsonObject report = new JsonObject();
                    report.addProperty("ms1",object.get("p2").getAsString());
                    report.addProperty("status",REPORT_OPEN);
                    getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString())
                            .get("reports").getAsJsonObject().add(object.get("p1").getAsString(),report);
                    if (send) addMsg("Документ '"+object.get("idDocument").getAsString()+
                                    "' проекта '"+object.get("project").getAsString()+"' ожидает ответа на репорт №"+
                                    object.get("p1").getAsString(), object.get("p2").getAsString());
                }
                case TYPE_MESSAGE -> {
                    dataJson.get("msg").getAsJsonArray().add(object.get("ms").getAsString());
                    if (!send) System.out.println(object.get("ms").getAsString());
                }
                default -> addLog("handler -> неккоректный тип");
            }
            flushData();
            if (send) {
                object.addProperty("t","M");
                object.addProperty("from",getFrom());
                socketHandler.addMs(object);
            }
            return true;
        }catch (Exception e){
            addLog(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public void addMsg(String action, String comment){
        int id = getIdMsg();
        String from;
        if (getFrom() == FACTORY) from = "Завод: ";
        else from = "Бюро :";
        String ms = id+" | "+from+action+" | "+comment;
        JsonObject object = new JsonObject();
        object.addProperty("type",TYPE_MESSAGE);
        object.addProperty("ms",ms);
        handler(object,true);
    }
    private class SocketHandler implements Runnable {
        static Queue<String> queue = new LinkedList<>();
        public void addMs(JsonObject ms){
            addMs(gson.toJson(ms));
        }
        public void addMs(String ms) {
            queue.add(ms);
        }
        @Override
        public void run() {
            Back.this.run(new Runnable() {
                @Override
                public void run() {
                    JsonObject f = new JsonObject();
                    f.addProperty("from",getFrom());
                    f.addProperty("t","P");
                    send(f);
                    while (!queue.isEmpty()){
                        send(queue.poll());
                    }
                    try {
                        String ms = reader.readLine();
                        //System.out.println(ms);
                        JsonArray data = gson.fromJson(ms, JsonArray.class);
                        for (int i = 0;i<data.size();i++){
                            handler(gson.fromJson(data.get(i).getAsString(), JsonObject.class),false);
                        }
                    }catch (Exception e){
                        addLog(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    protected abstract int getFrom();
}

