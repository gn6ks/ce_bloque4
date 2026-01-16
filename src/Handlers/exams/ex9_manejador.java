package Handlers.exams;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ex9_manejador implements Runnable {

    private Socket socket;
    private List<String> usuarios = new ArrayList<>();
    private List<String> contrasenyas = new ArrayList<>();
    private String usuario;
    private String nombreArchivo;


    public ex9_manejador(Socket cliente) {
        this.socket = cliente;
    }

    @Override
    public void run() {

        try (Socket s = socket; BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {

            cargarDatos();

            String usuario_contrasenya = br.readLine();

            if (!comprobarUsuarioAutenticado(usuario_contrasenya)) {
                pw.println("ERROR");
                return;
            }
            pw.println("OK");

            int numeroLineas = Integer.parseInt(br.readLine());
            System.out.println("Nº a recibir '" + numeroLineas + "'.");
            crearFicheroUsuario();

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < numeroLineas; i++) {
                String mensaje = br.readLine();
                System.out.println("Nº " + (i + 1) + "/" + numeroLineas + ": " + mensaje);
                sb.append(mensaje).append(":");
                guardarMensajeFichero(mensaje);
            }

            pw.println(sb);

            String estatusCliente = br.readLine();
            if ("ERROR".equals(estatusCliente)) {
                System.err.println("Las lineas NO coinciden con las que se ha enviado a cliente ESTATUS '" + estatusCliente + "'");
            } else {
                System.out.println("Las lineas coinciden con las que se ha enviado a cliente ESTATUS '" + estatusCliente + "'");
            }
            System.err.println("Cerrando conexion...");
            return;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void cargarDatos() {
        usuarios.clear();
        contrasenyas.clear();

        ArrayList<String[]> contenidoFichero = leerArchivoCSV(new File("src/Exam_resources/Ej9_Usuarios_autorizados.dat"));
        for (String[] linea : contenidoFichero) {
            usuarios.add(linea[0]);
            contrasenyas.add(linea[1]);
        }
    }

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

    public void crearFicheroUsuario() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        nombreArchivo = timestamp + "_log_" + usuario + ".dat";
    }

    public void guardarMensajeFichero(String mensaje) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo, true))) {
            bw.write(mensaje);
            bw.newLine();
        }
    }

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
