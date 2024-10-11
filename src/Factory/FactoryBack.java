package Factory;

import Main.Back;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;

public class FactoryBack extends Back{
    private static FactoryBack factoryBack = null;
    protected FileWriter logsWriter;
    private FactoryBack(){
        try {
            logsWriter = new FileWriter("src/Factory/Logs",true);
            start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static FactoryBack getInstance(){
        if (factoryBack == null) factoryBack = new FactoryBack();
        return factoryBack;
    }

    @Override
    public void addLog(String text) {
        System.out.println("Factory| "+ text);
        try {
            logsWriter.write(text);
            logsWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDataPath() {
        return "src/Factory/Data";
    }

    @Override
    public void handler(JsonObject object) {
        switch (object.get("type").getAsInt()){
            case TYPE_ANSWER -> {
                getDocument(object.get("project").getAsString(),object.get("idDocument").getAsString())
                        .getAsJsonArray("reports").get(object.get("idReport").getAsInt())
                        .getAsJsonObject().addProperty("answer",object.get("id").getAsInt());
            }
            case TYPE_RELEASE -> {
                JsonObject project = dataJson.getAsJsonObject("projects").getAsJsonObject(object.get("project").getAsString());
                JsonObject document = new JsonObject();
                document.addProperty("file",object.get("p1").getAsString());
                document.add("reports",new JsonArray());
                project.add(object.get("idDocument").getAsString(),document);
            }
            case TYPE_CORRECTION -> {
                JsonObject project = dataJson.getAsJsonObject("projects").getAsJsonObject(object.get("project").getAsString());
                project.getAsJsonObject(object.get("idDocument").getAsString()).addProperty("file",object.get("p1").getAsString());
            }
            case TYPE_NEW_PROJECT -> {
                dataJson.getAsJsonObject("projects").add(object.get("name").getAsString(),new JsonObject());
                addLog("файл создан");
            }
            default -> addLog("handler -> неккоректный тип");
        }
    }

}
