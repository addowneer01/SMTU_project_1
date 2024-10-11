package Ms;

import Bd.Document;
import Bd.Project;

import java.io.File;

public class bRelease extends Message{
    public bRelease(int idC, String namePersonalC, String nameProjectC, int idDocumentC, File fileC, String commentC) {
        super(idC, namePersonalC, nameProjectC, idDocumentC, TYPE_RELEASE);
        file=fileC;
        comment=commentC;
    }
    public bRelease(int idC, Project project, Document document, File fileC, String commentC){
        super(idC, project.namePersonal, project.name, document.id, TYPE_RELEASE);
        file=fileC;
        comment=commentC;
    }
    public String comment;
    public File file;
}
