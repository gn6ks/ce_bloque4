package Handlers.exams;

import Objects.exams.Message;

import java.io.*;
import java.net.Socket;

public class ex2_handler implements Runnable {
    private final Socket socket;

    /**
     * Constructor para inicializar el manejador con el socket del cliente.
     * @param client El socket de la conexión del cliente.
     */
    public ex2_handler(Socket client) {
        this.socket = client;
    }

    /**
     * Lógica principal del manejador que se ejecuta en un nuevo hilo.
     * Gestiona la comunicación, autenticación y recepción de datos.
     */
    @Override
    public void run() {
        try (Socket s = socket;
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {

            // La llamada inicial a flush() es crucial para que ObjectOutputStream
            // escriba su encabezado en el stream de salida, lo que permite a
            // ObjectInputStream en el cliente inicializarse correctamente.
            oos.flush();

            // 1. Lectura del mensaje inicial de LOGIN.
            Message msgLogin = (Message) ois.readObject();

            // 2. Comprobación de credenciales.
            if (!checkUser(msgLogin.getUser()) || !checkPassword(msgLogin.getPassword())) {
                // Si la autenticación falla, se envía un mensaje de ERROR y se termina el manejador.
                msgLogin.setContent("ERROR");
                oos.writeObject(msgLogin);
                oos.flush();
                return; // Termina la ejecución del hilo.
            }

            // 3. Autenticación exitosa: se envía la confirmación (200 OK).
            msgLogin.setContent("200 OK");
            oos.writeObject(msgLogin);
            oos.flush();

            // 1. Se espera el mensaje que indica el número de líneas (aunque el valor no se usa para el bucle).
            Message msgNumLines = (Message) ois.readObject();
            System.out.println("Numero de lineas de mensajes a recibir de cliente: " + msgNumLines.getContent());

            // 2. Preparación para la escritura. Se usa 'true' para el 'FileWriter' para AÑADIR (append) al archivo.
            File file = new File("contenido.txt");

            // 3. Se notifica al cliente que el servidor está listo para recibir el contenido ("PREPARED").
            msgNumLines.setContent("PREPARED");
            oos.writeObject(msgNumLines);
            oos.flush();

            // Uso de try-with-resources para el BufferedWriter/FileWriter para asegurar el cierre y el volcado de datos.
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                Message contentMsg;

                // Bucle infinito para recibir mensajes hasta que se reciba la señal de parada.
                while (true) {
                    contentMsg = (Message) ois.readObject();

                    // CONDICIÓN DE PARADA: El cliente envía la señal "END CLIENT".
                    if ("END CLIENT".equals(contentMsg.getContent())) {
                        System.out.println("END CLIENT mensaje recibido...");
                        break;
                    }

                    // Escritura del contenido del mensaje en el archivo 'contenido.txt'
                    System.out.println("Mensaje recibido: " + contentMsg.getContent());
                    bw.write(contentMsg.getContent());
                    bw.newLine(); // Añade un salto de línea después de cada mensaje.
                }

                // 1. Envío de la confirmación final al cliente ("END SERVER").
                Message endConnection = new Message();
                endConnection.setContent("END SERVER");
                oos.writeObject(endConnection);
                oos.flush();

                // 2. El 'return' termina la ejecución. Los streams del try-with-resources se cierran automáticamente.
                return;
            } // El BufferedWriter (bw) se cierra aquí, volcando los datos.

        } catch (Exception e) {
            // Manejo de excepciones, como IOException o ClassNotFoundException.
            e.printStackTrace();
        }
    }

    /**
     * Comprueba si el nombre de usuario existe en el archivo de usuarios autorizados.
     *
     * @param user El nombre de usuario a buscar.
     * @return true si el usuario es encontrado entre las etiquetas <usuario>...</usuario>, false en caso contrario.
     */
    private boolean checkUser(String user) {
        // Usa try-with-resources para asegurar que el BufferedReader se cierra.
        try (BufferedReader br = new BufferedReader(new FileReader("src/Exam_resources/Ej2_Usuarios_autorizados.txt"))) {
            String line;
            String tagStart = "<usuario>";
            String tagEnd = "</usuario>";

            while ((line = br.readLine()) != null) {

                // Comprueba si la línea contiene ambas etiquetas.
                if (line.contains(tagStart) && line.contains(tagEnd)) {
                    // Extracción del contenido entre las etiquetas.
                    int start = line.indexOf(tagStart) + tagStart.length();
                    int end = line.indexOf(tagEnd);
                    String fileUser = line.substring(start, end).trim();

                    // Comparación y retorno si se encuentra la coincidencia.
                    if (fileUser.equals(user)) {
                        return true;
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Comprueba si la contraseña existe en el archivo de contraseñas autorizadas.
     *
     * @param password La contraseña a buscar.
     * @return true si la contraseña es encontrada entre las etiquetas <contrasenya>...</contrasenya>, false en caso contrario.
     */
    private boolean checkPassword(String password) {
        // Usa try-with-resources para asegurar que el BufferedReader se cierra.
        try (BufferedReader br = new BufferedReader(new FileReader("src/Exam_resources/Ej2_Contrasenyas_autorizadas.txt"))) {
            String line;
            String tagStart = "<contrasenya>";
            String tagEnd = "</contrasenya>";

            while ((line = br.readLine()) != null) {
                // Comprueba si la línea contiene ambas etiquetas.
                if (line.contains(tagStart) && line.contains(tagEnd)) {
                    // Extracción del contenido entre las etiquetas.
                    int start = line.indexOf(tagStart) + tagStart.length();
                    int end = line.indexOf(tagEnd);
                    String filePassword = line.substring(start, end).trim();

                    // Comparación y retorno si se encuentra la coincidencia.
                    if (filePassword.equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}