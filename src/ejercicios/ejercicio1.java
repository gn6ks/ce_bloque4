package ejercicios;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ejercicio1 {

    public static void main(String[] args) {
        downloader("http://localhost:80/cloud/charlie.jpeg", "charlie.jpeg");
    }

    public static void downloader(String url, String recurso) {
        try {
            BufferedImage img = ImageIO.read(new URL(url));
            ImageIO.write(img, "jpeg", new File(recurso));
        } catch (IOException e) {
            System.err.println("error en la escritura / lectura de la imagen");
        }
    }

}
