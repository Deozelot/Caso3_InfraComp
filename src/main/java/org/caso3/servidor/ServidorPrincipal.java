package org.caso3.servidor;

import org.caso3.seguridad.UtilRSA;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class ServidorPrincipal {

    public static final int PUERTO = 5000;

    private static boolean enEjecucion = true;

    private static final List<String> listaTiemposFirma = new ArrayList<>();
    private static final List<String> listaTiemposCifradoTabla = new ArrayList<>();
    private static final List<String> listaTiemposVerificacion = new ArrayList<>();
    private static final List<String> listaTiemposCifradoRSA = new ArrayList<>();
    private static final List<String> listaTiemposCifradoAES = new ArrayList<>();


    protected static final String[][] TABLA_SERVICIOS = {
            {"S1", "Estado vuelo", "IPS1", "PS1"},
            {"S2", "Disponibilidad vuelos", "IPS2", "PS2"},
            {"S3", "Costo de un vuelo", "IPS3", "PS3"},
    };

    public static void main(String[] args) throws IOException {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor principal escuchando en el puerto " + PUERTO);

            // Hilo para leer comandos desde la consola
            Thread lectorComandos = new Thread(() -> {
                try (Scanner scanner = new Scanner(System.in)) {
                    while (enEjecucion) {
                        String comando = scanner.nextLine();
                        if ("END".equalsIgnoreCase(comando)) {
                            enEjecucion = false;
                            System.out.println("Cerrando servidor...");
                        }
                    }
                }
            });
            lectorComandos.start();

            // Bucle principal del servidor
            while (enEjecucion) {
                try {
                    try {
                        servidor.setSoTimeout(2000); // Evita bloqueo indefinido en accept()
                    } catch (SocketException ex) {
                        throw new RuntimeException(ex);
                    }
                    Socket cliente = null;
                    try {
                        cliente = servidor.accept();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("Nuevo cliente desde " + cliente.getInetAddress());
                    Delegado delegado = new Delegado(
                            cliente,
                            listaTiemposCifradoAES,
                            listaTiemposCifradoRSA,
                            listaTiemposFirma,
                            listaTiemposCifradoTabla,
                            listaTiemposVerificacion);
                    delegado.start();
                } catch (RuntimeException ex) {

                }
            }
            servidor.close();
            ExportadorExcel.exportarCSV("Tiempos.csv",listaTiemposFirma,listaTiemposCifradoTabla,listaTiemposVerificacion,listaTiemposCifradoRSA,listaTiemposCifradoAES);
            System.out.println("Servidor cerrado.");
        }
    }
}