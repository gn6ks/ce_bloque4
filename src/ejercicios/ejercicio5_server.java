package ejercicios;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ejercicio5_server {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(1234)) {

            System.out.println("Servidor escuchando en puerto 1234...");

            while (true) {
                Socket client = server.accept();
                System.out.println("Cliente conectado!");

                try (ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
                    Libro libro = (Libro) ois.readObject();
                    System.out.println("Mensaje recibido...");
                    System.out.println("Objeto recibido: " + libro.toString());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
