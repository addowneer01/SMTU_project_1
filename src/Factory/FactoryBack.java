package Factory;

import Main.Back;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;

public class FactoryBack extends Back{
    private static FactoryBack factoryBack = null;
    protected FileWriter logsWriter;
    private FactoryBack(){
        try {
            logsWriter = new FileWriter("src/Factory/Logs",true);
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
            case TYPE_ANSWER -> {}
            case TYPE_RELEASE -> {}

        }
    }
}
