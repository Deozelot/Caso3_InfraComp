package org.caso3.seguridad;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.GeneralSecurityException;

public class UtilCifrado {

    public static IvParameterSpec generarIV() throws GeneralSecurityException {
        byte[] ivBytes = new byte[16];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(ivBytes);
        return new IvParameterSpec(ivBytes);
    }

    public static byte[] cifrarAES(byte[] datos, SecretKey clave, IvParameterSpec iv) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, clave, iv);
        return cipher.doFinal(datos);
    }

    public static byte[] generarHMAC(byte[] datos, SecretKey claveHMAC) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(claveHMAC);
        return mac.doFinal(datos);
    }

    public static byte[] firmar(byte[] datos, PrivateKey privada) throws GeneralSecurityException {
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privada);
        signer.update(datos);
        return signer.sign();
    }

    public static boolean verificarFirma(byte[] datos, byte[] firma, PublicKey publica) throws GeneralSecurityException {
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publica);
        verifier.update(datos);
        return verifier.verify(firma);
    }

    public static SecretKey[] derivarLlaves(byte[] llaveMaestraDigest512) {
        // Se asume que llaveMaestraDigest512 tiene 64 bytes (512 bits)
        byte[] claveAES = new byte[32];  // 256 bits
        byte[] claveHMAC = new byte[32]; // 256 bits

        System.arraycopy(llaveMaestraDigest512, 0, claveAES, 0, 32);
        System.arraycopy(llaveMaestraDigest512, 32, claveHMAC, 0, 32);

        SecretKey keyAES = new SecretKeySpec(claveAES, "AES");
        SecretKey keyHMAC = new SecretKeySpec(claveHMAC, "HmacSHA256");
        return new SecretKey[]{keyAES, keyHMAC};
    }
}
