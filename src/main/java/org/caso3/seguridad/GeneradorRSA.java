package org.caso3.seguridad;

import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class GeneradorRSA {

    public static void main(String[] args) {
        try {
            KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
            generador.initialize(1024);
            KeyPair parLlaves = generador.generateKeyPair();

            PrivateKey privada = parLlaves.getPrivate();
            PublicKey publica = parLlaves.getPublic();

            // Guardar llave privada
            try (FileOutputStream fos = new FileOutputStream("llave_privada.der")) {
                fos.write(privada.getEncoded());
            }

            // Guardar llave pública
            try (FileOutputStream fos = new FileOutputStream("llave_publica.der")) {
                fos.write(publica.getEncoded());
            }

            System.out.println("Llaves RSA generadas y guardadas correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

