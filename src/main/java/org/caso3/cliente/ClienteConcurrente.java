package org.caso3.cliente;

import java.io.IOException;
import java.util.*;

public class ClienteConcurrente {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numeroClientes = 16;  // default value

        System.out.print("Ingrese el número de clientes (Enter para usar 16 por defecto): ");
        String input = scanner.nextLine();

        if (!input.trim().isEmpty()) {
            try {
                numeroClientes = Integer.parseInt(input.trim());
                if (numeroClientes <= 0) {
                    System.out.println("El número debe ser positivo. Usando 16 por defecto.");
                    numeroClientes = 16;
                }
            } catch (NumberFormatException e) {
                System.out.println("Número inválido, usando 16 por defecto.");
            }
        }

        System.out.println("Iniciando " + numeroClientes + " clientes...");

        LinkedList <Thread> clientes = new LinkedList<>();

        for (int i = 0; i < numeroClientes; i++) {

            Thread nuevo = new Thread(() -> {
                try {
                    Cliente.main(null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            nuevo.start();
            clientes.add(nuevo);
        }

        for (Thread cliente : clientes) {
            try {
                cliente.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("El Cliente ha terminado de ejecutarse.");
        }

        scanner.close();
    }
}