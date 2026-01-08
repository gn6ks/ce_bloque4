package Clientes.exams;

import Objects.exams.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ex4_client {

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in); Socket socket = new Socket("localhost", 1234); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());) {

            oos.flush();

            mostrarMenu();
            int opcion = leerOpcion(sc);

            switch (opcion) {
                case 1 -> primeraOpcion(sc, oos, ois);
                case 2 -> System.out.println("funcionalidad aun no programada");
                default -> System.out.println("❌ Opción inválida. Solo (1) o (2).");
            }


        } catch (Exception error) {
            error.printStackTrace();
        }

    }

    private static void mostrarMenu() {
        System.out.println("============ Sistema Cerrado ============");
        System.out.println("(1) Iniciar sesión");
        System.out.println("(2) Mostrar todos los usuarios/contraseñas");
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

    public static void primeraOpcion(Scanner sc, ObjectOutputStream oos, ObjectInputStream ois) throws Exception {

        iniciarSesion(sc, oos);

        Message mensajeRespuesta = (Message) ois.readObject();
        String respuestaServidor = mensajeRespuesta.getContent();

        if ("ERROR".equals(respuestaServidor)) {
            System.err.println("Ha habido un error en la autenticacion");
            System.err.println("Cerrando la conexion...");
            return;
        }

        System.out.print("Escriba el numero '" + respuestaServidor + "' para autenticarse: ");
        String numeroUsuario = sc.nextLine().trim();
        oos.writeObject(crearMensaje(numeroUsuario));

        Message contenidoFichero = (Message) ois.readObject();
        String contenido = contenidoFichero.getContent();

        if ("ERROR".equals(contenido)) {
            System.err.println("La respuesta ha fallado.");
            System.err.println("Cerrando la conexion...");
            return;
        }

        System.out.println("Autenticacion correcta.");
        System.out.println("Contenido de servidor '" + contenido + "'");
        oos.writeObject(crearMensaje("RECIBIDO"));

        return;
    }

    private static void iniciarSesion(Scanner sc, ObjectOutputStream oos) {

        System.out.print("usuario: ");
        String usuario = sc.nextLine().trim();
        System.out.print("contraseña numerica: ");
        String contrasenya = sc.nextLine().trim();
        try {
            oos.writeObject(crearMensaje(usuario));
            oos.writeObject(crearMensaje(contrasenya));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private static Message crearMensaje(String contenido) {
        Message msg = new Message();
        msg.setContent(contenido);
        return msg;
    }
}
