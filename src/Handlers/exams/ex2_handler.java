package Handlers.exams;

import Objects.exams.Message;

import java.io.*;
import java.net.Socket;

/**
 * Manejador de cliente para el servidor.
 * Protocolo esperado:
 * 1. Recibe Message con user/password → valida contra ficheros.
 * 2. Si OK, recibe número de líneas → responde "PREPARED".
 * 3. Recibe mensajes hasta "END CLIENT".
 * 4. Responde "END SERVER" y cierra.
 */
public class ex2_handler implements Runnable {

    private final Socket socket;

    public ex2_handler(Socket client) {
        this.socket = client;
    }

    @Override
    public void run() {
        try (Socket s = socket; ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {

            // Flush inicial para handshake de ObjectOutputStream/ObjectInputStream
            oos.flush();

            // ——— Paso 1: Autenticación ———
            Message loginMsg = (Message) ois.readObject();
            String user = loginMsg.getUser();
            String password = loginMsg.getPassword();

            if (!usuarioValido(user) || !contrasenyaValida(password)) {
                oos.writeObject(crearMensaje("ERROR"));
                return;
            }

            oos.writeObject(crearMensaje("200 OK"));

            // ——— Paso 2: Recepción del número de líneas ———
            Message numLinesMsg = (Message) ois.readObject();
            int numLineas = 0;
            try {
                numLineas = Integer.parseInt(numLinesMsg.getContent());
            } catch (NumberFormatException ignored) { /* Se ignora si no es número */ }

            System.out.println("📨 Cliente '" + user + "' enviará " + numLineas + " líneas.");

            oos.writeObject(crearMensaje("PREPARED"));

            // ——— Paso 3: Recepción y guardado del contenido ———
            File archivoSalida = new File("contenido.txt");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida, true))) { // true = modo 'append'

                int lineasRecibidas = 0;
                while (true) {
                    Message msg = (Message) ois.readObject();
                    String contenido = msg.getContent();

                    if ("END CLIENT".equals(contenido)) {
                        System.out.println("⏹️ Cliente terminó la transmisión.");
                        break;
                    }

                    // Guardamos la línea recibida
                    writer.write(contenido);
                    writer.newLine();
                    lineasRecibidas++;
                    System.out.println("📝 Guardada línea " + lineasRecibidas + ": " + contenido);
                }

                // ——— Paso 4: Confirmación final ———
                oos.writeObject(crearMensaje("END SERVER"));
                System.out.println("✅ " + lineasRecibidas + " líneas guardadas en '" + archivoSalida.getName() + "'.");

            } // writer se cierra y vacía el buffer automáticamente

        } catch (IOException e) {
            System.err.println("🔌 Conexión cerrada abruptamente por el cliente.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Clase 'Message' no encontrada en el classpath.");
        } catch (Exception e) {
            System.err.println("💥 Error inesperado en el manejador:");
            e.printStackTrace();
        }
    }

    // ——————————————————————— Métodos de validación ———————————————————————

    /**
     * Verifica si el usuario existe en el fichero 'Ej2_Usuarios_autorizados.txt'.
     * Formato esperado en el fichero: <usuario>nombre</usuario> (una por línea).
     */
    private boolean usuarioValido(String usuario) {
        return buscarEnFichero(usuario, "src/Exam_resources/Ej2_Usuarios_autorizados.txt", "usuario");
    }

    /**
     * Verifica si la contraseña existe en el fichero 'Ej2_Contrasenyas_autorizadas.txt'.
     * Formato esperado: <contrasenya>clave</contrasenya>.
     */
    private boolean contrasenyaValida(String contrasenya) {
        return buscarEnFichero(contrasenya, "src/Exam_resources/Ej2_Contrasenyas_autorizadas.txt", "contrasenya");
    }

    /**
     * Método genérico para buscar un valor entre etiquetas XML-like en un fichero.
     *
     * @param valor       Valor a buscar (ej. "alice")
     * @param rutaFichero Ruta del archivo a leer
     * @param etiqueta    Nombre de la etiqueta (ej. "usuario")
     * @return true si se encuentra, false en caso contrario
     */
    private boolean buscarEnFichero(String valor, String rutaFichero, String etiqueta) {
        String apertura = "<" + etiqueta + ">";
        String cierre = "</" + etiqueta + ">";

        try (BufferedReader br = new BufferedReader(new FileReader(rutaFichero))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.contains(apertura) && linea.contains(cierre)) {
                    int inicio = linea.indexOf(apertura) + apertura.length();
                    int fin = linea.indexOf(cierre, inicio);
                    String valorFichero = linea.substring(inicio, fin).trim();
                    if (valorFichero.equals(valor)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error leyendo '" + rutaFichero + "': " + e.getMessage());
        }
        return false;
    }

    // ——————————————————————— Métodos auxiliares ———————————————————————

    /**
     * Crea un nuevo Message con el contenido especificado.
     */
    private Message crearMensaje(String contenido) {
        Message msg = new Message();
        msg.setContent(contenido);
        return msg;
    }
}