package Bureau;

import Main.Back;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;

public class BureauBack extends Back {
    private static BureauBack bureauBack = null;
    protected FileWriter logsWriter;
    private BureauBack(){
        try {
            logsWriter = new FileWriter("src/Bureau/Logs",true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static BureauBack getInstance(){
        if (bureauBack == null) bureauBack = new BureauBack();
        return bureauBack;
    }

    @Override
    public void addLog(String text) {
        System.out.println("Bureau| "+ text);
        try {
            logsWriter.write(text);
            logsWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDataPath() {
        return "src/Bureau/Data";
    }

    @Override
    public void handler(JsonObject object) {

    }
}
