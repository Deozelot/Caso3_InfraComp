package org.caso3.seguridad;

import javax.crypto.Cipher;
import java.io.*;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

public class UtilRSA {

    public static void generarParRSA(String nombrePrivada, String nombrePublica) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair par = kpg.generateKeyPair();

        // Guardar privada DER
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(par.getPrivate().getEncoded());
        try (FileOutputStream fos = new FileOutputStream(nombrePrivada)) {
            fos.write(privSpec.getEncoded());
        }

        // Guardar pública DER
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(par.getPublic().getEncoded());
        try (FileOutputStream fos = new FileOutputStream(nombrePublica)) {
            fos.write(pubSpec.getEncoded());
        }
    }

    public static PrivateKey cargarllavePrivada(String ruta) throws Exception {
        byte[] bytes = leerBytes(ruta);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public static PublicKey cargarllavePublica(String ruta) throws Exception {
        byte[] bytes = leerBytes(ruta);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public static byte[] firmarDatos(PrivateKey clavePrivada, byte[] datos) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initSign(clavePrivada);
        firma.update(datos);
        return firma.sign();
    }

    public static boolean verificarFirma(PublicKey clavePublicaServidor, byte[] nonce, byte[] rta) {
        try {
            Signature firmaServidor = Signature.getInstance("SHA256withRSA");
            firmaServidor.initVerify(clavePublicaServidor);
            firmaServidor.update(nonce);
            return firmaServidor.verify(rta);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] concatenar(byte[]... arrays) {
        int total = Arrays.stream(arrays).mapToInt(a -> a.length).sum();
        byte[] resultado = new byte[total];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, resultado, pos, array.length);
            pos += array.length;
        }
        return resultado;
    }

    private static byte[] leerBytes(String ruta) throws IOException {
        try (FileInputStream fis = new FileInputStream(ruta)) {
            return fis.readAllBytes();
        }
    }

    public static byte[] generarNonce(int i) {
        byte[] nonce = new byte[i];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static byte[] cifrarRSA(PrivateKey llavePrivada, byte[] datos) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, llavePrivada);
        return cipher.doFinal(datos);
    }

    public static byte[] descifrarRSA(PublicKey llavePublica, byte[] datos) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, llavePublica);
        return cipher.doFinal(datos);
    }

}
