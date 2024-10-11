package Ms;

import Bd.Document;
import Bd.Project;

import java.io.File;

public class bCorrection extends Message{
    public bCorrection(int idC, String namePersonalC, String nameProjectC, int idDocumentC, File fileC, String commentC) {
        super(idC, namePersonalC, nameProjectC, idDocumentC, TYPE_CORRECTION);
        file=fileC;
        comment=commentC;
    }
    public bCorrection(int idC, Project project, Document document, File fileC, String commentC){
        super(idC, project.namePersonal, project.name, document.id, TYPE_CORRECTION);
        file=fileC;
        comment=commentC;
    }
    public String comment;
    public File file;
}
