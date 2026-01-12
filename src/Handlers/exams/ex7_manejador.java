package Handlers.exams;

import Objects.exams.Mensaje;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase que maneja la comunicación con un cliente en el servidor del ejercicio 7.
 * Cada cliente que se conecta tiene su propio hilo (Runnable) gestionado por esta clase.
 * Permite registrar usuarios, iniciar sesión y recibir líneas de texto que se guardan en un archivo.
 */
public class ex7_manejador implements Runnable {

    private Socket socket;
    private List<String> usuarios = new ArrayList<>();
    private List<String> contrasenyas = new ArrayList<>();
    private File archivo;
    private File archivoContenido = new File("C:\\Users\\pablo\\Documents\\_estudios\\_dam2\\ce_bloque4\\src\\Exam_resources\\Ej7_Usuarios_autorizados.txt");

    /**
     * Constructor que recibe el socket del cliente conectado.
     *
     * @param cliente el socket del cliente que se acaba de conectar al servidor.
     */
    public ex7_manejador(Socket cliente) {
        this.socket = cliente;
    }

    /**
     * Método principal del hilo. Gestiona toda la comunicación con el cliente:
     * - Recibe mensaje de inicio (registro o login)
     * - Valida credenciales
     * - Recibe líneas de texto y las guarda en un archivo único por usuario
     */
    @Override
    public void run() {
        try (Socket s = socket; ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {
            oos.flush();

            Mensaje mensajeInicio = (Mensaje) ois.readObject();
            String[] contenidoMensajeInicio = mensajeInicio.getContenido().split(";");

            cargarDatos();

            if ("REGISTRO".equals(contenidoMensajeInicio[0])) {
                registrarUsuario(contenidoMensajeInicio);
                return;
            }

            if (!comprobarInicioSesion(contenidoMensajeInicio[1], contenidoMensajeInicio[2])) {
                oos.writeObject(crearMensaje("401"));
                return;
            }

            oos.writeObject(crearMensaje("201"));

            Mensaje numeroLineasCliente = (Mensaje) ois.readObject();
            int numLineas = Integer.parseInt(numeroLineasCliente.getContenido());
            System.out.println("Se va a recibir '" + numLineas + "' líneas del cliente.");

            crearFichero(contenidoMensajeInicio[1]);

            oos.writeObject(crearMensaje("PREPARADO"));

            for (int i = 0; i < numLineas; i++) {
                Mensaje mensaje = (Mensaje) ois.readObject();
                System.out.println((i + 1) + ". " + mensaje.getContenido());

                if ("CIERRE".equals(mensaje.getContenido())) {
                    break;
                }
                escribirContenido(mensaje.getContenido());
            }

            System.out.println("Mensajes exportados correctamente en '" + archivo.getName() + "'.");

        } catch (Exception e) {
            throw new RuntimeException("Error en el hilo del cliente", e);
        }
    }

    /**
     * Registra un nuevo usuario añadiendo su nombre y contraseña al archivo de usuarios autorizados.
     *
     * @param contenido arreglo con [tipo, usuario, contraseña] del mensaje recibido.
     */
    public void registrarUsuario(String[] contenido) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoContenido, true))) {
            bw.newLine();
            bw.write(contenido[1] + ";" + contenido[2]);
            bw.newLine();

            System.out.println("Usuario '" + contenido[1] + "' dado de alta correctamente");
            System.out.println("Por favor, ejecute de nuevo el programa e inicie sesión.");
        } catch (Exception e) {
            throw new RuntimeException("Error al registrar usuario", e);
        }
    }

    /**
     * Lee un archivo de texto plano (CSV con separador ';') y devuelve sus líneas como arreglos de strings.
     *
     * @param archivo el archivo a leer.
     * @return una lista donde cada elemento es una línea dividida por ';'.
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
     * Carga los usuarios y contraseñas desde el archivo de autorizados a las listas internas.
     * Limpia las listas antes de cargar para evitar duplicados.
     */
    public void cargarDatos() {
        usuarios.clear();
        contrasenyas.clear();

        ArrayList<String[]> contenidoArchivo = leerArchivoCSV(archivoContenido);

        for (String[] linea : contenidoArchivo) {
            if (linea.length >= 2) { // Evitar errores si hay líneas vacías
                usuarios.add(linea[0]);
                contrasenyas.add(linea[1]);
            }
        }
    }

    /**
     * Comprueba si un par usuario/contraseña existe en las listas cargadas.
     *
     * @param usuario     el nombre de usuario a verificar.
     * @param contrasenya la contraseña a verificar.
     * @return true si las credenciales son válidas, false en caso contrario.
     */
    public boolean comprobarInicioSesion(String usuario, String contrasenya) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).equals(usuario) && contrasenyas.get(i).equals(contrasenya)) {
                System.out.println("Usuario '" + usuario + "' está en la lista de usuarios autorizados");
                return true;
            }
        }
        return false;
    }

    /**
     * Crea un archivo de texto único para el usuario, con su nombre y una marca de tiempo (ej: pablo_20260112_153045.txt).
     *
     * @param usuario el nombre del usuario autenticado.
     */
    public void crearFichero(String usuario) {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String nombreArchivo = usuario + "_" + ahora.format(formateador) + ".txt";
        archivo = new File(nombreArchivo);
    }

    /**
     * Escribe una línea de texto en el archivo del usuario (modo append).
     *
     * @param contenido la línea de texto a guardar.
     */
    public void escribirContenido(String contenido) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, true))) {
            bw.write(contenido);
            bw.newLine();
        } catch (Exception e) {
            throw new RuntimeException("Error al escribir en el archivo", e);
        }
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