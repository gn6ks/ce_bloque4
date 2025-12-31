package Clientes.exams;

import Objects.exams.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ex2_handler {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Socket socket = new Socket("localhost", 1234);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            oos.flush();

            System.out.println("============ Sistema Cerrado ============");
            System.out.println("(1). Iniciar sesion");
            System.out.println("(2). Registrarse");
            System.out.println("============ Sistema Cerrado ============");
            System.out.println("Escoger opcion (1) / (2)    ");
            int opcion = sc.nextInt();

            switch (opcion) {
                case 1 -> {
                    Message msgLogin = new Message();

                    System.out.println("Usuario: ");
                    String user = sc.next();
                    System.out.println("Contraseña: ");
                    String password = sc.next();

                    msgLogin.setUser(user);
                    msgLogin.setPassword(password);
                    oos.writeObject(msgLogin);
                    oos.flush();

                    Message responseLogin = (Message) ois.readObject();
                    if ("ERROR".equals(responseLogin.getContent())) {
                        System.out.println("error en usuario o contraseña...");
                        System.out.println("cerrando conexion...");
                        return;
                    }

                    System.out.println("Inicio de sesion correcto");
                    System.out.println("Numero de lineas de texto a enviar:  ");
                    int numLines = sc.nextInt();

                    responseLogin.setContent(String.valueOf(numLines));
                    oos.writeObject(responseLogin);
                    oos.flush();

                    Message responseNumLines = (Message) ois.readObject();
                    if (!"PREPARED".equals(responseNumLines.getContent())) {
                        System.out.println("error en la conexion...");
                        System.out.println("cerrando conexion...");
                        return;
                    }

                    sc.nextLine();
                    for (int i = 0; i < numLines; i++) {
                        System.out.println("Mensaje a enviar: ");
                        String line = sc.nextLine();

                        Message msg = new Message();
                        msg.setContent(line);
                        oos.writeObject(msg);
                        oos.flush();
                    }

                    Message endClient = new Message();
                    endClient.setContent("END CLIENT");
                    oos.writeObject(endClient);
                    oos.flush();

                    Message endConnection = (Message) ois.readObject();
                    if ("END SERVER".equals(endConnection.getContent())) {
                        System.out.println("END SERVER mensaje recibido");
                        System.out.println("cerrando conexion...");
                        return;
                    }
                }
//                case 2 -> ;
                default -> System.out.println("invalid option, try again...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
