package Servidores;

import ejercicios.Libro;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ejercicio5_server {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(1234)) {

            System.out.println("Servidor escuchando en puerto 1234...");

            while (true) {
                Socket client = server.accept();
                System.out.println("Cliente conectado!");

                /**
                 * recoger un objeto (Libro) y devolver un nuevo objeto (Libro) a cliente
                 */
//                try (ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
//                    Libro libro = (Libro) ois.readObject();
//                    System.out.println("Objeto recibido: " + libro.toString());
//
//                    Libro respuesta = new Libro();
//                    respuesta.setTitulo("Titulo 2");
//                    respuesta.setAutor("Autor 2");
//                    respuesta.setEditorial("Kirk Land News");
//
//                    oos.writeObject(respuesta);
//                    oos.flush();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                /**
                 * recoger un objeto (Libro), cambiarle un atributo mediante los setters programados y devolver mismo objeto (Libro) a cliente
                 */
                try (ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
                    Libro libro = (Libro) ois.readObject();
                    System.out.println("Objeto recibido: " + libro);

                    libro.setTitulo("JIJIJIJA");

                    oos.writeObject(libro);
                    oos.flush();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
