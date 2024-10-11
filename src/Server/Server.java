package Server;

import Main.Settings;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server implements Settings{
    static int countIdMsg;
    static FileWriter fileWriter;

    public static void main(String[] args) throws IOException {
        if (REFRESH_DATA) countIdMsg = 0;
        else {
            Scanner scan = new Scanner(new File("src/Server/Data"));
            countIdMsg = scan.nextInt();
        }
        fileWriter = new FileWriter("src/Server/Data");
        ServerSocket serverSocket = new ServerSocket(Settings.PORT_SERVER);
        while (true) {
            try{
                Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                String message = reader.readLine();
                if (message.equals("id")) writer.println(getId());
                else{
                    //
                }
                socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static int getId() throws IOException {
        countIdMsg++;
        fileWriter = new FileWriter("src/Server/Data");
        fileWriter.write(String.valueOf(countIdMsg));
        fileWriter.close();
        return countIdMsg;
    }
}
