package org.caso3.cliente;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Cliente {

    public static final String HOST = "localhost";
    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        try (
                Socket socket = new Socket(HOST, PUERTO);
                InputStream entrada = socket.getInputStream();
                OutputStream salida = socket.getOutputStream();
        ) {
            // Enviar mensaje
            String mensaje = "Consulta inicial";
            salida.write(mensaje.getBytes());
            System.out.println("Mensaje enviado: " + mensaje);

            // Recibir respuesta
            byte[] buffer = new byte[1024];
            int bytesLeidos = entrada.read(buffer);
            String respuesta = new String(buffer, 0, bytesLeidos);
            System.out.println("Respuesta del servidor: " + respuesta);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

