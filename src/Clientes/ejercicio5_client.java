package Clientes;

import ejercicios.Libro;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ejercicio5_client {
    public static void main(String[] args) {
        try (Socket client = new Socket("localhost", 1234); ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {

            Libro libro = new Libro();
            libro.setTitulo("Titulo 1");
            libro.setAutor("Autor");
            libro.setEditorial("Tilin Editorial Papers");

            oos.writeObject(libro);
            oos.flush();

            Libro libro2 = (Libro) ois.readObject();
            System.out.println("Objeto recibido: " + libro2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
