package Factory;

import Main.TypesMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Scanner;

public class FactoryVMain implements TypesMessage {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Введите имя пользователя");
        String namePersonal = scan.nextLine();
        FactoryBack.getInstance();
        boolean q = true;
        while (q){
            String ms = scan.nextLine();
            FactoryBack.getInstance().addLog(ms,true);
            String[] command = ms.split(" ");
            switch (command[0]){
                case "/id" -> System.out.println(FactoryBack.getInstance().getIdMsg());
                case "/debug" -> {
                    JsonObject d = new JsonObject();
                    try{
                        d.addProperty("type",command[1]);
                        d.addProperty("project",command[2]);
                        d.addProperty("idDocument",command[3]);
                        d.addProperty("p1",command[4]);
                        d.addProperty("p2",command[5]);
                    }catch (Exception e){}
                    FactoryBack.getInstance().handler(d,true);
                }
                case "/help" -> {
                    System.out.println("/help");
                    System.out.println("/auth {name}");
                    System.out.println("/exit");
                    System.out.println("/ping");
                    System.out.println("/send {type} {project} {idDocument} {parametr1} {parametr2} " );
                    System.out.println("    confirm -> {answer(true/false)} {comment}");
                    System.out.println("    report  -> {id} {comment}");
                    System.out.println("    start   -> {comment}");
                    System.out.println("/getMsHistory {quantity (0-all) }" );
                    System.out.println("/getProjects" );
                    System.out.println("/getProject {name}" );
                    System.out.println("/getDocument {project} {id}" );
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
                case "/send" -> {
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
                    out.addProperty("project",command[2]);
                    out.addProperty("idDocument",command[3]);
                    if (FactoryBack.getInstance().handler(out,true)) System.out.println("Успешно отправлено");
                    else System.out.println("Ошибка обработки БД");
                }
                case "/getMsHistory" -> {
                    int k;
                    if (command.length == 1) k = 5;
                    else k = Integer.parseInt(command[1]);
                    JsonArray data = FactoryBack.getInstance().getDataMs();
                    if (data.size()<k || k==0) k = data.size();
                    for (int i = data.size()-k;i<data.size();i++){
                        System.out.println(data.get(i).getAsString());
                    }
                }
                case "/getProjects"->{
                    JsonArray data = FactoryBack.getInstance().dataJson.get("projects").getAsJsonObject().get("names").getAsJsonArray();
                    System.out.println("Проекты: ");
                    for (int i = 0;i<data.size();i++) System.out.println("  "+data.get(i).getAsString());
                }
                case "/getProject" ->{
                    JsonArray data = FactoryBack.getInstance().getProject(command[1]).get("names").getAsJsonArray();
                    System.out.println("Документы: ");
                    for (int i = 0;i<data.size();i++) System.out.println("  "+data.get(i).getAsString());
                }
                case "/getDocument" ->{
                    JsonObject data = FactoryBack.getInstance().getDocument(command[1],command[2]);
                    String st;
                    if (data.get("status").getAsInt()==STATUS_AT_WORK) st = "в работе";
                    else if (data.get("status").getAsInt()==STATUS_APPROVED) st = "одобрен";
                    else st = "ожидает одобрения";
                    System.out.println("    Статус: "+st);
                    System.out.println("    Ссылка на файл: "+data.get("file").getAsString());
                }
                default -> System.out.println("Некорректная команда");
            }
        }
    }
}
