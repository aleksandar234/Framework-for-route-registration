package server;

import framework.init.Init;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static final int TCP_PORT = 8082;

    // Server koji radi na portu 8080
    // i ima vise razlicith threadova da moze vise ljudi istvremeno da koristi nas server
    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Init.init();
        try {
            // Slusam na tom portu
            ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("Server is running at http://localhost:"+TCP_PORT);
            while(true){
                // prihvatam nove zahteve na taj socket
                Socket socket = serverSocket.accept();
                // i da bi zasebno obradjivao otvorim novi thred gde se poziva metoda run preko runnable interfejsa
                // => ServerThread
                new Thread(new ServerThread(socket)).start();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
