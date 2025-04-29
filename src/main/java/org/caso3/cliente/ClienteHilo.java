package org.caso3.cliente;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClienteHilo extends Thread {

    private final int id;

    public ClienteHilo(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        try (
                Socket socket = new Socket(ClienteConcurrente.HOST, ClienteConcurrente.PUERTO);
                InputStream entrada = socket.getInputStream();
                OutputStream salida = socket.getOutputStream();
        ) {
            String mensaje = "Consulta del cliente #" + id;

            long inicio = System.nanoTime();

            salida.write(mensaje.getBytes());

            byte[] buffer = new byte[1024];
            int bytesLeidos = entrada.read(buffer);
            String respuesta = new String(buffer, 0, bytesLeidos);

            long fin = System.nanoTime();

            long duracionMs = (fin - inicio) / 1_000_000;

            System.out.println("Cliente #" + id + " recibió: " + respuesta +
                    " en " + duracionMs + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
