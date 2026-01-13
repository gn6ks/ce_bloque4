package Handlers.exams;

import Objects.exams.Mensaje;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manejador de clientes para el ejercicio 8.
 * Cada instancia atiende a un único cliente conectado por socket.
 * Permite a usuarios autenticados leer su bandeja de entrada (inbox)
 * o enviar mensajes a otros usuarios autorizados.
 */
public class ex8_manejador implements Runnable {


    private Socket socket;

    private static File usuariosAutenticados = new File("src/Exam_resources/Ej8_usuarios.data");
    private static File archivoInboxData = new File("src/Exam_resources/Ej8_inbox_refs.data");

    private static List<String> usuarios = new ArrayList<>();

    private static String usuario;
    private String inbox;

    private static ArrayList<File> ficheros = new ArrayList<>(Arrays.asList(new File("src/Exam_resources/Ej8_inbox1.data"), new File("src/Exam_resources/Ej8_inbox2.data"), new File("src/Exam_resources/Ej8_inbox3.data")));

    public ex8_manejador(Socket cliente) {
        this.socket = cliente;
    }

    /**
     * Método principal del hilo. Gestiona toda la lógica del cliente:
     * - Recibe el nombre de usuario
     * - Verifica si está autorizado
     * - Muestra menú: leer inbox o enviar mensaje
     * - Ejecuta la opción elegida
     */
    @Override
    public void run() {
        try (Socket s = socket; ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {
            oos.flush();

            // Recibir nombre de usuario
            Mensaje mensajeUsuarioCliente = (Mensaje) ois.readObject();
            usuario = mensajeUsuarioCliente.getContenido();

            // Cargar datos de usuarios e inboxes
            cargarDatos();

            // Verificar si el usuario está autorizado
            if (!comprobarUsuarioAutenticado()) {
                System.err.println("El usuario '" + usuario + "' no está autenticado");
                return;
            }

            // Enviar opciones al cliente
            oos.writeObject(crearMensaje("(1). Leer Inbox"));
            oos.writeObject(crearMensaje("(2). Enviar un mensaje"));

            // Leer opción del cliente
            Mensaje opcionUsuarioCliente = (Mensaje) ois.readObject();

            if ("OPCION_1".equals(opcionUsuarioCliente.getContenido())) {
                // Enviar el contenido del inbox del usuario
                switch (inbox) {
                    case "inbox1" -> enviarInbox(ficheros.get(0), oos);
                    case "inbox2" -> enviarInbox(ficheros.get(1), oos);
                    case "inbox3" -> enviarInbox(ficheros.get(2), oos);
                }
                return;
            }

            if ("OPCION_2".equals(opcionUsuarioCliente.getContenido())) {
                // Enviar lista de usuarios posibles
                oos.writeObject(crearMensaje(usuarios.toString()));

                // Recibir mensaje en formato "destinatario:cuerpo"
                Mensaje mensajeEnviar = (Mensaje) ois.readObject();
                String[] contenido = mensajeEnviar.getContenido().split(":", 2); // Limitar a 2 partes

                if (contenido.length < 2) {
                    System.err.println("Formato de mensaje inválido.");
                    return;
                }

                String destinatario = contenido[0];
                String cuerpoMensaje = contenido[1];

                // Obtener el archivo del inbox del destinatario
                File inboxDestino = obtenerInboxPorUsuario(destinatario);
                if (inboxDestino != null) {
                    escribirCuerpoMensajeInbox(cuerpoMensaje, inboxDestino);
                    System.out.println("Mensaje enviado a '" + destinatario + "'");
                } else {
                    System.err.println("Destinatario '" + destinatario + "' no válido.");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error en el hilo del cliente", e);
        }
    }

    /**
     * Envía al cliente el número de mensajes y luego el contenido completo de un inbox.
     *
     * @param fichero archivo del inbox a enviar.
     * @param oos     flujo de salida hacia el cliente.
     * @throws IOException si ocurre un error al leer o escribir.
     */
    private void enviarInbox(File fichero, ObjectOutputStream oos) throws IOException {
        int numLineas = leerLineasInboxUsuario(fichero);
        oos.writeObject(crearMensaje(String.valueOf(numLineas)));
        mensajesInboxUsuario(fichero, oos);
    }

    /**
     * Devuelve el archivo del inbox correspondiente a un usuario dado.
     *
     * @param destinatario nombre del usuario receptor.
     * @return el archivo del inbox asociado, o null si no existe.
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
     * Carga desde los archivos:
     * - La lista de usuarios autorizados
     * - El inbox asignado al usuario actual
     */
    public void cargarDatos() {
        usuarios.clear();

        ArrayList<String[]> contenidoArchivo = leerArchivoCSV(usuariosAutenticados);
        ArrayList<String[]> contenidoFichero = leerArchivoCustom(archivoInboxData);

        // Cargar usuarios (archivo separado por ';')
        for (String[] linea : contenidoArchivo) {
            for (String u : linea) {
                usuarios.add(u.trim());
            }
        }

        // Asignar inbox al usuario actual (archivo separado por ':')
        for (String[] linea : contenidoFichero) {
            if (usuario.equals(linea[0].trim())) {
                inbox = linea[1].trim();
            }
        }
    }

    /**
     * Verifica si el usuario actual está en la lista de usuarios autorizados.
     *
     * @return true si el usuario está autorizado, false en caso contrario.
     */
    public boolean comprobarUsuarioAutenticado() {
        return usuarios.contains(usuario);
    }

    /**
     * Lee un archivo de texto separado por punto y coma (;).
     *
     * @param archivo el archivo a leer.
     * @return una lista de arreglos, donde cada arreglo representa una línea dividida por ';'.
     */
    public static ArrayList<String[]> leerArchivoCSV(File archivo) {
        ArrayList<String[]> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea.split(";"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineas;
    }

    /**
     * Lee un archivo de texto separado por dos puntos (:).
     *
     * @param archivo el archivo a leer.
     * @return una lista de arreglos, donde cada arreglo representa una línea dividida por ':'.
     */
    public static ArrayList<String[]> leerArchivoCustom(File archivo) {
        ArrayList<String[]> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea.split(":"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineas;
    }

    /**
     * Cuenta el número de líneas en un archivo de inbox.
     *
     * @param ficheroLeer el archivo cuyas líneas se quieren contar.
     * @return el número total de líneas.
     */
    public static int leerLineasInboxUsuario(File ficheroLeer) {
        int contador = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(ficheroLeer))) {
            while (br.readLine() != null) {
                contador++;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al contar líneas del inbox", e);
        }
        return contador;
    }

    /**
     * Envía todas las líneas de un archivo de inbox al cliente, una por una.
     *
     * @param ficheroLeer el archivo a leer.
     * @param oos         el flujo de salida hacia el cliente.
     */
    public static void mensajesInboxUsuario(File ficheroLeer, ObjectOutputStream oos) {
        try (BufferedReader br = new BufferedReader(new FileReader(ficheroLeer))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                oos.writeObject(crearMensaje(linea));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al enviar mensajes del inbox", e);
        }
    }

    /**
     * Escribe un nuevo mensaje en el archivo de un inbox.
     * El formato guardado es: "remitente:mensaje".
     *
     * @param cuerpoMensaje el contenido del mensaje a guardar.
     * @param ruta          el archivo del inbox donde se guardará.
     */
    public static void escribirCuerpoMensajeInbox(String cuerpoMensaje, File ruta) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta, true))) {
            bw.newLine();
            bw.write(usuario + ":" + cuerpoMensaje);
        } catch (Exception e) {
            throw new RuntimeException("Error al escribir en el inbox", e);
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