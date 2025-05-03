package org.caso3.cliente;

import org.caso3.seguridad.UtilCifrado;
import org.caso3.seguridad.UtilDH;
import org.caso3.seguridad.UtilRSA;

import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class ClienteIterativo {

    private Socket socket = null;

    public void main(String[] args) throws IOException {
        try {
            this.socket = new Socket("localhost", 5000);
            System.out.println("Cliente conectado al servidor...");
            for (int i = 0; i < 32; i++) {
                correr32Veces();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void correr32Veces(){
        try {
            Socket socket = new Socket("localhost", 5000);
            System.out.println("Cliente conectado al servidor...");

            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

            // 0. Inicialización
            PublicKey clavePublicaServidor = UtilRSA.cargarllavePublica("llave_publica.der");


            // 1. Cliente envía "HELLO"
            salida.writeUTF("HELLO");

            // 2a. Cliente genera un reto (nonce)
            byte[] nonce = UtilRSA.generarNonce(16);
            // 2b. Envía el reto
            salida.writeInt(nonce.length);
            salida.write(nonce);

            // 3. Recibe la respuesta firmada del servidor
            int lenRta = entrada.readInt();
            byte[] rta = new byte[lenRta];
            entrada.readFully(rta);

            // 5a. Verifica la firma
            boolean valido = UtilRSA.verificarFirma(clavePublicaServidor, nonce, rta);

            // 6. Responde OK o ERROR
            salida.writeUTF(valido ? "OK" : "ERROR");
            if (!valido) {
                System.out.println("Autenticación fallida.");
                return;
            }

            // 7-8. Recibe parámetros DH y firma
            int lenP = entrada.readInt();
            byte[] pBytes = new byte[lenP];
            entrada.readFully(pBytes);

            int lenG = entrada.readInt();
            byte[] gBytes = new byte[lenG];
            entrada.readFully(gBytes);

            int lenGX = entrada.readInt();
            byte[] gxBytes = new byte[lenGX];
            entrada.readFully(gxBytes);

            int lenFirma = entrada.readInt();
            byte[] firmaParams = new byte[lenFirma];
            entrada.readFully(firmaParams);

            // 9. Verifica la firma sobre (P, G, G^x)
            byte[] datosParams = UtilRSA.concatenar(pBytes, gBytes, gxBytes);
            boolean firmaValida = UtilRSA.verificarFirma(clavePublicaServidor, datosParams, firmaParams);

            // 10. Responde OK o ERROR
            salida.writeUTF(firmaValida ? "OK" : "ERROR");
            if (!firmaValida) {
                System.out.println("Firma DH inválida.");
                return;
            }
            // 11a. Calcula G^y mod p y claves
            DHParameterSpec dhParams = new DHParameterSpec(
                    new BigInteger(pBytes),
                    new BigInteger(gBytes)
            );

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
            kpg.initialize(dhParams);
            KeyPair parCliente = kpg.generateKeyPair();

            KeyFactory kf = KeyFactory.getInstance("DH");
            PublicKey pubServidor = kf.generatePublic(new X509EncodedKeySpec(gxBytes));

            SecretKey llaveMaestra = UtilDH.generarLlaveCompartida(parCliente.getPrivate(), pubServidor);
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            byte[] digest = sha512.digest(llaveMaestra.getEncoded());
            SecretKey[] llavesSesion = UtilCifrado.derivarLlaves(digest);
            SecretKey aesKey = llavesSesion[0];
            SecretKey macKey = llavesSesion[1];

            // Enviar G^y
            byte[] gyBytes = parCliente.getPublic().getEncoded();
            salida.writeInt(gyBytes.length);
            salida.write(gyBytes);

            // 12a. Genera IV
            byte[] ivBytes = UtilCifrado.generarIV().getIV();
            // 12b. Envia IV
            salida.writeInt(ivBytes.length);
            salida.write(ivBytes);

            // 13. Recibe mensaje cifrado y HMAC
            int lenCifrado = entrada.readInt();
            byte[] cifrado = new byte[lenCifrado];
            entrada.readFully(cifrado);

            int lenHmac = entrada.readInt();
            byte[] hmac = new byte[lenHmac];
            entrada.readFully(hmac);

            // Verifica HMAC
            boolean hmacValido = UtilCifrado.verificarHMAC(cifrado, hmac, macKey);
            if (!hmacValido) {
                System.out.println("HMAC inválido.");
                return;
            }

            byte[] plano = UtilCifrado.descifrarAES(cifrado, aesKey, new IvParameterSpec(ivBytes));
            System.out.println("Servicios recibidos: " + new String(plano));

            // 14. Envía ID servicio y HMAC
            String idServicio = "SVC-01";
            byte[] datosCif = UtilCifrado.cifrarAES(idServicio.getBytes(), aesKey, new IvParameterSpec(ivBytes));
            byte[] hmacDatos = UtilCifrado.generarHMAC(datosCif, macKey);

            salida.writeInt(datosCif.length);
            salida.write(datosCif);
            salida.writeInt(hmacDatos.length);
            salida.write(hmacDatos);

            // 15-18. Confirmación final
            int lenRptaCif = entrada.readInt();
            byte[] rptaCif = new byte[lenRptaCif];
            entrada.readFully(rptaCif);

            int lenHmacRpta = entrada.readInt();
            byte[] hmacRpta = new byte[lenHmacRpta];
            entrada.readFully(hmacRpta);

            boolean hmacOk = UtilCifrado.verificarHMAC(rptaCif, hmacRpta, macKey);
            if (!hmacOk) {
                System.out.println("HMAC respuesta inválido.");
                return;
            }

            byte[] respuesta = UtilCifrado.descifrarAES(rptaCif, aesKey, new IvParameterSpec(ivBytes));
            System.out.println("Servidor respondió: " + new String(respuesta));

            salida.writeUTF("OK");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
