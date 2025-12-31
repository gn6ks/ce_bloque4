package Clientes.exams;

import Objects.exams.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente para el sistema de envío de mensajes.
 * Protocolo:
 * 1. Envía credenciales (usuario/contraseña).
 * 2. Si OK, envía número de líneas.
 * 3. Envía cada línea como un Message.
 * 4. Envía "END CLIENT" → espera "END SERVER".
 */
public class ex2_client {

    public static void main(String[] args) {
        // Uso de try-with-resources: cierra Scanner, Socket, streams automáticamente.
        try (Scanner sc = new Scanner(System.in);
             Socket socket = new Socket("localhost", 1234);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            // Flush inicial: necesario para que ObjectInputStream en el servidor se inicialice correctamente.
            oos.flush();

            // --- Menú de selección ---
            mostrarMenu();
            int opcion = leerOpcion(sc);

            switch (opcion) {
                case 1 -> iniciarSesion(sc, oos, ois);
                case 2 -> System.out.println("⚠️ Función de registro aún no implementada.");
                default -> System.out.println("❌ Opción inválida. Solo (1) o (2).");
            }

        } catch (Exception e) {
            System.err.println("💥 Error en la ejecución del cliente:");
            e.printStackTrace();
        }
    }

    // ——————————————————————— Métodos auxiliares ———————————————————————

    private static void mostrarMenu() {
        System.out.println("============ Sistema Cerrado ============");
        System.out.println("(1) Iniciar sesión");
        System.out.println("(2) Registrarse");
        System.out.println("=========================================");
    }

    private static int leerOpcion(Scanner sc) {
        System.out.print("→ Elija una opción: ");
        String input = sc.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1; // Opción inválida
        }
    }

    private static void iniciarSesion(Scanner sc, ObjectOutputStream oos, ObjectInputStream ois) throws Exception {

        // ——— Fase 1: Autenticación ———
        System.out.print("Usuario: ");
        String user = sc.nextLine().trim();
        System.out.print("Contraseña: ");
        String password = sc.nextLine().trim();

        // Envía credenciales
        Message loginMsg = new Message();
        loginMsg.setUser(user);
        loginMsg.setPassword(password);
        oos.writeObject(loginMsg);
        oos.flush();

        // Recibe respuesta
        Message respuesta = (Message) ois.readObject();
        if ("ERROR".equals(respuesta.getContent())) {
            System.out.println("❌ Autenticación fallida: usuario o contraseña incorrectos.");
            return;
        }
        System.out.println("✅ Inicio de sesión correcto.");

        // ——— Fase 2: Envío del número de líneas ———
        System.out.print("Número de líneas a enviar: ");
        int numLineas;
        try {
            numLineas = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("❌ Número inválido. Abortando.");
            return;
        }

        // Envía número de líneas
        Message numMsg = new Message();
        numMsg.setContent(String.valueOf(numLineas));
        oos.writeObject(numMsg);
        oos.flush();

        // Espera confirmación del servidor
        Message confirmacion = (Message) ois.readObject();
        if (!"PREPARED".equals(confirmacion.getContent())) {
            System.out.println("⚠️ El servidor no está listo para recibir datos.");
            return;
        }

        // ——— Fase 3: Envío de cada línea ———
        for (int i = 1; i <= numLineas; i++) {
            System.out.printf("Línea %d/%d: ", i, numLineas);
            String linea = sc.nextLine();

            Message msg = new Message();
            msg.setContent(linea);
            oos.writeObject(msg);
            oos.flush(); // Asegura envío inmediato (útil en redes lentas)
        }

        // ——— Fase 4: Finalización ———
        Message finCliente = new Message();
        finCliente.setContent("END CLIENT");
        oos.writeObject(finCliente);
        oos.flush();

        // Espera confirmación final
        Message finServidor = (Message) ois.readObject();
        if ("END SERVER".equals(finServidor.getContent())) {
            System.out.println("✅ Conexión cerrada correctamente.");
        } else {
            System.out.println("⚠️ Respuesta inesperada al finalizar.");
        }
    }
}