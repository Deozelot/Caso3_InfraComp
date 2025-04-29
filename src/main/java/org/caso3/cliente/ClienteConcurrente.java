package org.caso3.cliente;

import java.util.Scanner;

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

        for (int i = 0; i < numeroClientes; i++) {
            new Thread(() -> {
                Cliente.main(null);
            }).start();
        }

        scanner.close();
    }
}

