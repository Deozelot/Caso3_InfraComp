package org.caso3.servidor;

import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

public class ServidorPrincipal {

    public static final int PUERTO = 5000;

    private PrivateKey llavePrivada;
    private PublicKey llavePublica;

    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor principal escuchando en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();
                System.out.println("Nuevo cliente conectado desde " + cliente.getInetAddress());

                // Crear delegado por cada cliente
                Delegado delegado = new Delegado(cliente);
                delegado.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void iniciarProtocolo(Socket cliente) throws Exception {
        // 0a. Cargar llaves del servidor (desde archivos .der)
        this.llavePrivada = cargarLlavePrivada();
        this.llavePublica = cargarLlavePublica();

        // 1-2. Recibir "HELLO", generar nonce y enviarlo
        // 3. Calcular respuesta Rta = cifrar con llavePrivada
        // 7-8. Generar parámetros DH y enviarlos
        // 13, 15-16. Cifrar con K_AB1 y generar HMAC
        // Manejo de la comunicación con el cliente...
    }

    private PrivateKey cargarLlavePrivada() {
        // Lógica para leer archivo 'llave_privada.der'
        return null;
    }
    private PublicKey cargarLlavePublica() {
        // Lógica para leer archivo 'llave_publica.der'
        return null;
    }
}

