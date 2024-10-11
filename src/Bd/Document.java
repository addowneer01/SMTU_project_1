package Bd;

import Ms.fReport;

import java.io.File;
import java.util.ArrayList;

public class Document {
    public int id;
    public File file;
    public boolean status;
    public ArrayList<fReport> reports = new ArrayList<>();
    public Document(int idC,File fileC,boolean statusC){
        id = idC;
        file = fileC;
        status = statusC;
    }
}
