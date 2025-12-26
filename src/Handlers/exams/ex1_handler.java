package Handlers.exams;

import Objects.exams.Message;

import java.io.*;
import java.net.Socket;

public class ex1_handler implements Runnable {

    private final Socket socket;

    public ex1_handler(Socket client) {
        this.socket = client;
    }

    @Override
    public void run() {
        try (Socket s = socket; ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream()); ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {

            oos.flush();

            // 1. Comprobar usuario
            Message msgUser = (Message) ois.readObject();
            if (!checkUser(msgUser.getUser())) {
                msgUser.setContent("ERROR");
                oos.writeObject(msgUser);
                oos.flush();
                return;
            }

            msgUser.setContent("200 OK");
            oos.writeObject(msgUser);
            oos.flush();

            // 2. Comprobar contraseña
            Message msgPass = (Message) ois.readObject();
            if (!checkPassword(msgPass.getPassword())) {
                msgPass.setContent("ERROR");
                oos.writeObject(msgPass);
                oos.flush();
                return;
            }

            msgPass.setContent("200 OK");
            oos.writeObject(msgPass);
            oos.flush();

            // 3. Esperar PREPARADO
            Message prepared = (Message) ois.readObject();
            if (!"PREPARADO".equals(prepared.getContent())) {
                return;
            }

            // 4. Enviar número de líneas
            File file = new File("src/Exam_resources/Ej1_Contenido_a_enviar.txt");
            int lines = countLines(file);

            Message numLines = new Message();
            numLines.setContent(String.valueOf(lines));
            oos.writeObject(numLines);
            oos.flush();

            // 5. Enviar contenido línea a línea
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Message contentMsg = new Message();
                    contentMsg.setContent(line);
                    oos.writeObject(contentMsg);
                    oos.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkUser(String user) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/Exam_resources/Ej1_Usuarios_autorizados.txt"))) {
            String line;
            String tagStart = "<usuario>";
            String tagEnd = "</usuario>";

            while ((line = br.readLine()) != null) {

                if (line.contains(tagStart) && line.contains(tagEnd)) {
                    int start = line.indexOf(tagStart) + tagStart.length();
                    int end = line.indexOf(tagEnd);
                    String fileUser = line.substring(start, end).trim();

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

    private boolean checkPassword(String password)  {
        try (BufferedReader br = new BufferedReader(new FileReader("src/Exam_resources/Ej1_Contrasenyas_autorizadas.txt"))) {
            String line;
            String tagStart = "<contrasenya>";
            String tagEnd = "</contrasenya>";

            while ((line = br.readLine()) != null) {
                if (line.contains(tagStart) && line.contains(tagEnd)) {
                    int start = line.indexOf(tagStart) + tagStart.length();
                    int end = line.indexOf(tagEnd);
                    String filePassword = line.substring(start, end).trim();

                    if (filePassword.equals(password)) {
                        return true;
                    }
                }
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private int countLines(File file) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.readLine() != null) {
                count++;
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
