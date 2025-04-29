package org.caso3.servidor;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Delegado extends Thread {

    private Socket cliente;

    public Delegado(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    public void run() {
        try (
                InputStream entrada = cliente.getInputStream();
                OutputStream salida = cliente.getOutputStream();
        ) {
            long inicioAtencion = System.nanoTime();

            byte[] buffer = new byte[1024];
            int bytesLeidos = entrada.read(buffer);
            String recibido = new String(buffer, 0, bytesLeidos);
            System.out.println("Cliente dijo: " + recibido);

            String respuesta = "Respuesta a: " + recibido;
            salida.write(respuesta.getBytes());

            long finAtencion = System.nanoTime();

            long tiempoAtencionMs = (finAtencion - inicioAtencion) / 1_000_000;
            System.out.println("Tiempo de atención (ms): " + tiempoAtencionMs);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                cliente.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
