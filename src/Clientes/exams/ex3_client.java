package Clientes.exams;

import Objects.exams.Message;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * 🖥️ Cliente para el Ejercicio 3: recuperación/cambio de contraseña mediante pregunta secreta.
 * 📜 Flujo de interacción:
 * 1️⃣ Muestra un menú y permite elegir "Iniciar sesión" (opción 1).
 * 2️⃣ Solicita nombre de usuario y lo envía al servidor.
 * 3️⃣ Si el servidor responde "ERROR", termina.
 * 4️⃣ Si responde con una pregunta, la muestra y espera la respuesta del usuario.
 * 5️⃣ Envía la respuesta y espera confirmación ("200 OK" o "ERROR").
 * 6️⃣ Si es OK, pide la nueva contraseña (dos veces) y valida localmente.
 * 7️⃣ Envía la nueva contraseña y espera confirmación final.
 */
public class ex3_client {

    /**
     * 🚀 Método principal: inicia el cliente y establece conexión con el servidor.
     * ✅ Se conecta a {@code localhost:1234}.
     * 📥 Usa streams de objetos para comunicación binaria segura.
     */
    public static void main(String[] args) {
        try ( Scanner sc = new Scanner(System.in);
              Socket socket = new Socket("localhost", 1234);
              ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
              ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());) {

            oos.flush();

            mostrarMenu();
            int opcion = leerOpcion(sc);

            switch (opcion) {
                case 1 -> primeraOpcion(sc, oos, ois);
                case 2 -> System.out.println("⚠️ Función de registro aún no implementada.");
                default -> System.out.println("❌ Opción inválida. Solo (1) o (2).");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 📋 Muestra el menú principal al usuario.
     * ✅ Solo la opción 1 ("Iniciar sesión") está funcional.
     */
    private static void mostrarMenu() {
        System.out.println("============ Sistema Cerrado ============");
        System.out.println("(1) Iniciar sesión");
        System.out.println("(2) Mostrar todos los usuarios/contraseñas");
        System.out.println("=========================================");
    }

    /**
     * 🔢 Lee la opción del menú desde la entrada estándar.
     * ⚠️ Devuelve -1 si la entrada no es un número entero válido.
     *
     * @param sc Scanner para leer la entrada.
     * @return Entero con la opción elegida, o -1 si es inválida.
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
     * 🔐 Ejecuta el flujo completo de cambio de contraseña (opción 1 del menú).
     * ✅ Incluye validación local de repetición de contraseña.
     * ✅ Maneja respuestas "ERROR" y "200 OK" del servidor.
     *
     * @param sc  Scanner para interactuar con el usuario.
     * @param oos Stream de salida para enviar datos al servidor.
     * @param ois Stream de entrada para recibir datos del servidor.
     * @throws Exception si ocurre un error de E/S o de serialización.
     */
    public static void primeraOpcion(Scanner sc, ObjectOutputStream oos, ObjectInputStream ois) throws Exception {
        iniciarSesion(sc, oos, ois);

        Message mensajeServidor = (Message) ois.readObject();
        String contenidoServidor = mensajeServidor.getContent();

        if ("ERROR".equals(contenidoServidor)) {
            System.out.println("❌ Acceso denegado: Usuario no encontrado o no autorizado.");
            return;
        }

        System.out.print("🔒 Pregunta de seguridad: " + contenidoServidor + "\n→ Respuesta: ");
        String respuesta = sc.nextLine().trim();

        oos.writeObject(crearMensaje(respuesta));

        Message mensajeRespuesta = (Message) ois.readObject();

        if (!"200 OK".equals(mensajeRespuesta.getContent())) {
            System.out.println("❌ Respuesta secreta incorrecta.");
            return;
        }

        System.out.println("\n✅ Respuesta correcta. Ingrese su nueva contraseña.");
        System.out.print("Contrasenya nueva: ");
        String contrasenya = sc.nextLine().trim();
        System.out.print("Repita la contrasenya nueva: ");
        String contrasenyaRepetida = sc.nextLine().trim();

        if (contrasenya.equals(contrasenyaRepetida)) {
            oos.writeObject(crearMensaje(contrasenya));
        } else {
            System.out.println("❌ Las dos contrasenyas no son iguales.");
            return;
        }

        Message mensajeConfirmacionFinal = (Message) ois.readObject();

        if (mensajeConfirmacionFinal.getContent().trim().equals("200 OK")) {
            System.out.println("✅ Contraseña cambiada con éxito. 🔌 Cerrando conexión.");
        } else {
            System.out.println("⚠️ Hubo un error desconocido al cambiar la contraseña en el servidor.");
        }
    }

    /**
     * 🪪 Solicita el nombre de usuario y lo envía al servidor.
     *
     * @param sc  Scanner para leer la entrada.
     * @param oos Stream de salida al servidor.
     * @param ois No usado aquí, pero pasado por coherencia con el flujo.
     * @throws Exception si falla la escritura del objeto.
     */
    private static void iniciarSesion(Scanner sc, ObjectOutputStream oos, ObjectInputStream ois) throws Exception {
        System.out.print("Nombre de usuario: ");
        String usuario = sc.nextLine().trim();
        oos.writeObject(crearMensaje(usuario));
    }

    /**
     * 📦 Crea un nuevo objeto {@link Message} con el contenido especificado.
     *
     * @param contenido Texto que contendrá el mensaje.
     * @return Objeto Message listo para enviar.
     */
    private static Message crearMensaje(String contenido) {
        Message msg = new Message();
        msg.setContent(contenido);
        return msg;
    }
}