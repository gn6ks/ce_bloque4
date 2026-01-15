package Clientes.exams;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * 💻 Cliente para interactuar con el servidor de mensajería.
 * <p>
 * Permite a un usuario autenticado:
 * <ul>
 *   <li>Leer su bandeja de entrada</li>
 *   <li>Enviar un mensaje a otro usuario válido</li>
 * </ul>
 * </p>
 */
public class ex8_bien_cliente {

    private static String usuario;
    private static String direccion;
    private static int puerto;

    /**
     * 🚀 Punto de entrada de la aplicación.
     * <p>
     * Solicita datos de conexión, se conecta al servidor y permite elegir una acción.
     * </p>
     *
     * @param args Argumentos de línea de comandos (no usados).
     */
    public static void main(String[] args) {
        conexionPrincipal(); // 👥 Pedir datos al usuario

        try (
                Scanner sc = new Scanner(System.in);
                Socket cliente = new Socket(direccion, puerto); // 📡 Conexión TCP
                PrintWriter pw = new PrintWriter(cliente.getOutputStream(), true);
                BufferedReader br = new BufferedReader(new InputStreamReader(cliente.getInputStream()))
        ) {
            // Enviar nombre de usuario al servidor
            pw.println(usuario);

            // Recibir menú
            String opcion1 = br.readLine();
            String opcion2 = br.readLine();
            if (opcion1 == null || opcion2 == null) {
                System.err.println("❌ Servidor no respondió correctamente.");
                return;
            }

            mostrarMenu(opcion1, opcion2);
            int opcion = leerOpcion(sc);

            switch (opcion) {
                case 1 -> {
                    primeraOpcion(pw, br);
                    System.out.println("✅ Cerrando conexión...");
                }
                case 2 -> {
                    segundaOpcion(sc, pw, br);
                    System.out.println("✅ Cerrando conexión...");
                }
                default -> System.err.println("❌ Opción inválida");
            }

        } catch (IOException e) {
            System.err.println("⚠️ Error de conexión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 📝 Solicita al usuario los datos de conexión: dirección, puerto y nombre de usuario.
     */
    public static void conexionPrincipal() {
        Scanner sc = new Scanner(System.in);
        System.out.println("============= Menú Conexión =============");
        System.out.print("dirección: ");
        direccion = sc.nextLine().trim();
        System.out.print("puerto: ");
        puerto = sc.nextInt();
        sc.nextLine(); // Limpiar el salto de línea pendiente
        System.out.print("usuario: ");
        usuario = sc.nextLine().trim();
        System.out.println("============= Menú Conexión =============");
    }

    /**
     * 🖥️ Muestra el menú recibido del servidor.
     *
     * @param opcionUno Primera línea del menú.
     * @param opcionDos Segunda línea del menú.
     */
    public static void mostrarMenu(String opcionUno, String opcionDos) {
        System.out.println("============ Sistema Cerrado ============");
        System.out.println(opcionUno);
        System.out.println(opcionDos);
        System.out.println("=========================================");
    }

    /**
     * 🔢 Lee y valida la opción seleccionada por el usuario.
     *
     * @param sc Scanner para entrada estándar.
     * @return Número de opción (1 o 2), o -1 si es inválida.
     */
    public static int leerOpcion(Scanner sc) {
        System.out.print("→ Elija una opción: ");
        String input = sc.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 📬 Acción: Leer la bandeja de entrada del usuario.
     * <p>
     * Recibe el número de mensajes y luego cada mensaje en formato {@code remitente:mensaje}.
     * </p>
     *
     * @param pw Flujo de escritura hacia el servidor.
     * @param br Flujo de lectura desde el servidor.
     */
    public static void primeraOpcion(PrintWriter pw, BufferedReader br) {
        pw.println("OPCION_1");

        try {
            String numStr = br.readLine();
            if (numStr == null) {
                System.err.println("❌ El servidor no envió el número de mensajes.");
                return;
            }

            int numLineas = Integer.parseInt(numStr);
            System.out.println("📬 Se van a recibir '" + numLineas + "' mensajes.");

            for (int i = 0; i < numLineas; i++) {
                String linea = br.readLine();
                if (linea == null) break;

                String[] partes = linea.split(":", 2);
                if (partes.length < 2) {
                    System.out.println("⚠️ Formato de mensaje desconocido: " + linea);
                    continue;
                }

                String remitente = partes[0];
                String contenido = partes[1];
                System.out.println("📨 Remitente: " + remitente + " | Mensaje: " + contenido);
            }
        } catch (IOException e) {
            System.err.println("❌ Error al leer mensajes: " + e.getMessage());
        }
    }

    /**
     * ✉️ Acción: Enviar un mensaje a otro usuario.
     * <p>
     * Solicita al usuario un mensaje en formato {@code destinatario:mensaje}.
     * </p>
     *
     * @param sc Scanner para entrada del mensaje.
     * @param pw Flujo de escritura hacia el servidor.
     * @param br Flujo de lectura desde el servidor.
     * @throws IOException Si ocurre un error de red.
     */
    public static void segundaOpcion(Scanner sc, PrintWriter pw, BufferedReader br) throws IOException {
        pw.println("OPCION_2");

        String usuariosPosibles = br.readLine();
        if (usuariosPosibles == null) {
            System.err.println("❌ El servidor no envió la lista de usuarios.");
            return;
        }

        System.out.println("👥 Usuarios posibles: " + usuariosPosibles);
        System.out.print("✏️ Formato mensaje (usuarioDestino:cuerpo_del_mensaje): ");
        String mensajeEntero = sc.nextLine().trim();
        pw.println(mensajeEntero);
        System.out.println("✅ Mensaje enviado.");
    }
}