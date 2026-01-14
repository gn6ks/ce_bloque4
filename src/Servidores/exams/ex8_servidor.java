package Servidores.exams;

import Handlers.exams.ex8_manejador;
import Handlers.exams.ex8_bien_manejador;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ex8_servidor {

//    public static void main(String[] args) {
//        try (ServerSocket servidor = new ServerSocket(5001);) {
//            System.out.println("Servidor escuchando en puerto 1234...");
//            while (true) {
//                Socket conexion = servidor.accept();
//                System.out.println("Cliente conectado!");
//
//                ex8_manejador manejador = new ex8_manejador(conexion);
//
//                Thread t = new Thread(manejador);
//                t.start();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(5001);) {
            System.out.println("Servidor escuchando en puerto 1234...");
            while (true) {
                Socket conexion = servidor.accept();
                System.out.println("Cliente conectado!");

                ex8_bien_manejador manejador = new ex8_bien_manejador(conexion);

                Thread t = new Thread(manejador);
                t.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
