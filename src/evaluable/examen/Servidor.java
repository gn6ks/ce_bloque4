package evaluable.examen;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(5001);) {
            System.out.println("Servidor escuchando en puerto 5001...");
            while (true) {
                Socket conexion = servidor.accept();
                System.out.println("Cliente conectado!");

                Multihilo manejador = new Multihilo(conexion);

                Thread t = new Thread(manejador);
                t.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
