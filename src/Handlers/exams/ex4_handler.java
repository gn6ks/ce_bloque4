package Handlers.exams;

import Objects.exams.Message;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ex4_handler implements Runnable {

    private Socket socket;
    private int posicion;
    private List<String> numerosEscritos = new ArrayList<>(Arrays.asList("cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"));
    private List<String> usuarios = new ArrayList<>();
    private List<String> contrasenyas = new ArrayList<>();
    private List<String> contenido = new ArrayList<>();

    public ex4_handler(Socket cliente) {
        this.socket = cliente;
    }

    @Override
    public void run() {

        try (Socket s = socket; ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {

            oos.flush();

            Message mensajeUsuario = (Message) ois.readObject();
            String usuario = mensajeUsuario.getContent();
            Message mensajeContrasenya = (Message) ois.readObject();
            String contrasenya = mensajeContrasenya.getContent();

            cargarDatos();

            if (!comprobarUsuarioAutorizado(usuario, contrasenya)) {
                System.err.println("El usuario '" + usuario + "' no se encuentra autorizado");
                oos.writeObject(crearMensaje("ERROR"));
                return;
            }

            int numeroRandom = (int) (Math.random() * 9) + 1;
            oos.writeObject(crearMensaje(String.valueOf(numeroRandom)));

            Message numeroEscrito_Cliente = (Message) ois.readObject();
            String numeroEscrito = numeroEscrito_Cliente.getContent();

            if (!comprobarAutenticacion(numeroRandom, numeroEscrito)) {
                System.err.println("El numero escrito por parte del usuario '" + numeroEscrito + "' no coincide con la autenticacion");
                oos.writeObject(crearMensaje("ERROR"));
                return;
            }

            recogerContenido();
            oos.writeObject(crearMensaje(contenido.get(posicion)));

            Message cierreCliente = (Message) ois.readObject();

            if (!"RECIBIDO".equals(cierreCliente.getContent())) {
                System.err.println("No se ha cerrado correctamente la conexion");
                System.err.println("Cerrando la conexion correctamente...");
                return;
            }

            System.err.println("Cerrando la conexion correctamente...");
            return;

        } catch (Exception e) {
            System.err.println("💥 Error inesperado en el manejador:");
            e.printStackTrace();
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

        File archivo_usuarios_autorizados = new File("src/Exam_resources/Ej4_usuarios_autorizados.txt");
        ArrayList<String[]> contenidoArchivo = leerArchivoCSV(archivo_usuarios_autorizados);

        for (String[] linea : contenidoArchivo) {
            usuarios.add(linea[0]);
            contrasenyas.add(linea[1]);
        }
    }

    public void recogerContenido() {
        contenido.clear();

        File archivo_contenido = new File("src/Exam_resources/Ej4_contenido.txt");
        ArrayList<String[]> contenidoArchivo = leerArchivoCSV(archivo_contenido);

        for (String[] linea : contenidoArchivo) {
            contenido.add(linea[1]);
        }
    }

    public boolean comprobarUsuarioAutorizado(String usuario, String contrasenya) {
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).equals(usuario) && contrasenyas.get(i).equals(contrasenya)) {
                System.out.println("Usuario '" + usuario + "' esta  en la lista de usuarios autorizados");
                posicion = i;

                return true;
            }
        }
        return false;
    }

    public boolean comprobarAutenticacion(int numeroRandom, String numeroEscrito) {
        if (numerosEscritos.get(numeroRandom).equals(numeroEscrito)) {
            return true;
        }
        return false;
    }

    private static Message crearMensaje(String contenido) {
        Message msg = new Message();
        msg.setContent(contenido);
        return msg;
    }
}
