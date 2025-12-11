package Servidores;

import Handlers.password_handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class password_server {

    public static void main (String[] args) {
        try (ServerSocket socket = new ServerSocket(1234);) {
            System.out.println("Servidor escuchando en puerto 1234...");
            while (true) {
                Socket client = socket.accept();
                System.out.println("Cliente conectado!");

                password_handler pwsHandler = new password_handler(client);

                Thread t = new Thread(pwsHandler);
                t.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
