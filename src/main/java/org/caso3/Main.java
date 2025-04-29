package org.caso3;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Aquí irán las invocaciones al servidor principal y al cliente según se necesite
    }
}

/*
Notas:
- Se usará java.security, javax.crypto y java.net para la implementación.
- Generar previamente llaves pública y privada RSA 1024 bits para el servidor.
- Intercambio de llaves con Diffie-Hellman (1024 bits, usando AlgorithmParameterGenerator)
- Uso de AES 256 bits en CBC con PKCS5Padding
- HMAC-SHA256 para integridad
- SHA256withRSA para firma digital
- Medición de tiempos con System.nanoTime()
*/