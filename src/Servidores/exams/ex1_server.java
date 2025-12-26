package Servidores.exams;

import Handlers.exams.ex1_handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ex1_server {
    public static void main (String[] args) {
        try (ServerSocket socket = new ServerSocket(1234);) {
            System.out.println("Servidor escuchando en puerto 1234...");
            while (true) {
                Socket client = socket.accept();
                System.out.println("Cliente conectado!");

                ex1_handler handler = new ex1_handler(client);

                Thread t = new Thread(handler);
                t.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
