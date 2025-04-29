package org.caso3.seguridad;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.AlgorithmParameterSpec;

public class UtilDH {

    public static AlgorithmParameterSpec generarParametrosDH() throws Exception {
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        paramGen.init(2048);
        AlgorithmParameters params = paramGen.generateParameters();
        return params.getParameterSpec(DHParameterSpec.class);
    }

    public static KeyPair generarParDH(AlgorithmParameterSpec spec) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
        keyGen.initialize(spec);
        return keyGen.generateKeyPair();
    }

    public static SecretKey generarLlaveCompartida(PrivateKey privada, PublicKey publica) throws Exception {
        KeyAgreement acuerdo = KeyAgreement.getInstance("DH");
        acuerdo.init(privada);
        acuerdo.doPhase(publica, true);
        byte[] secretBytes = acuerdo.generateSecret();
        byte[] keyBytes = new byte[32];
        System.arraycopy(secretBytes, 0, keyBytes, 0, keyBytes.length);
        return new SecretKeySpec(keyBytes, "AES");
    }
}