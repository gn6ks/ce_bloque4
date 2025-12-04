package ejercicios;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ejercicio5_client {
    public static void main(String[] args) {
        try (Socket client = new Socket("localhost", 1234); ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream())) {

            Libro libro = new Libro();
            libro.setTitulo("Titulo 1");
            libro.setAutor("Autor");
            libro.setEditorial("Tilin Editorial Papers");
            oos.writeObject(libro);

            System.out.println("Mensaje enviado!");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
