package org.caso3.servidor;

import java.net.ServerSocket;
import java.net.Socket;

public class ServidorPrincipal {

    public static final int PUERTO = 5000;

    protected static final String[][] TABLA_SERVICIOS = {
        {"S1", "Estado vuelo", "127.0.0.1", "PS1"},
        {"S2", "Disponibilidad vuelos", "127.0.0.1", "PS2"},
        {"S3", "Costo de un vuelo", "127.0.0.1", "PS3"},
    };

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor principal escuchando en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();
                System.out.println("Nuevo cliente desde " + cliente.getInetAddress());

                Delegado delegado = new Delegado(cliente);
                delegado.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


