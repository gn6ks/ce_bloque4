package Clientes;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class calculate_client {
    public static void main(String[] args) {
        try (Socket client = new Socket("localhost", 5000); PrintWriter pw = new PrintWriter(client.getOutputStream(), true)) {

            String operator = ("*");
            String number1 = ("100");
            String number2 = ("2");
            String clientName = ("Biggy Chesse");
            pw.println(operator);
            pw.println(number1);
            pw.println(number2);
            pw.println(clientName);

            System.out.println("Mensaje enviado!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
