package Handlers.exams;

import Objects.exams.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 🧠 Manejador de cliente para el servidor del Ejercicio 2.
 * 📜 Protocolo de comunicación esperado (orden estricto):
 * 1️⃣ Cliente envía un objeto Message con usuario y contraseña → servidor valida.
 * 2️⃣ Si la autenticación es correcta, el cliente envía un Message con el número de líneas que enviará.
 * 3️⃣ Servidor responde "PREPARED" y empieza a recibir líneas (como objetos Message).
 * 4️⃣ El cliente envía líneas hasta que manda un Message con contenido "END CLIENT".
 * 5️⃣ El servidor guarda todas las líneas en un archivo y responde "END SERVER".
 * 6️⃣ Conexión se cierra.
 */
public class ex2_handler implements Runnable {

    private final Socket socket;
    private final List<String> usuarios = new ArrayList<>();
    private final List<String> contrasenas = new ArrayList<>();

    /**
     * Constructor: recibe el socket del cliente y lo guarda.
     *
     * @param client Socket de la conexión entrante.
     */
    public ex2_handler(Socket client) {
        this.socket = client;
    }

    /**
     * 🏃‍♂️ Método principal que se ejecuta en un hilo separado por cada cliente.
     * - Carga las credenciales autorizadas desde los ficheros.
     * - Autentica al usuario.
     * - Recibe y guarda el contenido enviado por el cliente.
     */
    @Override
    public void run() {
        try (
                Socket s = socket; // No cerramos el socket dos veces: solo lo "tomamos" aquí
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream())
        ) {
            // para asegurarse de que el OOS esta bien hecho
            oos.flush();

            cargarCredenciales();

            Message loginMsg = (Message) ois.readObject();
            String user = loginMsg.getUser();
            String password = loginMsg.getPassword();

            if (!credencialesValidas(user, password)) {
                oos.writeObject(crearMensaje("ERROR"));
                System.out.println("❌ Acceso denegado para usuario: " + user);
                return;
            }

            oos.writeObject(crearMensaje("200 OK"));
            System.out.println("✅ Usuario '" + user + "' autenticado correctamente.");

            Message numLinesMsg = (Message) ois.readObject();
            int numLineas = 0;
            try {
                numLineas = Integer.parseInt(numLinesMsg.getContent());
            } catch (NumberFormatException e) {
                System.err.println("⚠️ El cliente no envió un número válido de líneas.");
            }

            System.out.println("📨 Cliente '" + user + "' enviará aproximadamente " + numLineas + " líneas.");

            oos.writeObject(crearMensaje("PREPARED"));

            File archivoSalida = new File("contenido.txt");

            // ⚠️ Modo 'append' (true): añade al final del archivo, no lo sobrescribe.
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoSalida, true))) {

                int lineasRecibidas = 0;
                while (true) {
                    Message msg = (Message) ois.readObject();
                    String contenido = msg.getContent();

                    if ("END CLIENT".equals(contenido)) {
                        System.out.println("⏹️ Cliente terminó la transmisión.");
                        break;
                    }

                    writer.write(contenido);
                    writer.newLine();
                    lineasRecibidas++;
                    System.out.println("📝 Guardada línea " + lineasRecibidas + ": " + contenido);
                }

                // ✅ Confirmamos al cliente que hemos terminado
                oos.writeObject(crearMensaje("END SERVER"));
                System.out.println("✅ " + lineasRecibidas + " líneas guardadas en '" + archivoSalida.getName() + "'.");

            }

        } catch (IOException e) {
            System.err.println("🔌 Conexión cerrada abruptamente por el cliente.");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Clase 'Message' no encontrada. ¿El cliente usa el mismo classpath?");
        } catch (Exception e) {
            System.err.println("💥 Error inesperado en el manejador:");
            e.printStackTrace();
        }
    }

    /**
     * 📥 Carga los usuarios y contraseñas desde los ficheros de autorización.
     * ✅ Importante:
     * - Se asume que ambos ficheros tienen el MISMO NÚMERO de entradas.
     * - La entrada i del fichero de usuarios corresponde a la entrada i del fichero de contraseñas.
     * Ejemplo de ficheros:
     * Ej2_Usuarios_autorizados.txt:
     * <usuario>admin</usuario>
     * <usuario>user1</usuario>
     * Ej2_Contrasenyas_autorizadas.txt:
     * <contrasenya>root123</contrasenya>
     * <contrasenya>pass456</contrasenya>
     */
    private void cargarCredenciales() {
        usuarios.clear();
        contrasenas.clear();

        cargarFichero("src/Exam_resources/Ej2_Usuarios_autorizados.txt", "usuario", usuarios);
        cargarFichero("src/Exam_resources/Ej2_Contrasenyas_autorizadas.txt", "contrasenya", contrasenas);
    }

    /**
     * 📖 Método auxiliar para leer un fichero y extraer valores entre etiquetas XML-like.
     *
     * @param ruta     Ruta del fichero a leer (ej. "src/Exam_resources/Ej2_Usuarios_autorizados.txt")
     * @param etiqueta Nombre de la etiqueta a buscar (ej. "usuario" → busca <usuario>...</usuario>)
     * @param destino  Lista donde se guardarán los valores extraídos (ej. la lista 'usuarios')
     */
    private void cargarFichero(String ruta, String etiqueta, List<String> destino) {
        // Generamos las cadenas de apertura y cierre esperadas
        String apertura = "<" + etiqueta + ">";
        String cierre = "</" + etiqueta + ">";

        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int lineaNumero = 1; // Para mensajes de error más útiles

            while ((linea = br.readLine()) != null) {
                // Solo procesamos líneas que contengan AMBAS etiquetas (apertura y cierre)
                if (linea.contains(apertura) && linea.contains(cierre)) {
                    // Buscamos la posición de inicio del valor (justo después de la etiqueta de apertura)
                    int inicioTag = linea.indexOf(apertura);
                    int inicio = inicioTag + apertura.length();
                    // Buscamos el cierre de la etiqueta, empezando desde 'inicio'
                    int fin = linea.indexOf(cierre, inicio);

                    // Validamos que las posiciones sean válidas
                    if (inicioTag != -1 && fin != -1 && inicio < fin) {
                        String valor = linea.substring(inicio, fin).trim(); // .trim() elimina espacios sobrantes
                        destino.add(valor);
                    } else {
                        System.err.println("⚠️ Formato incorrecto en línea " + lineaNumero + " de '" + ruta + "': " + linea);
                    }
                }
                lineaNumero++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("❌ Fichero no encontrado: '" + ruta + "'. ¿Ruta correcta? ¿Está en el classpath?");
        } catch (IOException e) {
            System.err.println("❌ Error leyendo '" + ruta + "': " + e.getMessage());
        }
    }

    /**
     * 🔍 Valida que un par (usuario, contraseña) exista y esté en la misma posición en las listas.
     *
     * @param usuario    Nombre de usuario proporcionado por el cliente.
     * @param contrasena Contraseña proporcionada por el cliente.
     * @return true si existe un índice i tal que: usuarios[i] == usuario && contrasenas[i] == contrasena
     */
    private boolean credencialesValidas(String usuario, String contrasena) {
        // Comparamos hasta el mínimo de ambas listas (por si hay desfase)
        int max = Math.min(usuarios.size(), contrasenas.size());

        for (int i = 0; i < max; i++) {
            if (usuarios.get(i).equals(usuario) && contrasenas.get(i).equals(contrasena)) {
                System.out.println("🔑 Coincidencia encontrada en posición " + i + ": (" + usuario + ", " + contrasena + ")");
                return true;
            }
        }
        return false;
    }

    /**
     * 📦 Crea un nuevo objeto Message con el contenido indicado.
     *
     * @param contenido Texto que contendrá el mensaje.
     * @return Objeto Message listo para enviar.
     */
    private Message crearMensaje(String contenido) {
        Message msg = new Message();
        msg.setContent(contenido);
        return msg;
    }
}