package Handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class calculate_cHandler implements Runnable {
    private final Socket socket;

    public calculate_cHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String operator = br.readLine();
            String number1 = br.readLine();
            String number2 = br.readLine();
            String clientName = br.readLine();

            int resultOfOperation = calculate(operator, number1, number2);
            System.out.println("calculations done for client: " + clientName + " -- result: " + resultOfOperation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int extractNumber(String line)
    {
        int number;
        try {
            number = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            number = 0;
        }
        if (number >= 100000000) { number = 0; }
        return number;
    }

    public static int calculate(String op,String n1,String n2) {
        int result = 0;
        char symbol = op.charAt(0);
        int num1 = extractNumber(n1);
        int num2 = extractNumber(n2);
        if (symbol == '+') {
            result = num1 + num2;
        } else if (symbol == '-') {
            result = num1 - num2;
        } else if (symbol == '*') {
            result = num1 * num2;
        } else if (symbol == '/') {
            result = num1 / num2;
        } else {
            result = -1;
        }
        return result;
    }
}

