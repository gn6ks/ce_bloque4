package Clientes.exams;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import Objects.exams.Mensaje;

public class ex7_cliente {

    public static void main(String[] args) {

        try (Scanner sc = new Scanner(System.in); Socket socket = new Socket("localhost", 1234); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());) {

            oos.flush();

            mostrarMenu();
            int opcion = leerOpcion(sc);

            switch (opcion) {
                case 1 -> iniciarSesion(sc, oos);
                case 2 -> {
                    registrarUsuario(sc, oos);
                    return;
                }
                default -> System.err.println("Opcion invalida");
            }

            Mensaje mensajeCodigoInicio = (Mensaje) ois.readObject();
            if ("401".equals(mensajeCodigoInicio.getContenido())) {
                System.err.println("Error en el inicio de sesion / registro");
                return;
            }

            System.out.print("Numero de lineas a enviar a servidor: ");
            int numeroLineas = sc.nextInt();
            sc.nextLine();
            oos.writeObject(crearMensaje(String.valueOf(numeroLineas)));

            Mensaje mensajeServidorPreparacion = (Mensaje) ois.readObject();

            if (!"PREPARADO".equals(mensajeServidorPreparacion.getContenido())) {
                System.err.println("No hay preparacion por parte del servidor...");
                return;
            }

            for (int i = 0; i < numeroLineas; i++) {
                System.out.printf("Línea %d/%d: ", (i + 1), numeroLineas);
                String linea = sc.nextLine().trim();

                oos.writeObject(crearMensaje(linea));
                oos.flush();
            }

            oos.writeObject(crearMensaje("CIERRE"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

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

    public static void iniciarSesion(Scanner sc, ObjectOutputStream oos) {

        try {
            System.out.println("Datos de usuario (usuario/contraseña) inicio de sesion: ");
            System.out.print("Usuario: ");
            String usuario = sc.nextLine().trim();
            System.out.print("Contraseña: ");
            String contrasenya = sc.nextLine().trim();

            oos.writeObject(crearMensaje("LOGIN;" + usuario + ";" + contrasenya));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void registrarUsuario(Scanner sc, ObjectOutputStream oos) {

        try {
            System.out.println("Datos para dar de alta (usuario/contraseña)");
            System.out.print("Usuario: ");
            String usuario = sc.nextLine().trim();
            System.out.print("Contraseña: ");
            String contrasenya = sc.nextLine().trim();

            oos.writeObject(crearMensaje("REGISTRO;" + usuario + ";" + contrasenya));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static Mensaje crearMensaje(String contenido) {
        Mensaje msj = new Mensaje();
        msj.setContenido(contenido);
        return msj;
    }

}
