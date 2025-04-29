package org.caso3.seguridad;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class UtilCifrado {

    public static IvParameterSpec generarIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static byte[] cifrarAES(byte[] datos, SecretKey clave, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, clave, iv);
        return cipher.doFinal(datos);
    }

    public static byte[] descifrarAES(byte[] datos, SecretKey clave, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, clave, iv);
        return cipher.doFinal(datos);
    }

    public static byte[] generarHMAC(byte[] datos, SecretKey claveHMAC) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(claveHMAC);
        return mac.doFinal(datos);
    }

    public static byte[] firmar(byte[] datos, PrivateKey privada) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initSign(privada);
        firma.update(datos);
        return firma.sign();
    }

    public static boolean verificarFirma(byte[] datos, byte[] firmaBytes, PublicKey publica) throws Exception {
        Signature firma = Signature.getInstance("SHA256withRSA");
        firma.initVerify(publica);
        firma.update(datos);
        return firma.verify(firmaBytes);
    }

    public static SecretKey[] derivarLlaves(byte[] digest) throws Exception {
        byte[] claveAES = new byte[32];
        byte[] claveHMAC = new byte[32];

        System.arraycopy(digest, 0, claveAES, 0, 32);
        System.arraycopy(digest, 32, claveHMAC, 0, 32);

        SecretKey aesKey = new SecretKeySpec(claveAES, "AES");
        SecretKey hmacKey = new SecretKeySpec(claveHMAC, "HmacSHA256");

        return new SecretKey[] { aesKey, hmacKey };
    }
}
