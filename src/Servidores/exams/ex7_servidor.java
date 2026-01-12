package Servidores.exams;

import Handlers.exams.ex7_manejador;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ex7_servidor {

    public static void main(String[] args) {
        try (ServerSocket socket = new ServerSocket(1234);) {
            System.out.println("Servidor escuchando en puerto 1234...");
            while (true) {
                Socket cliente = socket.accept();
                System.out.println("Cliente conectado!");

                ex7_manejador manejador = new ex7_manejador(cliente);

                Thread t = new Thread(manejador);
                t.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
