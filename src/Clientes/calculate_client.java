package Clientes;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class calculate_client {
    public static void main(String[] args) {
        try (Socket client = new Socket("localhost", 5000); PrintWriter pw = new PrintWriter(client.getOutputStream(), true)) {

            String operator = ("*");
            String number1 = ("2");
            String number2 = ("3");
            pw.println(operator);
            pw.println(number1);
            pw.println(number2);

            System.out.println("Mensaje enviado!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
