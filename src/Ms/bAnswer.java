package Ms;

import Bd.Document;

import Bd.Project;

public class bAnswer extends Message{
    public bAnswer(int idC, String namePersonalC, String nameProjectC, int idDocumentC, int idRC, String commentC) {
        super(idC, namePersonalC, nameProjectC, idDocumentC, TYPE_ANSWER);
        idR=idRC;
        comment=commentC;
    }
    public bAnswer(int idC, Project project, Document document, int idRC, String commentC){
        super(idC, project.namePersonal, project.name, document.id, TYPE_ANSWER);
        idR=idRC;
        comment=commentC;
    }
    public String comment;
    public int idR;
}
