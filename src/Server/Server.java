package Server;

import Main.Config;
import Main.TypesMessage;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Config, TypesMessage {
    static int countIdMsg;
    static FileWriter fileWriter;
    static Gson gson = new Gson();
    private static final Queue<String> queueToFactory = new LinkedList<>();
    private static final Queue<String> queueToBureau = new LinkedList<>();
    private static final ExecutorService executor = Executors.newFixedThreadPool(1);

    public static void main(String[] args) throws IOException {
        if (REFRESH_DATA) countIdMsg = 0;
        else {
            Scanner scan = new Scanner(new File(pathSdata));
            try {
                countIdMsg = scan.nextInt();
            }catch (Exception e){
                countIdMsg = 0;
            }
        }
        fileWriter = new FileWriter(pathSdata);
        ServerSocket serverSocket = new ServerSocket(Config.PORT_SERVER);
        executor.execute(new IDRunnable());
        while (true) {
            try{
                Socket socket = serverSocket.accept();
                //System.out.println("open");
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                int from = -1;
                while (socket.isConnected()){
                    String message = reader.readLine();
                    //System.out.println("Пришло: "+message);
                    //if (message.equals("id")) writer.println(getId());
                    JsonObject in = gson.fromJson(message, JsonObject.class);
                    from = in.get("from").getAsInt();
                    switch (in.get("t").getAsString()){
                        case "M" -> {
                            if (from == FACTORY) {
                                addQueueBureau(message);
                            }
                            else {
                                addQueueFactory(message);
                            }
                        }
                        //default -> writer.println(0);
                    }
                    if (!reader.ready())break;
                }
                if (from != -1) push(socket,reader,writer,from);
                socket.close();
                //System.out.println("close");
            }
//            catch (NullPointerException e){}
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static int getId() throws IOException {
        countIdMsg++;
        fileWriter = new FileWriter(pathSdata);
        fileWriter.write(String.valueOf(countIdMsg));
        fileWriter.close();
        return countIdMsg;
    }
    private static void push(Socket socket, BufferedReader reader, PrintWriter writer, int from) throws IOException {
        Queue<String> queue;
        if (from == FACTORY) queue = queueToFactory;
        else queue = queueToBureau;
        JsonArray object = new JsonArray();
        while (!queue.isEmpty()) object.add(queue.poll());
        writer.println(gson.toJson(object));
    }
    private static void addQueueFactory(String message){
        queueToFactory.add(message);
    }
    private static void addQueueBureau(String message){
        queueToBureau.add(message);
    }
    private static class IDRunnable implements Runnable{
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT_ID);
                while (true){
                    Socket socket = serverSocket.accept();
                    OutputStream output = socket.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println(getId());
                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
