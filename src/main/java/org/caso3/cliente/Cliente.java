package org.caso3.cliente;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.*;
import java.math.BigInteger;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.caso3.seguridad.UtilCifrado;
import org.caso3.seguridad.UtilDH;


public class Cliente {

    public static void main(String[] args) {
        try (
                Socket socket = new Socket("localhost", 5000);
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
        ) {
            // Recibir p y g
            int lenP = entrada.readInt();
            byte[] pBytes = new byte[lenP];
            entrada.readFully(pBytes);
            BigInteger p = new BigInteger(pBytes);

            int lenG = entrada.readInt();
            byte[] gBytes = new byte[lenG];
            entrada.readFully(gBytes);
            BigInteger g = new BigInteger(gBytes);

            DHParameterSpec params = new DHParameterSpec(p, g);

            // Generar par de llaves cliente
            KeyPair parCliente = UtilDH.generarParDH(params);

            // Recibir llave pública del servidor
            int lenClaveServidor = entrada.readInt();
            byte[] pubServerBytes = new byte[lenClaveServidor];
            entrada.readFully(pubServerBytes);

            KeyFactory kf = KeyFactory.getInstance("DH");
            PublicKey pubServidor = kf.generatePublic(new X509EncodedKeySpec(pubServerBytes));

            // Enviar llave pública cliente
            byte[] pubClienteBytes = parCliente.getPublic().getEncoded();
            salida.writeInt(pubClienteBytes.length);
            salida.write(pubClienteBytes);

            // Calcular llave compartida
            SecretKey llaveMaestra = UtilDH.generarLlaveCompartida(parCliente.getPrivate(), pubServidor);
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            byte[] digest = sha512.digest(llaveMaestra.getEncoded());

            // Derivar llaves AES y HMAC
            SecretKey[] llavesSesion = UtilCifrado.derivarLlaves(digest);
            SecretKey aesKey = llavesSesion[0];

            System.out.println("Cliente derivó llaves AES y HMAC correctamente");

            // Cifrar mensaje
            IvParameterSpec iv = UtilCifrado.generarIV();
            String mensaje = "Consulta del cliente";
            byte[] cifrado = UtilCifrado.cifrarAES(mensaje.getBytes(), aesKey, iv);

            // Enviar IV y mensaje cifrado
            salida.writeInt(iv.getIV().length);
            salida.write(iv.getIV());
            salida.writeInt(cifrado.length);
            salida.write(cifrado);

            System.out.println("Mensaje cifrado enviado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
