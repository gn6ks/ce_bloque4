package Handlers.exams;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 🧠 MANEJADOR DEL SERVIDOR (un hilo por cliente)
 *
 * Este código se ejecuta en el **servidor**.
 * Cada vez que un cliente se conecta, se crea una instancia de esta clase
 * para manejar su conexión de forma independiente (multihilo).
 *
 * 💡 Funcionalidad:
 * - Autentica al usuario con usuario:contraseña
 * - Recibe N líneas de texto
 * - Guarda esas líneas en un archivo único por usuario
 * - Confirma al cliente que los datos llegaron bien
 */
public class ex9_manejador implements Runnable {

    private Socket socket;
    private List<String> usuarios = new ArrayList<>();
    private List<String> contrasenyas = new ArrayList<>();
    private String usuario;          // Usuario autenticado
    private String nombreArchivo;    // Nombre del log que crearemos

    /**
     * 🛠️ Constructor: recibe la conexión (socket) del cliente
     */
    public ex9_manejador(Socket cliente) {
        this.socket = cliente;
    }

    /**
     * 🏃‍♂️ MÉTODO PRINCIPAL del hilo (se ejecuta cuando empieza el hilo)
     *
     * Flujo:
     * 1. Cargar lista de usuarios permitidos
     * 2. Leer credenciales del cliente
     * 3. Si es válido → OK, si no → ERROR y salir
     * 4. Recibir número de líneas
     * 5. Crear archivo de log con marca de tiempo
     * 6. Recibir cada línea y guardarla en el archivo
     * 7. Enviar confirmación al cliente
     * 8. Verificar si el cliente confirma que todo llegó bien
     */
    @Override
    public void run() {
        try (
                // 📥 Entrada y salida del socket (con autocierre)
                Socket s = socket;
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // 🔑 Paso 1: Cargar usuarios autorizados desde archivo
            cargarDatos();

            // 👤 Paso 2: Leer credenciales (formato: "usuario:contraseña")
            String usuario_contrasenya = br.readLine();

            // 🔒 Paso 3: Validar credenciales
            if (!comprobarUsuarioAutenticado(usuario_contrasenya)) {
                pw.println("ERROR"); // ❌ Acceso denegado
                return;
            }
            pw.println("OK"); // ✅ Bienvenido

            // 📥 Paso 4: Leer cuántas líneas va a enviar el cliente
            int numeroLineas = Integer.parseInt(br.readLine());
            System.out.println("Nº a recibir '" + numeroLineas + "'.");

            // 📄 Paso 5: Crear nombre único para el archivo de log
            crearFicheroUsuario();

            StringBuilder sb = new StringBuilder();

            // 📥 Paso 6: Recibir cada línea y guardarla
            for (int i = 0; i < numeroLineas; i++) {
                String mensaje = br.readLine();
                System.out.println("Nº " + (i + 1) + "/" + numeroLineas + ": " + mensaje);
                sb.append(mensaje).append(":");
                guardarMensajeFichero(mensaje); // 💾 Guardar en disco
            }

            // ✅ Paso 7: Enviar todas las líneas recibidas de vuelta (para verificación)
            pw.println(sb);

            // 📬 Paso 8: Esperar confirmación del cliente
            String estatusCliente = br.readLine();
            if ("ERROR".equals(estatusCliente)) {
                System.err.println("Las lineas NO coinciden con las que se ha enviado a cliente ESTATUS '" + estatusCliente + "'");
            } else {
                System.out.println("Las lineas coinciden con las que se ha enviado a cliente ESTATUS '" + estatusCliente + "'");
            }
            System.err.println("Cerrando conexion...");

        } catch (Exception e) {
            throw new RuntimeException("⚠️ Error en el manejador del cliente", e);
        }
    }

    /**
     * 📂 CARGAR USUARIOS AUTORIZADOS
     * Lee el archivo 'Ej9_Usuarios_autorizados.dat'
     * Formato esperado: usuario:contraseña (una por línea)
     */
    public void cargarDatos() {
        usuarios.clear();
        contrasenyas.clear();

        ArrayList<String[]> contenidoFichero = leerArchivoCSV(new File("src/Exam_resources/Ej9_Usuarios_autorizados.dat"));
        for (String[] linea : contenidoFichero) {
            usuarios.add(linea[0]);
            contrasenyas.add(linea[1]);
        }
    }

    /**
     * 🔐 VALIDAR CREDENCIALES
     * Compara usuario y contraseña con la lista cargada.
     * @return true si es válido, false si no.
     */
    public boolean comprobarUsuarioAutenticado(String usuario_contrasenya) {
        String[] datos = usuario_contrasenya.split(":");
        usuario = datos[0];
        String contrasenya = datos[1];
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).equals(usuario) && contrasenyas.get(i).equals(contrasenya)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 📝 CREAR NOMBRE DE ARCHIVO ÚNICO
     * Usa formato: 20260115_143022_log_Ana92.dat
     * Así nunca sobrescribimos logs antiguos.
     */
    public void crearFicheroUsuario() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        nombreArchivo = timestamp + "_log_" + usuario + ".dat";
    }

    /**
     * 💾 GUARDAR MENSAJE EN ARCHIVO
     * Añade una línea al archivo de log del usuario (modo append).
     */
    public void guardarMensajeFichero(String mensaje) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
            bw.write(mensaje);
            bw.newLine();
        }
    }

    /**
     * 📖 LEER ARCHIVO CSV (o .dat con separador :)
     * Divide cada línea por ":" y devuelve una lista de arreglos.
     */
    public static ArrayList<String[]> leerArchivoCSV(File archivo) {
        ArrayList<String[]> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea.split(":"));
            }
        } catch (IOException e) {
            System.err.println("⚠️ Error al leer archivo CSV: " + archivo.getAbsolutePath());
            e.printStackTrace();
        }
        return lineas;
    }
}