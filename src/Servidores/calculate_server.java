package Servidores;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import Handlers.calculate_cHandler;

public class calculate_server {

    public static void main(String[] args) {
        try (ServerSocket socket = new ServerSocket(5000);) {
            System.out.println("Servidor escuchando en puerto 5000...");

            /**
             * normal calculations between 1-to-1 client/server information
             */
//            while (true) {
//                Socket client = socket.accept();
//                System.out.println("Cliente conectado!");
//
//                try (BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
//                    String operator = br.readLine();
//                    String number1 = br.readLine();
//                    String number2 = br.readLine();
//                    String clientName = br.readLine();
//
//                    Integer resultOfOperation = calculate(operator, number1, number2);
//                    System.out.println("calculations done for client: " + clientName + " -- result: " + resultOfOperation);
//                }
//            }

            /**
             * multiple calculations always on listen server, 1-Thread-for-client
             */
            while (true) {
                Socket client = socket.accept();
                System.out.println("Cliente conectado!");

                calculate_cHandler cHandler = new calculate_cHandler(client);
                Thread t = new Thread(cHandler);
                t.start();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}