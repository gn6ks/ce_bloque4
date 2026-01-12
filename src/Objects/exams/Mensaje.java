package Objects.exams;

import java.io.Serializable;

public class Mensaje implements Serializable {

    private String contenido;

    public Mensaje() {};

    public Mensaje(String contenido) {
        this.contenido = contenido;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    @Override
    public String toString() {
        return "Mensaje {" +
                "contenido='" + contenido + '\'' +
                '}';
    }
}
