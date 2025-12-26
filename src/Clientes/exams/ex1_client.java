package Clientes.exams;

import Objects.exams.Message;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ex1_client {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Socket socket = new Socket("localhost", 1234); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            oos.flush();

            // 1. Enviar usuario
            System.out.print("User name: ");
            String user = sc.nextLine();

            Message msgUser = new Message();
            msgUser.setUser(user);
            oos.writeObject(msgUser);
            oos.flush();

            Message responseUser = (Message) ois.readObject();
            if ("ERROR".equals(responseUser.getContent())) {
                System.out.println("Usuario no autorizado");
                return;
            }

            // 2. Enviar contraseña
            System.out.print("Password: ");
            String pass = sc.nextLine();

            Message msgPass = new Message();
            msgPass.setPassword(pass);
            oos.writeObject(msgPass);
            oos.flush();

            Message responsePass = (Message) ois.readObject();
            if ("ERROR".equals(responsePass.getContent())) {
                System.out.println("Contraseña incorrecta");
                return;
            }

            // 3. Enviar PREPARADO
            Message prepared = new Message();
            prepared.setContent("PREPARADO");
            oos.writeObject(prepared);
            oos.flush();

            // 4. Recibir número de líneas
            Message numLinesMsg = (Message) ois.readObject();
            int numLines = Integer.parseInt(numLinesMsg.getContent());

            System.out.println("Número de líneas a recibir: " + numLines);

            // 5. Recibir líneas
            for (int i = 0; i < numLines; i++) {
                Message lineMsg = (Message) ois.readObject();
                System.out.println(lineMsg.getContent());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
