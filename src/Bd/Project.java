package Bd;

import java.util.ArrayList;

public class Project {
    public String name;
    public String namePersonal;
    public ArrayList<Document> documents= new ArrayList<>();
    public Project(String nameC,String namePersonalC){
        name=nameC;
        namePersonal=namePersonalC;
    }
}
