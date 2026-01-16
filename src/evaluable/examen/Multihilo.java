package evaluable.examen;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Multihilo implements Runnable {

    private Socket socket;
    private String usuario;
    private String contrasenya;

    private String palabraUsuario;
    private String definicionUsuario;
    private static File ficheroCorrespondiente;

    private List<String> palabras = new ArrayList<>();
    private List<String> definiciones = new ArrayList<>();
    private List<String> palabras_empieza_a = new ArrayList<>();
    private List<String> palabras_empieza_b = new ArrayList<>();
    private List<String> palabras_empieza_c = new ArrayList<>();

    private static ArrayList<File> ficheros = new ArrayList<>(Arrays.asList(
            new File("src/evaluable/diccionari/a.txt"),
            new File("src/evaluable/diccionari/b.txt"),
            new File("src/evaluable/diccionari/c.txt")
    ));

    public Multihilo(Socket cliente) {
        this.socket = cliente;
    }

    @Override
    public void run() {

        try (Socket s = socket;
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter pw = new PrintWriter(socket.getOutputStream(), true)) {

            usuario = br.readLine().toLowerCase();
            contrasenya = br.readLine().toLowerCase();

            if ("root".equals(usuario) && "root".equals(contrasenya)) {
                pw.println("(1). Consultar");
                pw.println("(2). Afegir paraula");
            } else {
                pw.println("(1). Consultar");
            }

            String eleccionUsuario = br.readLine();
            if (1 == Integer.parseInt(eleccionUsuario)) {
                pw.println("200 OK");
            } else if (2 == Integer.parseInt(eleccionUsuario) && "root".equals(usuario) && "root".equals(contrasenya)) {
                pw.println("200 OK");
            } else if (2 == Integer.parseInt(eleccionUsuario) && !"root".equals(usuario) && !"root".equals(contrasenya)) {
                pw.println("403 FORBIDDEN");
            }

            cargarDatos();

            if (1 == Integer.parseInt(eleccionUsuario)) {
                palabraUsuario = br.readLine();
                buscarDefinicionPalabra(pw);
            }
            if (2 == Integer.parseInt(eleccionUsuario) && "root".equals(usuario) && "root".equals(contrasenya)) {
                palabraUsuario = br.readLine();
                String definicionPalabra = br.readLine();
                if (buscarPalabra(palabraUsuario)) {
                    pw.println("OVERWRITE???");

                    String respuestaOverwrite = br.readLine();

                    if ("YES".equals(respuestaOverwrite)) {
                        sobreescribirFichero(definicionPalabra);
                    }
                } else {
                    escribirPalabraNueva(palabraUsuario, definicionPalabra);
                    pw.println("OK");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void sobreescribirFichero(String definicionPalabra) throws IOException {
        List<String> listaCorrecta = new ArrayList<>();

        if (palabraUsuario.startsWith("a")) {
            listaCorrecta  = palabras_empieza_a;
            ficheroCorrespondiente = ficheros.getFirst();
        } else if (palabraUsuario.startsWith("b")) {
            listaCorrecta = palabras_empieza_b;
            ficheroCorrespondiente = ficheros.get(2);
        } else if (palabraUsuario.startsWith("c")) {
            listaCorrecta = palabras_empieza_c;
            ficheroCorrespondiente = ficheros.getLast();
        }

//        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ficheroCorrespondiente))) {
//            for (int i = 0; i < listaCorrecta.size(); i++) {
//                if (palabraUsuario.equals(listaCorrecta.get(i))) {
//                    bw.newLine();
//                    bw.write(palabraUsuario + ";" + definicionPalabra);
//                } else {
//                    bw.newLine();
//                    bw.write(listaCorrecta.get(i));
//                }
//            }
//        }

        int numeroInterrumpir = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ficheroCorrespondiente))) {
            for (int i = 0; i < listaCorrecta.size(); i++) {
                if (palabraUsuario.equals(listaCorrecta.get(i))) {
                    bw.newLine();
                    bw.write(palabraUsuario + ";" + definicionPalabra);
                    numeroInterrumpir = i;
                    break;
                }
            }
            List<String> nuevaLista = listaCorrecta.subList(numeroInterrumpir, listaCorrecta.size());
            for (int j = numeroInterrumpir; j < listaCorrecta.size(); j++) {
                bw.newLine();
                bw.write(nuevaLista.get(j));
            }
        }
    }

    private static void escribirPalabraNueva(String palabraBuscar, String definicionPalabra) throws IOException {
        if (palabraBuscar.startsWith("a")) {
            ficheroCorrespondiente = ficheros.getFirst();
        } else if (palabraBuscar.startsWith("b")) {
            ficheroCorrespondiente = ficheros.get(2);
        } else if (palabraBuscar.startsWith("c")){
            ficheroCorrespondiente = ficheros.getLast();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ficheroCorrespondiente, true))) {
            bw.newLine();
            bw.write(palabraBuscar + ";" + definicionPalabra);
        }
    }

    private void buscarDefinicionPalabra(PrintWriter pw) {
        for (int i = 0; i < palabras.size(); i++) {
            if (palabraUsuario.equals(palabras.get(i))) {
                int posicionPalabra = i;
                String definicion = definiciones.get(posicionPalabra);
                pw.println("Definicion: " + definicion);
                break;
            }
        }
        pw.println("404 NOT FOUND");
    }

    private boolean buscarPalabra(String palabraBuscar) {
        for (int i = 0; i < palabras.size(); i++) {
            if (palabraBuscar.equals(palabras.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void cargarDatos() {
        palabras.clear();
        definiciones.clear();

        for (int i = 0; i < ficheros.size(); i++) {
            ArrayList<String[]> contenidoFichero = leerArchivoCSV(ficheros.get(i));

            for (String[] linea : contenidoFichero) {
                palabras.add(linea[0]);
                definiciones.add(linea[1]);
            }
        }

        palabras_empieza_a = crearListas(ficheros.getFirst());
        palabras_empieza_b = crearListas(ficheros.get(2));
        palabras_empieza_c = crearListas(ficheros.getLast());

    }

    public static ArrayList<String> crearListas(File archivo) {
        ArrayList<String> arreglo = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                arreglo.add(linea);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arreglo;
    }

    public static ArrayList<String[]> leerArchivoCSV(File archivo) {
        ArrayList<String[]> lineas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea.split(";"));
            }
        } catch (IOException e) {
            System.err.println("Error al leer archivo CSV: " + archivo.getAbsolutePath());
            e.printStackTrace();
        }
        return lineas;
    }
}
