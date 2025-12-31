package Handlers.exams;

import Objects.exams.Message;

import java.io.*;
import java.net.Socket;

public class ex2_handler implements Runnable {
    private final Socket socket;

    public ex2_handler(Socket client) {
        this.socket = client;
    }

    @Override
    public void run() {
        try (Socket s = socket;
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(s.getInputStream())) {

            oos.flush();

            Message msgLogin = (Message) ois.readObject();
            if (!checkUser(msgLogin.getUser()) || !checkPassword(msgLogin.getPassword())) {
                msgLogin.setContent("ERROR");
                oos.writeObject(msgLogin);
                oos.flush();
                return;
            }

            msgLogin.setContent("200 OK");
            oos.writeObject(msgLogin);
            oos.flush();

            Message msgNumLines = (Message) ois.readObject();
            System.out.println("Numero de lineas de mensajes a recibir de cliente: " + msgNumLines.getContent());
            File file = new File("contenido.txt");
            msgNumLines.setContent("PREPARED");
            oos.writeObject(msgNumLines);
            oos.flush();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                Message contentMsg;

                while (true) {
                    contentMsg = (Message) ois.readObject();

                    if ("END CLIENT".equals(contentMsg.getContent())) {
                        System.out.println("END CLIENT mensaje recibido...");
                        break;
                    }

                    System.out.println("Mensaje recibido: " + contentMsg.getContent());
                    bw.write(contentMsg.getContent());
                    bw.newLine();
                }

                Message endConnection = new Message();
                endConnection.setContent("END SERVER");
                oos.writeObject(endConnection);
                oos.flush();
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkUser(String user) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/Exam_resources/Ej2_Usuarios_autorizados.txt"))) {
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
        try (BufferedReader br = new BufferedReader(new FileReader("src/Exam_resources/Ej2_Contrasenyas_autorizadas.txt"))) {
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
}
