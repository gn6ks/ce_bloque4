package ejercicios;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ejercicio3_client {
    public static void main(String[] args) {
        try (Socket client = new Socket("localhost", 1234); PrintWriter pw = new PrintWriter(client.getOutputStream(), true)) {

            String message = "lololololo";
            pw.println(message);

            System.out.println("Mensaje enviado!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
