package ejercicios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ejercicio3_server {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(1234)) {

            System.out.println("Servidor escuchando en puerto 1234...");

            while (true) {
                Socket client = server.accept();
                System.out.println("Cliente conectado!");

                try (BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                    String message = br.readLine();
                    System.out.println("Mensaje recibido: " + message);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
