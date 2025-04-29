package org.caso3.servidor;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.caso3.seguridad.UtilCifrado;
import org.caso3.seguridad.UtilDH;

public class Delegado extends Thread {
    private Socket cliente;

    public Delegado(Socket cliente) {
        this.cliente = cliente;
    }

    @Override
    public void run() {
        long inicioConexion = System.nanoTime();

        try (
                DataInputStream entrada = new DataInputStream(cliente.getInputStream());
                DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());
        ) {
            // Servidor genera parámetros DH
            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(1024);
            AlgorithmParameters params = paramGen.generateParameters();
            DHParameterSpec dhParams = params.getParameterSpec(DHParameterSpec.class);
            KeyPair parServidor = UtilDH.generarParDH(dhParams);

            // Enviar p, g y llave pública servidor
            byte[] pBytes = dhParams.getP().toByteArray();
            salida.writeInt(pBytes.length);
            salida.write(pBytes);

            byte[] gBytes = dhParams.getG().toByteArray();
            salida.writeInt(gBytes.length);
            salida.write(gBytes);

            byte[] pubServidorBytes = parServidor.getPublic().getEncoded();
            salida.writeInt(pubServidorBytes.length);
            salida.write(pubServidorBytes);

            // Recibir llave pública cliente
            int lenClaveCliente = entrada.readInt();
            byte[] pubClienteBytes = new byte[lenClaveCliente];
            entrada.readFully(pubClienteBytes);

            KeyFactory kf = KeyFactory.getInstance("DH");
            PublicKey pubCliente = kf.generatePublic(new X509EncodedKeySpec(pubClienteBytes));

            // Calcular llave compartida
            SecretKey llaveMaestra = UtilDH.generarLlaveCompartida(parServidor.getPrivate(), pubCliente);
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            byte[] digest = sha512.digest(llaveMaestra.getEncoded());

            SecretKey[] llavesSesion = UtilCifrado.derivarLlaves(digest);
            SecretKey aesKey = llavesSesion[0];

            System.out.println("Servidor derivó llaves AES y HMAC correctamente");

            int lenIV = entrada.readInt();
            byte[] ivBytes = new byte[lenIV];
            entrada.readFully(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            int lenCifrado = entrada.readInt();
            byte[] cifrado = new byte[lenCifrado];
            entrada.readFully(cifrado);

            long inicioDescifrado = System.nanoTime();

            // Descifrar mensaje
            byte[] plano = UtilCifrado.descifrarAES(cifrado, aesKey, iv);

            long finDescifrado = System.nanoTime();

            System.out.println("Mensaje recibido y descifrado: " + new String(plano));

            long duracionConexionMs = (finDescifrado - inicioConexion) / 1_000_000;
            long duracionDescifradoMs = (finDescifrado - inicioDescifrado) / 1_000_000;

            System.out.println("Duración total sesión: " + duracionConexionMs + " ms | Descifrado: " + duracionDescifradoMs + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { cliente.close(); } catch (IOException ex) { ex.printStackTrace(); }
        }
    }
}

