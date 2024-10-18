package Factory;

import Main.TypesMessage;
import com.google.gson.JsonObject;

import java.util.Scanner;

public class FactoryVMain implements TypesMessage {
    public static void main(String[] args) {
        FactoryBack.getInstance();
        Scanner scan = new Scanner(System.in);
        System.out.println("Введите имя пользователя");
        String namePersonal = scan.nextLine();
        boolean q = true;
        while (q){
            String ms = scan.nextLine();
            FactoryBack.getInstance().addLog(ms,true);
            String[] command = ms.split(" ");
            switch (command[0]){
                case "/debug" -> {
                    JsonObject d = new JsonObject();
                    try{
                        d.addProperty("type",command[1]);
                        d.addProperty("project",command[2]);
                        d.addProperty("idDocument",command[3]);
                        d.addProperty("p1",command[4]);
                        d.addProperty("p2",command[5]);
                    }catch (Exception e){}
                    FactoryBack.getInstance().handler(d);
                }
                case "/help" -> {
                    System.out.println("/help");
                    System.out.println("/auth {name}");
                    System.out.println("/exit");
                    System.out.println("/ping");
                    System.out.println("/sendMs {type} {project} {idDocument} {parametr1} {parametr2} " );
                    System.out.println("    confirm -> {answer(true/false)} {comment}");
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
                case "/exit" -> {
                    q = false;
                    FactoryBack.getInstance().stop();
                }
                case "/ping" -> {
                    if (FactoryBack.getInstance().connectPing()) System.out.println("Сервер работает");
                    else System.out.println("Сервер недоступен");
                }
                case "/sendMS" -> {
                    boolean exception = false;
                    JsonObject out = new JsonObject();
                    out.addProperty("namePersonal",namePersonal);
                    try {
                        switch (command[1]){
                            case "confirm" -> {
                                out.addProperty("type",TYPE_CONFIRM);
                                out.addProperty("p1",command[4]);
                                out.addProperty("p2",command[5]);
                            }
                            case "report" -> {
                                out.addProperty("type",TYPE_REPORT);
                                out.addProperty("p1",command[4]);
                                out.addProperty("p2",command[5]);
                            }
                            case "start" -> {
                                out.addProperty("type",TYPE_START_WORK);
                                out.addProperty("p1",command[4]);
                            }
                            default -> {
                                System.out.println("Неккоректный тип (confirm, report, start)");
                                exception = true;
                            }
                        }
                    }catch (Exception e){
                        System.out.println("Неккоректные параметры");
                        exception = true;
                    }
                    if (FactoryBack.getInstance().check(command[2],command[3])) {
                        System.out.println("Проекта или документа не существует");
                        exception = true;
                    }
                    if (exception) break;
                    out.addProperty("id",FactoryBack.getInstance().getIdMsg());
                    out.addProperty("project",command[2]);
                    out.addProperty("document",command[3]);
                    //FactoryBack.getInstance().send(out);
                    if (FactoryBack.getInstance().handler(out)) System.out.println("Успешно отправленно");
                    else System.out.println("Ошибка обработки БД");
                }
                default -> System.out.println("Некорректная команда");
            }
        }
    }
}
