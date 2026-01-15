package Handlers.exams;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 🧠 Clase que maneja la lógica del servidor para un sistema de mensajería simple.
 * <p>
 * Cada instancia de esta clase se ejecuta en un hilo separado y atiende a un cliente (usuario)
 * que desea leer su bandeja de entrada o enviar un mensaje a otro usuario autorizado.
 * </p>
 */
public class ex8_bien_manejador implements Runnable {

    private Socket socket;

    private static final File usuariosAutenticados = new File("src/Exam_resources/Ej8_usuarios.data");

    private static final File archivoInboxData = new File("src/Exam_resources/Ej8_inbox_refs.data");

    private static final List<String> usuarios = new ArrayList<>();

    private static String usuario;

    private String inbox;

    /** 📁 Lista de archivos reales donde se almacenan los mensajes de cada inbox */
    private static final ArrayList<File> ficheros = new ArrayList<>(Arrays.asList(
            new File("src/Exam_resources/Ej8_inbox1.data"),
            new File("src/Exam_resources/Ej8_inbox2.data"),
            new File("src/Exam_resources/Ej8_inbox3.data")
    ));

    /**
     * 🔌 Constructor que recibe la conexión entrante del cliente.
     *
     * @param cliente El {@link Socket} asociado a la conexión TCP con el cliente.
     */
    public ex8_bien_manejador(Socket cliente) {
        this.socket = cliente;
    }

    /**
     * ⚙️ Método principal ejecutado por el hilo.
     * <p>
     * Realiza el flujo completo de autenticación, menú y acciones (leer/enviar).
     * </p>
     */
    @Override
    public void run() {
        try (
                // conexion que se cierra si se hace 'return;'
                Socket s = socket;
                // 📥 Flujo de lectura desde el cliente
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // 📤 Flujo de escritura hacia el cliente
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Paso 1: Recibir nombre de usuario
            usuario = br.readLine();
            if (usuario == null || usuario.trim().isEmpty()) {
                System.err.println("Nombre de usuario vacío o nulo.");
                return;
            }

            // Paso 2: Cargar datos del sistema
            cargarDatos();

            // Paso 3: Verificar autenticación
            if (!comprobarUsuarioAutenticado()) {
                System.err.println("El usuario '" + usuario + "' no está autenticado ❌");
                return;
            }

            // Paso 4: Enviar menú al cliente
            pw.println("(1). Leer Inbox");
            pw.println("(2). Enviar un mensaje");

            // Paso 5: Leer opción del cliente
            String opcionCliente = br.readLine();
            if (opcionCliente == null) return;

            // Acción: Leer inbox
            if ("OPCION_1".equals(opcionCliente)) {
                switch (inbox) {
                    case "inbox1" -> enviarInbox(ficheros.get(0), pw);
                    case "inbox2" -> enviarInbox(ficheros.get(1), pw);
                    case "inbox3" -> enviarInbox(ficheros.get(2), pw);
                    default -> System.err.println("Inbox desconocido para el usuario: " + usuario);
                }
                return;
            }

            // Acción: Enviar mensaje
            if ("OPCION_2".equals(opcionCliente)) {
                pw.println(usuarios); // Enviar lista de destinatarios válidos

                String mensajeEnviar = br.readLine();
                if (mensajeEnviar == null || !mensajeEnviar.contains(":")) {
                    System.err.println("Formato de mensaje inválido. Debe ser 'destinatario:mensaje' ❌");
                    return;
                }

                String[] contenido = mensajeEnviar.split(":", 2);
                String destinatario = contenido[0].trim();
                String cuerpoMensaje = contenido[1];

                File inboxDestino = obtenerInboxPorUsuario(destinatario);
                if (inboxDestino != null) {
                    escribirCuerpoMensajeInbox(cuerpoMensaje, inboxDestino);
                    System.out.println("✅ Mensaje enviado a '" + destinatario + "'");
                } else {
                    System.err.println("Destinatario '" + destinatario + "' no válido ❌");
                }
            }

        } catch (IOException e) {
            System.err.println("Error en la conexión con el cliente: " + e.getMessage());
        }
    }

    /**
     * 🗺️ Obtiene el archivo de inbox correspondiente a un destinatario.
     *
     * @param destinatario Nombre del usuario destinatario.
     * @return El {@link File} del inbox si existe, {@code null} si no.
     */
    private File obtenerInboxPorUsuario(String destinatario) {
        return switch (destinatario) {
            case "salva" -> ficheros.get(0);
            case "pascual" -> ficheros.get(1);
            case "raquel" -> ficheros.get(2);
            default -> null;
        };
    }

    /**
     * 📥 Carga los datos del sistema desde los archivos de configuración:
     * <ul>
     *   <li>Lista de usuarios válidos</li>
     *   <li>Asignación de inbox por usuario</li>
     * </ul>
     */
    public void cargarDatos() {
        usuarios.clear();

        // Cargar usuarios válidos (archivo separado por ';')
        ArrayList<String[]> lineasUsuarios = leerArchivoCSV(usuariosAutenticados);
        for (String[] linea : lineasUsuarios) {
            for (String u : linea) {
                if (u != null && !u.trim().isEmpty()) {
                    usuarios.add(u.trim());
                }
            }
        }

        // Asignar inbox al usuario actual (archivo separado por ':')
        ArrayList<String[]> lineasInbox = leerArchivoCustom(archivoInboxData);
        for (String[] linea : lineasInbox) {
            if (linea.length >= 2 && usuario.equals(linea[0].trim())) {
                inbox = linea[1].trim();
                return;
            }
        }
        inbox = null; // No se encontró asignación
    }

    /**
     * ✅ Verifica si el usuario actual está en la lista de usuarios autenticados.
     *
     * @return {@code true} si el usuario es válido, {@code false} en caso contrario.
     */
    public boolean comprobarUsuarioAutenticado() {
        return usuarios.contains(usuario);
    }

    /**
     * 📖 Lee un archivo donde los campos están separados por punto y coma (;).
     *
     * @param archivo Archivo a leer.
     * @return Lista de arreglos, uno por línea, con los campos separados.
     */
    public static ArrayList<String[]> leerArchivoCSV(File archivo) {
        ArrayList<String[]> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea.split(";"));
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al leer archivo CSV: " + archivo.getAbsolutePath());
            e.printStackTrace();
        }
        return lineas;
    }

    /**
     * 📝 Lee un archivo donde los campos están separados por dos puntos (:).
     *
     * @param archivo Archivo a leer.
     * @return Lista de arreglos, uno por línea, con los campos separados.
     */
    public static ArrayList<String[]> leerArchivoCustom(File archivo) {
        ArrayList<String[]> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea.split(":"));
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al leer archivo personalizado: " + archivo.getAbsolutePath());
            e.printStackTrace();
        }
        return lineas;
    }

    /**
     * 📤 Envía todo el contenido de un archivo de inbox al cliente.
     * <p>
     * Primero envía el número de mensajes, luego cada mensaje línea por línea.
     * </p>
     *
     * @param fichero Archivo del inbox a enviar.
     * @param pw      Flujo de salida hacia el cliente.
     */
    public static void enviarInbox(File fichero, PrintWriter pw) {
        int numLineas = leerLineasInboxUsuario(fichero);
        pw.println(numLineas);
        mensajesInboxUsuario(fichero, pw);
    }

    /**
     * 🔢 Cuenta el número de líneas (mensajes) en un archivo de inbox.
     *
     * @param ficheroLeer Archivo a contar.
     * @return Número de líneas.
     */
    public static int leerLineasInboxUsuario(File ficheroLeer) {
        int contador = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(ficheroLeer))) {
            while (br.readLine() != null) {
                contador++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al contar líneas del inbox", e);
        }
        return contador;
    }

    /**
     * 📩 Envía todas las líneas de un archivo de inbox al cliente.
     *
     * @param ficheroLeer Archivo a leer.
     * @param pw          Flujo de salida hacia el cliente.
     */
    public static void mensajesInboxUsuario(File ficheroLeer, PrintWriter pw) {
        try (BufferedReader br = new BufferedReader(new FileReader(ficheroLeer))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                pw.println(linea);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar mensajes del inbox", e);
        }
    }

    /**
     * ✍️ Escribe un nuevo mensaje en el archivo de inbox de un destinatario.
     * <p>
     * El formato guardado es: {@code remitente:mensaje}
     * </p>
     *
     * @param cuerpoMensaje Cuerpo del mensaje a guardar.
     * @param ruta          Archivo de destino (inbox del destinatario).
     */
    public static void escribirCuerpoMensajeInbox(String cuerpoMensaje, File ruta) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta, true))) {
            bw.newLine();
            bw.write(usuario + ":" + cuerpoMensaje);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en el inbox", e);
        }
    }
}