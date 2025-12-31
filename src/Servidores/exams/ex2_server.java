package Servidores.exams;

import Handlers.exams.ex2_handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ex2_server {
    public static void main (String[] args) {
        try (ServerSocket socket = new ServerSocket(1234);) {
            System.out.println("Servidor escuchando en puerto 1234...");
            while (true) {
                Socket client = socket.accept();
                System.out.println("Cliente conectado!");

                ex2_handler handler = new ex2_handler(client);

                Thread t = new Thread(handler);
                t.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
