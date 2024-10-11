package Factory;

import Ms.*;
import com.google.gson.JsonObject;

import java.util.Scanner;

public class FactoryVMain implements TypesMessage {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Введите имя пользователя");
        String namePersonal = scan.nextLine();
        boolean q = true;
        while (q){
            String[] command = scan.nextLine().split(" ");
            switch (command[0]){
                //case "/debug" -> System.out.println(FactoryBack.getInstance().getIdMsg());
                case "/help" -> {
                    System.out.println("/help");
                    System.out.println("/auth {name}");
                    System.out.println("/exit");
                    System.out.println("/ping");
                    System.out.println("/sendMs {namePersonal} {type} {project} {idDocument} {parametr1} {parametr2} " );
                    System.out.println("    confirm -> {answer} {comment}");
                    System.out.println("    report  -> {id} {comment}");
                    System.out.println("    start   -> {comment}");
                    System.out.println("/getMsHistory" );
                    System.out.println("/getMs {id}" );
                    System.out.println("/getProject" );
                    System.out.println("/getDocument {project} {id}" );
                    System.out.println("/getStatusDocument {project} {id}");
                    System.out.println("/startWork {namePersonal} {project} {id}");
                }
                case "/auth" -> namePersonal=scan.nextLine();
                case "/exit" -> q = false;
                case "/ping" -> {
                    if (FactoryBack.getInstance().connectPing()) System.out.println("Сервер работает");
                    else System.out.println("Сервер недоступен");
                }
                case "/sendMS" -> {
                    boolean exception = false;
                    JsonObject out = new JsonObject();
                    out.addProperty("namePersonal",namePersonal);
                    switch (command[2]){
                        case "confirm" -> {
                            out.addProperty("type",TYPE_CONFIRM);
                            out.addProperty("p1",command[5]);
                            out.addProperty("p2",command[6]);
                        }
                        case "report" -> {
                            out.addProperty("type",TYPE_REPORT);
                            out.addProperty("p1",command[5]);
                            out.addProperty("p2",command[6]);
                        }
                        case "start" -> {
                            out.addProperty("type",TYPE_START_WORK);
                            out.addProperty("p1",command[5]);
                        }
                        default -> {
                            System.out.println("Неккоректный тип (confirm, report, start)");
                            exception = true;
                        }
                    }
                    if (FactoryBack.getInstance().check(command[3],command[4])) {
                        System.out.println("Проекта или документа не существует");
                        exception = true;
                    }
                    if (exception) break;
                    out.addProperty("id",FactoryBack.getInstance().getIdMsg());
                    out.addProperty("project",command[3]);
                    out.addProperty("document",command[4]);
                    FactoryBack.getInstance().send(out);
                }
                default -> System.out.println("Некорректная команда");
            }
        }
    }
}
