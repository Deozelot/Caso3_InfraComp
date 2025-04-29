package org.caso3.cliente;

public class ClienteConcurrente {

    public static final String HOST = "localhost";
    public static final int PUERTO = 5000;

    public static void main(String[] args) {
        int numeroClientes = 32; // valor por defecto

        if (args.length > 0) {
            try {
                numeroClientes = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Número de clientes inválido. Usando 32 por defecto.");
            }
        }

        for (int i = 0; i < numeroClientes; i++) {
            new ClienteHilo(i + 1).start();
        }
    }
}