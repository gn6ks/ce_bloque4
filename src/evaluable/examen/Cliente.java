package evaluable.examen;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Cliente {

    public static void main(String[] args) {
        try (
                Scanner sc = new Scanner(System.in);
                Socket cliente = new Socket("localhost", 5001);
                PrintWriter pw = new PrintWriter(cliente.getOutputStream(), true);
                BufferedReader br = new BufferedReader(new InputStreamReader(cliente.getInputStream()))
        ) {

            System.out.printf("Usuario: ");
            String usuario = sc.nextLine();
            System.out.printf("Contraseña: ");
            String contrasenya = sc.nextLine();

            pw.println(usuario.toLowerCase());
            pw.println(contrasenya.toLowerCase());

            String primeraOpcion = br.readLine();

            System.out.println("====== Opciones ======");
            System.out.println(primeraOpcion);
            if ("root".equals(usuario) && "root".equals(contrasenya)) {
                String segundaOpcion = br.readLine();
                System.out.println(segundaOpcion);
            }
            System.out.println("====== Opciones ======");
            System.out.println("Escoga -> (1/2): ");
            int opcion = sc.nextInt();
            sc.nextLine();

            String respuestaServidor;

            do {
                switch (opcion) {
                    case 1 -> pw.println(opcion);
                    case 2 -> {
                        if ("root".equals(contrasenya) && "root".equals(usuario)) {
                            pw.println(opcion);
                        } else {
                            System.out.println("No eres root :c");
                        }
                    }
                    default -> {
                        System.err.println("Opcion invalida...");
                        System.err.println("Saliendo del menu...");
                        return;
                    }
                }
                respuestaServidor = br.readLine();
            } while ("403 FORBIDDEN".equals(respuestaServidor));

            if (opcion == 1) {
                System.out.printf("Palabra a buscar: ");
                String palabra = sc.nextLine();
                pw.println(palabra);
                String definicion = br.readLine();
                if (!"404 NOT FOUND".equals(definicion)) {
                    System.out.println(definicion);
                    System.err.println("Cerrando conexion...");
                    return;
                } else {
                    System.err.println("Palabra '" + palabra + "' no encontrada.");
                    System.err.println("Cerrando conexion...");
                    return;
                }
            }
            if (opcion == 2 && "root".equals(usuario) && "root".equals(contrasenya)) {
                System.out.printf("Palabra a buscar: ");
                String palabra = sc.nextLine();
                pw.println(palabra);
                System.out.printf("Definicion: ");
                String definicion = sc.nextLine();
                pw.println(definicion);

                String respuestaServidorOpcion2 = br.readLine();
                if ("OVERWRITE???".equals(respuestaServidorOpcion2)) {
                    System.out.printf("Confirmar OVERWRITE por nueva definicion '" + definicion + "' (Y/N): ");
                    String confirmar = sc.nextLine().toUpperCase();
                    if ("Y".equals(confirmar)) {
                        pw.println("YES");
                    } else if ("N".equals(confirmar)) {
                        System.err.println("Cerrando conexion...");
                        return;
                    } else {
                        System.err.println("Opcion invalida");
                        System.err.println("Cerrando conexion...");
                        return;
                    }
                }
                System.err.println("Cerrando conexion...");
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
