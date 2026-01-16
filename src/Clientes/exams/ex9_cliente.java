package Clientes.exams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ex9_cliente {

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in); Socket cliente = new Socket("localhost", 5001); // 📡 Conexión TCP
             PrintWriter pw = new PrintWriter(cliente.getOutputStream(), true); BufferedReader br = new BufferedReader(new InputStreamReader(cliente.getInputStream()))) {

            System.out.printf("Usuario: ");
            String usuario = sc.nextLine();
            System.out.printf("Contraseña: ");
            String contrasenya = sc.nextLine();

            pw.println(usuario + ":" + contrasenya);

            String estatusServidor = br.readLine();
            if ("ERROR".equals(estatusServidor)) {
                System.err.println("Usuario '" + usuario + "' no esta en la lista de usuarios.");
                return;
            }

            System.out.printf("Nº lineas a enviar a servidor: ");
            int numLineas = sc.nextInt();
            sc.nextLine();

            pw.println(numLineas);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < numLineas; i++) {
                System.out.printf("Nº " + (i + 1) + "/" + numLineas + ": ");
                String mensaje = sc.nextLine();
                pw.println(mensaje);
                sb.append(mensaje).append(":");
            }

            String mensajesRecibidosServidor = br.readLine();
            String[] mensajesRecibidos = mensajesRecibidosServidor.split(":");
            String mensajesEnviadosCliente = sb.toString();
            String[] mensajesEnviados = mensajesEnviadosCliente.split(":");

            boolean estanBienLasLineas = false;
            for (int i = 0; i < numLineas; i++) {
                if (mensajesEnviados[i].equals(mensajesRecibidos[i])) {
                    estanBienLasLineas = true;
                }
            }

            if (estanBienLasLineas) {
                pw.println("OK");
                System.out.println("Las lineas coinciden con las que ha recibido servidor ESTATUS 'OK'");
            } else {
                pw.println("ERROR");
            }

            System.err.println("Cerrando conexion...");
            return;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
