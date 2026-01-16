package Clientes.exams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * 💻 CLIENTE TCP SIMPLE
 *
 * Este programa simula un cliente que:
 * - Se conecta a un servidor en localhost:5001
 * - Envía credenciales (usuario:contraseña)
 * - Si es aceptado, envía N líneas de texto
 * - Verifica que el servidor recibió correctamente los datos
 *
 * 🎯 Propósito: probar la comunicación cliente-servidor con autenticación y verificación.
 */
public class ex9_cliente {

    /**
     * 🚀 MÉTODO PRINCIPAL
     *
     * Flujo:
     * 1. Pedir usuario y contraseña
     * 2. Enviar credenciales al servidor
     * 3. Si el servidor dice "ERROR" → salir
     * 4. Pedir cuántas líneas enviar
     * 5. Enviar cada línea
     * 6. Recibir eco del servidor (todas las líneas juntas)
     * 7. Comparar lo enviado vs lo recibido
     * 8. Enviar confirmación al servidor (OK o ERROR)
     */
    public static void main(String[] args) {
        try (
                Scanner sc = new Scanner(System.in);
                // 📡 Conexión TCP al servidor
                Socket cliente = new Socket("localhost", 5001);
                PrintWriter pw = new PrintWriter(cliente.getOutputStream(), true);
                BufferedReader br = new BufferedReader(new InputStreamReader(cliente.getInputStream()))
        ) {
            // 👤 Paso 1: Pedir credenciales
            System.out.printf("Usuario: ");
            String usuario = sc.nextLine();
            System.out.printf("Contraseña: ");
            String contrasenya = sc.nextLine();

            // 🔑 Paso 2: Enviar credenciales
            pw.println(usuario + ":" + contrasenya);

            // 📬 Paso 3: Leer respuesta del servidor
            String estatusServidor = br.readLine();
            if ("ERROR".equals(estatusServidor)) {
                System.err.println("Usuario '" + usuario + "' no está autorizado.");
                return;
            }

            // 📤 Paso 4: Cuántas líneas enviar
            System.out.printf("Nº líneas a enviar al servidor: ");
            int numLineas = sc.nextInt();
            sc.nextLine(); // Consumir el salto de línea

            pw.println(numLineas);

            StringBuilder sb = new StringBuilder();

            // 📤 Paso 5: Enviar cada línea
            for (int i = 0; i < numLineas; i++) {
                System.out.printf("Nº " + (i + 1) + "/" + numLineas + ": ");
                String mensaje = sc.nextLine();
                pw.println(mensaje);
                sb.append(mensaje).append(":");
            }

            // 📥 Paso 6: Recibir eco del servidor
            String mensajesRecibidosServidor = br.readLine();
            String[] mensajesRecibidos = mensajesRecibidosServidor.split(":");
            String mensajesEnviadosCliente = sb.toString();
            String[] mensajesEnviados = mensajesEnviadosCliente.split(":");

            // 🔍 Paso 7: Comparar línea por línea
            boolean estanBienLasLineas = true; // ⚠️ Corregido: antes decía "false"
            for (int i = 0; i < numLineas; i++) {
                if (!mensajesEnviados[i].equals(mensajesRecibidos[i])) {
                    estanBienLasLineas = false;
                    break; // Si una falla, ya no coincide
                }
            }

            // 📬 Paso 8: Enviar confirmación al servidor
            if (estanBienLasLineas) {
                pw.println("OK");
                System.out.println("✅ Las líneas coinciden con las que recibió el servidor. ESTATUS 'OK'");
            } else {
                pw.println("ERROR");
                System.out.println("❌ Las líneas NO coinciden. ESTATUS 'ERROR'");
            }

            System.err.println("Cerrando conexión...");

        } catch (Exception e) {
            throw new RuntimeException("⚠️ Error en el cliente", e);
        }
    }
}