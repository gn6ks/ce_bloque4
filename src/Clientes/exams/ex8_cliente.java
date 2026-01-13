package Clientes.exams;

import Objects.exams.Mensaje;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Cliente del ejercicio 8 que se conecta a un servidor para:
 * - Leer su bandeja de entrada (inbox)
 * - Enviar mensajes a otros usuarios autorizados
 * <p>
 * El cliente primero se conecta al servidor, envía su nombre de usuario,
 * y luego elige una de las dos opciones disponibles.
 */
public class ex8_cliente {

    private static String usuario;
    private static String direccion;
    private static int puerto;

    /**
     * Método principal del cliente.
     * Solicita conexión, envía usuario, muestra menú y ejecuta la opción elegida.
     *
     * @param args argumentos de la línea de comandos (no se usan).
     */
    public static void main(String[] args) {
        conexionPrincipal();

        try (Scanner sc = new Scanner(System.in); Socket socket = new Socket(direccion, puerto); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            oos.flush();

            // Enviar nombre de usuario al servidor
            oos.writeObject(crearMensaje(usuario));

            // Recibir las dos opciones del menú
            Mensaje primerMensajeServer = (Mensaje) ois.readObject();
            Mensaje segundoMensajeServer = (Mensaje) ois.readObject();
            mostrarMenu(primerMensajeServer.getContenido(), segundoMensajeServer.getContenido());

            int opcion = leerOpcion(sc);

            switch (opcion) {
                case 1 -> {
                    primeraOpcion(oos, ois);
                    System.err.println("Cerrando conexión...");
                    return;
                }
                case 2 -> {
                    segundaOpcion(sc, oos, ois);
                    System.err.println("Cerrando conexión...");
                    return;
                }
                default -> System.err.println("Opción inválida");
            }

        } catch (Exception e) {
            throw new RuntimeException("Error en el cliente", e);
        }
    }

    /**
     * Solicita al usuario los datos de conexión: dirección, puerto y nombre de usuario.
     */
    public static void conexionPrincipal() {
        Scanner sc = new Scanner(System.in);

        System.out.println("============= Menú Conexión =============");
        System.out.print("dirección: ");
        direccion = sc.nextLine().trim();
        System.out.print("puerto: ");
        puerto = sc.nextInt();
        sc.nextLine();
        System.out.print("usuario: ");
        usuario = sc.nextLine().trim();
        System.out.println("============= Menú Conexión =============");
    }

    /**
     * Muestra el menú de opciones recibido del servidor.
     *
     * @param opcionUno primera opción (ej: "(1). Leer Inbox").
     * @param opcionDos segunda opción (ej: "(2). Enviar un mensaje").
     */
    public static void mostrarMenu(String opcionUno, String opcionDos) {
        System.out.println("============ Sistema Cerrado ============");
        System.out.println(opcionUno);
        System.out.println(opcionDos);
        System.out.println("=========================================");
    }

    /**
     * Lee la opción seleccionada por el usuario y la convierte a número entero.
     *
     * @param sc el lector de entrada estándar.
     * @return el número de la opción, o -1 si la entrada no es válida.
     */
    public static int leerOpcion(Scanner sc) {
        System.out.print("→ Elija una opción: ");
        String input = sc.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1; // Opción inválida
        }
    }

    /**
     * Opción 1: Leer el inbox del usuario.
     * Recibe del servidor el número de mensajes y luego cada mensaje uno por uno.
     *
     * @param oos flujo de salida hacia el servidor.
     * @param ois flujo de entrada desde el servidor.
     * @throws IOException si ocurre un error de red.
     */
    public static void primeraOpcion(ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        oos.writeObject(crearMensaje("OPCION_1"));

        try {
            // Recibir número de mensajes
            Mensaje numeroLineasCliente = (Mensaje) ois.readObject();
            int numLineas = Integer.parseInt(numeroLineasCliente.getContenido());
            System.out.println("Se van a recibir '" + numLineas + "' mensajes.");

            // Recibir cada mensaje
            for (int i = 0; i < numLineas; i++) {
                Mensaje mensaje = (Mensaje) ois.readObject();
                System.out.println((i + 1) + ". " + mensaje.getContenido());
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Tipo de mensaje desconocido", e);
        }
    }

    /**
     * Opción 2: Enviar un mensaje a otro usuario.
     * El servidor envía la lista de usuarios posibles.
     * El cliente introduce el mensaje en formato "destinatario:mensaje".
     *
     * @param sc  lector de entrada estándar.
     * @param oos flujo de salida hacia el servidor.
     * @param ois flujo de entrada desde el servidor.
     * @throws IOException si ocurre un error de red.
     */
    public static void segundaOpcion(Scanner sc, ObjectOutputStream oos, ObjectInputStream ois) throws IOException {
        oos.writeObject(crearMensaje("OPCION_2"));

        try {
            // Recibir lista de usuarios posibles
            Mensaje usuariosPosibles = (Mensaje) ois.readObject();
            System.out.println("Usuarios posibles: " + usuariosPosibles.getContenido());
            System.out.print("Formato mensaje (usuarioDestino:cuerpo_del_mensaje): ");
            String mensajeEntero = sc.nextLine().trim();
            oos.writeObject(crearMensaje(mensajeEntero));
            System.out.println("✅ Mensaje enviado.");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Tipo de mensaje desconocido", e);
        }
    }

    /**
     * Crea un nuevo objeto Mensaje con el contenido especificado.
     *
     * @param contenido el texto que contendrá el mensaje.
     * @return un objeto Mensaje listo para enviar.
     */
    private static Mensaje crearMensaje(String contenido) {
        Mensaje msj = new Mensaje();
        msj.setContenido(contenido);
        return msj;
    }
}