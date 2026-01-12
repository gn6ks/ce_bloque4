package Handlers.exams;

import Objects.exams.Mensaje;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ex7_manejador implements Runnable {

    private Socket socket;
    private List<String> usuarios = new ArrayList<>();
    private List<String> contrasenyas = new ArrayList<>();
    private File archivo;
    private File archivoContenido = new File("C:\\Users\\pablo\\Documents\\_estudios\\_dam2\\ce_bloque4\\src\\Exam_resources\\Ej7_Usuarios_autorizados.txt");;

    public ex7_manejador(Socket cliente) {
        this.socket = cliente;
    }

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
            System.out.println("Se va a recibir '" + numLineas + "' del cliente.");
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
            throw new RuntimeException(e);
        }

    }

    public void registrarUsuario(String[] contenido) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoContenido, true))) {
            bw.newLine();
            bw.write(contenido[1] + ";" + contenido[2]);
            bw.newLine();

            System.out.println("Usuario '" + contenido[1] + "' dado de alta correctamente");
            System.out.println("Por favor, ejecute de nuevo el programa e inicie sesión.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

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

    public void cargarDatos() {
        usuarios.clear();
        contrasenyas.clear();

        ArrayList<String[]> contenidoArchivo = leerArchivoCSV(archivoContenido);

        for (String[] linea : contenidoArchivo) {
            usuarios.add(linea[0]);
            contrasenyas.add(linea[1]);
        }
    }

    public boolean comprobarInicioSesion(String usuario, String contrasenya) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).equals(usuario) && contrasenyas.get(i).equals(contrasenya)) {
                System.out.println("Usuario '" + usuario + "' esta  en la lista de usuarios autorizados");

                return true;
            }
        }
        return false;
    }

    public void crearFichero(String usuario) {

        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String nombreArchivo = usuario + "_" + ahora.format(formateador) + ".txt";
        archivo = new File(nombreArchivo);

    }

    public void escribirContenido(String contenido) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo, true))) {
            bw.write(contenido);
            bw.newLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static Mensaje crearMensaje(String contenido) {
        Mensaje msj = new Mensaje();
        msj.setContenido(contenido);
        return msj;
    }

}
