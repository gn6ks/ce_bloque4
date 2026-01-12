package Clientes.exams;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import Objects.exams.Mensaje;

/**
 * Cliente del ejercicio 7 que se conecta a un servidor para:
 * - Registrarse como nuevo usuario
 * - Iniciar sesión si ya está registrado
 * - Enviar varias líneas de texto que el servidor guardará en un archivo
 */
public class ex7_cliente {

    /**
     * Método principal del cliente.
     * Muestra un menú, permite elegir entre registrarse o iniciar sesión,
     * y luego envía líneas de texto al servidor.
     *
     * @param args argumentos de la línea de comandos (no se usan).
     */
    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in); Socket socket = new Socket("localhost", 1234); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            oos.flush();

            mostrarMenu();
            int opcion = leerOpcion(sc);

            switch (opcion) {
                case 1 -> iniciarSesion(sc, oos);
                case 2 -> {
                    registrarUsuario(sc, oos);
                    return; // Tras registrar, el cliente debe reiniciarse para iniciar sesión
                }
                default -> System.err.println("Opción inválida");
            }

            Mensaje mensajeCodigoInicio = (Mensaje) ois.readObject();
            if ("401".equals(mensajeCodigoInicio.getContenido())) {
                System.err.println("Error en el inicio de sesión / registro");
                return;
            }

            System.out.print("Número de líneas a enviar al servidor: ");
            int numeroLineas = sc.nextInt();
            sc.nextLine();

            oos.writeObject(crearMensaje(String.valueOf(numeroLineas)));

            Mensaje mensajeServidorPreparacion = (Mensaje) ois.readObject();
            if (!"PREPARADO".equals(mensajeServidorPreparacion.getContenido())) {
                System.err.println("El servidor no está preparado para recibir datos...");
                return;
            }

            // Enviar cada línea
            for (int i = 0; i < numeroLineas; i++) {
                System.out.printf("Línea %d/%d: ", (i + 1), numeroLineas);
                String linea = sc.nextLine().trim();
                oos.writeObject(crearMensaje(linea));
                oos.flush();
            }

            oos.writeObject(crearMensaje("CIERRE"));

        } catch (Exception e) {
            throw new RuntimeException("Error en el cliente", e);
        }
    }

    /**
     * Muestra el menú de opciones al usuario.
     */
    private static void mostrarMenu() {
        System.out.println("============ Sistema Cerrado ============");
        System.out.println("(1) Iniciar sesión");
        System.out.println("(2) Registrarse");
        System.out.println("=========================================");
    }

    /**
     * Lee la opción seleccionada por el usuario y la convierte a número entero.
     *
     * @param sc el lector de entrada estándar.
     * @return el número de la opción, o -1 si la entrada no es válida.
     */
    private static int leerOpcion(Scanner sc) {
        System.out.print("→ Elija una opción: ");
        String input = sc.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1; // Opción inválida
        }
    }

    /**
     * Solicita credenciales al usuario y envía un mensaje de tipo "LOGIN" al servidor.
     *
     * @param sc  el lector de entrada estándar.
     * @param oos el flujo de salida hacia el servidor.
     * @throws IOException si ocurre un error al enviar el mensaje.
     */
    public static void iniciarSesion(Scanner sc, ObjectOutputStream oos) throws IOException {
        System.out.println("Datos de usuario (usuario/contraseña) inicio de sesión: ");
        System.out.print("Usuario: ");
        String usuario = sc.nextLine().trim();
        System.out.print("Contraseña: ");
        String contrasenya = sc.nextLine().trim();

        oos.writeObject(crearMensaje("LOGIN;" + usuario + ";" + contrasenya));
    }

    /**
     * Solicita datos para un nuevo usuario y envía un mensaje de tipo "REGISTRO" al servidor.
     *
     * @param sc  el lector de entrada estándar.
     * @param oos el flujo de salida hacia el servidor.
     * @throws IOException si ocurre un error al enviar el mensaje.
     */
    public static void registrarUsuario(Scanner sc, ObjectOutputStream oos) throws IOException {
        System.out.println("Datos para dar de alta (usuario/contraseña)");
        System.out.print("Usuario: ");
        String usuario = sc.nextLine().trim();
        System.out.print("Contraseña: ");
        String contrasenya = sc.nextLine().trim();

        oos.writeObject(crearMensaje("REGISTRO;" + usuario + ";" + contrasenya));
    }

    /**
     * Crea un objeto Mensaje con el contenido especificado.
     *
     * @param contenido el texto que llevará el mensaje.
     * @return un nuevo objeto Mensaje listo para enviar.
     */
    private static Mensaje crearMensaje(String contenido) {
        Mensaje msj = new Mensaje();
        msj.setContenido(contenido);
        return msj;
    }
}