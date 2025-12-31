package Clientes.exams;

import Objects.exams.Message; // Clase para la transferencia de datos (debe ser Serializable).

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ex2_handler {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Intenta establecer la conexión con el servidor en "localhost" y puerto 1234.
        // Uso de try-with-resources para asegurar el cierre automático de todos los recursos (Socket, OOS, OIS).
        try (Socket socket = new Socket("localhost", 1234);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            // La llamada inicial a flush() es necesaria para enviar el encabezado de ObjectOutputStream,
            // permitiendo que ObjectInputStream del servidor se inicialice.
            oos.flush();

            // Interfaz de usuario simple para seleccionar la opción.
            System.out.println("============ Sistema Cerrado ============");
            System.out.println("(1). Iniciar sesion");
            System.out.println("(2). Registrarse");
            System.out.println("============ Sistema Cerrado ============");
            System.out.println("Escoger opcion (1) / (2)    ");
            int opcion = sc.nextInt();

            switch (opcion) {
                case 1 -> {
                    // --- FASE 1: Recolección de Credenciales e Intento de Autenticación ---
                    Message msgLogin = new Message();

                    System.out.println("Usuario: ");
                    String user = sc.next();
                    System.out.println("Contraseña: ");
                    String password = sc.next();

                    // Se cargan las credenciales en el objeto Message y se envían al servidor.
                    msgLogin.setUser(user);
                    msgLogin.setPassword(password);
                    oos.writeObject(msgLogin);
                    oos.flush(); // Se asegura que el mensaje de login se envíe inmediatamente.

                    // Se espera la respuesta del servidor sobre la autenticación.
                    Message responseLogin = (Message) ois.readObject();
                    if ("ERROR".equals(responseLogin.getContent())) {
                        System.out.println("error en usuario o contraseña...");
                        System.out.println("cerrando conexion...");
                        return; // Termina la ejecución si la autenticación falla.
                    }

                    // --- FASE 2: Preparación para el Envío de Contenido ---
                    System.out.println("Inicio de sesion correcto");
                    System.out.println("Numero de lineas de texto a enviar:  ");
                    int numLines = sc.nextInt();

                    // Se envía el número de líneas que se van a transmitir.
                    responseLogin.setContent(String.valueOf(numLines));
                    oos.writeObject(responseLogin);
                    oos.flush();

                    // Se espera la confirmación del servidor ("PREPARED") de que está listo para recibir el contenido.
                    Message responseNumLines = (Message) ois.readObject();
                    if (!"PREPARED".equals(responseNumLines.getContent())) {
                        System.out.println("error en la conexion...");
                        System.out.println("cerrando conexion...");
                        return;
                    }

                    // Se consume el salto de línea pendiente después de sc.nextInt().
                    sc.nextLine();

                    // --- FASE 3: Envío del Contenido Línea por Línea ---
                    for (int i = 0; i < numLines; i++) {
                        System.out.println("Mensaje a enviar: ");
                        String line = sc.nextLine();

                        // Cada línea de texto se encapsula en un nuevo objeto Message y se envía individualmente.
                        Message msg = new Message();
                        msg.setContent(line);
                        oos.writeObject(msg);
                        oos.flush(); // Se recomienda flush() después de cada objeto enviado para reducir latencia en la transferencia.
                    }

                    // --- FASE 4: Finalización de la Transmisión ---

                    // Se envía la señal al servidor para indicar que el cliente ha terminado de enviar datos.
                    Message endClient = new Message();
                    endClient.setContent("END CLIENT");
                    oos.writeObject(endClient);
                    oos.flush();

                    // Se espera la confirmación final del servidor ("END SERVER").
                    Message endConnection = (Message) ois.readObject();
                    if ("END SERVER".equals(endConnection.getContent())) {
                        System.out.println("END SERVER mensaje recibido");
                        System.out.println("cerrando conexion...");
                        return; // Cierre exitoso de la conexión.
                    }
                }
//                case 2 -> ; // Opción de registro no implementada.
                default -> System.out.println("invalid option, try again...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}