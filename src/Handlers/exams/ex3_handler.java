package Handlers.exams;

import Objects.exams.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * 🧠 Manejador de cliente para el servidor del Ejercicio 3.
 * 📜 Protocolo de comunicación esperado (orden estricto):
 * 1️⃣ Cliente envía un objeto Message con el nombre de usuario → servidor busca en su base.
 * 2️⃣ Si el usuario existe, el servidor responde con su pregunta secreta.
 *    Si no existe, responde "ERROR" y cierra la conexión.
 * 3️⃣ Cliente envía un Message con su respuesta secreta.
 * 4️⃣ Si la respuesta es correcta, servidor responde "200 OK".
 *    Si es incorrecta, responde "ERROR".
 * 5️⃣ Cliente envía la nueva contraseña (ya validada localmente).
 * 6️⃣ Servidor la escribe en el fichero correspondiente (usuario1.txt, usuario2.txt o usuario3.txt).
 * 7️⃣ Servidor confirma con "200 OK" y cierra la conexión.
 */
public class ex3_handler implements Runnable {

    private Socket socket;
    private List<String> usuarios = new ArrayList<>();
    private List<String> preguntas = new ArrayList<>();
    private List<String> respuestas = new ArrayList<>();
    private String contenido;
    private Integer numeroLinea;

    /**
     * Constructor: recibe el socket del cliente y lo guarda para su uso posterior.
     *
     * @param cliente Socket de la conexión entrante.
     */
    public ex3_handler(Socket cliente) {
        this.socket = cliente;
    }

    /**
     * 🏃‍♂️ Método principal que se ejecuta en un hilo separado por cada cliente.
     * - Carga los usuarios, preguntas y respuestas desde el fichero CSV.
     * - Valida el usuario recibido.
     * - Verifica la respuesta secreta.
     * - Actualiza la contraseña en el fichero correspondiente.
     */
    @Override
    public void run() {
        try (
                Socket s = socket;
                ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {

            oos.flush();

            Message MensajeUsuario = (Message) ois.readObject();
            String usuario = MensajeUsuario.getContent();
            cargarUsuarios_Respuestas();

            if (!comprobarUsuario(usuario)) {
                oos.writeObject(crearMensaje("ERROR"));
                System.out.println("❌ Acceso denegado para usuario: " + usuario);
                return;
            }

            oos.writeObject(crearMensaje(contenido));
            oos.flush();
            System.out.println("✅ Usuario '" + usuario + "' autenticado correctamente.");
            System.out.println("Pregunta de seguridad enviada, pendiente de responder...");

            Message mensajeRespuesta = (Message) ois.readObject();
            String respuestaUsuario = mensajeRespuesta.getContent();

            if (!comprobarRespuesta(respuestaUsuario)) {
                System.out.println("Respuesta secreta incorrecta");
                oos.writeObject(crearMensaje("ERROR"));
            }

            System.out.println("Respuesta secreta correcta");
            oos.writeObject(crearMensaje("200 OK"));
            oos.flush();

            Message mensajeNuevaContrasenya = (Message) ois.readObject();
            String nuevaContrasenya = mensajeNuevaContrasenya.getContent();

            switch (numeroLinea) {
                case 0 -> cambiarContrasenya(nuevaContrasenya, "src/Exam_resources/Ej3_usuario1.txt");
                case 1 -> cambiarContrasenya(nuevaContrasenya, "src/Exam_resources/Ej3_usuario2.txt");
                case 2 -> cambiarContrasenya(nuevaContrasenya, "src/Exam_resources/Ej3_usuario3.txt");
            }

            oos.writeObject(crearMensaje("200 OK"));
            oos.flush();

            System.err.println("Cerrando hilo de la conexion...");
            return;


        } catch (Exception e) {
            System.err.println("💥 Error inesperado en el manejador:");
            e.printStackTrace();
        }
    }

    /**
     * 📖 Lee un archivo CSV delimitado por punto y coma (;) y devuelve una lista con sus filas.
     * ✅ Cada fila se divide en campos usando {@code String.split(";")}.
     * ⚠️ No maneja comillas ni escapes —espera un formato simple: {@code usuario;pregunta;respuesta}.
     *
     * @param archivo El fichero CSV a leer (ej. "preguntas_secretas.txt").
     * @return Una lista de arreglos de cadenas, donde cada arreglo es una fila dividida en campos.
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
     * 📥 Carga los datos de usuarios, preguntas y respuestas desde el fichero CSV.
     * ✅ Se asume que el fichero tiene 3 columnas por línea:
     *    - Columna 0: nombre de usuario
     *    - Columna 1: pregunta secreta
     *    - Columna 2: respuesta secreta
     * ⚠️ Solo se procesan líneas con al menos 3 campos.
     * 🔁 Limpia las listas internas antes de cargar nuevos datos.
     */
    public void cargarUsuarios_Respuestas() {
        usuarios.clear();
        preguntas.clear();
        respuestas.clear();

        File archivoCSV = new File("src/Exam_resources/Ej3_preguntas_secretas.txt");
        ArrayList<String[]> contenidoArchivo = leerArchivoCSV(archivoCSV);

        for (String[] linea : contenidoArchivo) {
            if (linea.length >= 3) {
                usuarios.add(linea[0]);
                preguntas.add(linea[1]);
                respuestas.add(linea[2]);
            }
        }
    }

    /**
     * 🔍 Busca un usuario en la lista {@link #usuarios}.
     * ✅ Si lo encuentra:
     *    - Guarda su pregunta en {@link #contenido}
     *    - Guarda su índice (0, 1 o 2) en {@link #numeroLinea}
     *    - Devuelve {@code true}
     * ❌ Si no lo encuentra, devuelve {@code false}.
     *
     * @param usuario Nombre del usuario a buscar (debe coincidir exactamente).
     * @return {@code true} si el usuario está autorizado; {@code false} en caso contrario.
     */
    public boolean comprobarUsuario(String usuario) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).equals(usuario)) {
                System.out.println("🔑 Coincidencia encontrada en posición " + i + ": (" + usuario +  ")");
                contenido = preguntas.get(i);
                numeroLinea = i;

                return true;
            }
        }
        return false;
    }

    /**
     * ✅ Verifica si la respuesta del cliente coincide con la almacenada.
     * ⚠️ Requiere que {@link #comprobarUsuario(String)} se haya llamado antes
     *    para que {@link #numeroLinea} esté inicializado.
     *
     * @param respuesta Respuesta introducida por el cliente.
     * @return {@code true} si es correcta; {@code false} si no.
     */
    public boolean comprobarRespuesta(String respuesta) {
        if (respuestas.get(numeroLinea).equals(respuesta)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 💾 Sobrescribe el fichero de contraseña del usuario con la nueva contraseña.
     * ✅ Escribe EXACTAMENTE la cadena recibida (sin saltos de línea adicionales).
     * ⚠️ La asociación usuario ↔ fichero se hace por índice:
     *    - Índice 0 → Ej3_usuario1.txt
     *    - Índice 1 → Ej3_usuario2.txt
     *    - Índice 2 → Ej3_usuario3.txt
     *
     * @param contrasenya Nueva contraseña a almacenar.
     * @param ruta Ruta completa del fichero a sobrescribir.
     */
    public void cambiarContrasenya(String contrasenya, String ruta) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            bw.write(contrasenya);
            System.out.println("Contrasenya nueva almacenada correctamente");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 📦 Crea un nuevo objeto {@link Message} con el contenido especificado.
     *
     * @param contenido Texto que contendrá el mensaje.
     * @return Objeto Message listo para enviar a través del stream.
     */
    private Message crearMensaje(String contenido) {
        Message msg = new Message();
        msg.setContent(contenido);
        return msg;
    }
}