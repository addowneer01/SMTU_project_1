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
    public void addLog(String text, boolean silence) {
        if (!silence) System.out.println("Logs| "+ text);
        try {
            if (silence) logsWriter.write("    sin: ");
            logsWriter.write(text+"\n");
            logsWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addLog(String text) {
        addLog(text,false);
    }

    @Override
    public String getDataPath() {
        return "src/Factory/Data";
    }


}
