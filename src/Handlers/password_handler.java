package Handlers;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import Objects.Password;

public class password_handler implements Runnable {

    private Socket socket;

    public password_handler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());) {

            // Enviar objeto vacío
            Password password = new Password();
            oos.writeObject(password);
            oos.flush();

            // Recibir password del cliente
            Password received = (Password) ois.readObject();

            // Calcular hash
            String encrypted = toMD5(received.getPlainPassword());
            received.setEncryptedPassword(encrypted);

            // Enviar password con hash
            oos.writeObject(received);
            oos.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String toMD5(String input) {
        try {
            // Obtener instancia de MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Calcular el hash
            byte[] hashBytes = md.digest(input.getBytes());

            // Convertir los bytes a formato hexadecimal
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
