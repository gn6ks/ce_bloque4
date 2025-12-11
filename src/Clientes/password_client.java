package Clientes;

import Objects.Password;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class password_client {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try (Socket client = new Socket("localhost", 1234); ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(client.getInputStream());) {

            // Recibir objeto inicial
            Password password = (Password) ois.readObject();

            // Leer contraseña del usuario
            System.out.print("Password (NOT ENCRYPTED): ");
            String plainPassword = sc.next();
            password.setPlainPassword(plainPassword);

            // Enviar al servidor
            oos.writeObject(password);
            oos.flush();

            // Recibir password ya encriptado
            Password encrypted = (Password) ois.readObject();

            System.out.println("MD5: " + encrypted.getEncryptedPassword());

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }
}
