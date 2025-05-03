package org.caso3.seguridad;

import java.security.*;
import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class UtilDH {

    public static KeyPair generarParDH() throws Exception {
        AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
        paramGen.init(2048);
        AlgorithmParameters params = paramGen.generateParameters();
        DHParameterSpec dhSpec = params.getParameterSpec(DHParameterSpec.class);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
        kpg.initialize(dhSpec);
        return kpg.generateKeyPair();
    }

    public static DHParameterSpec obtenerParametros(PublicKey publicKey) throws Exception {
        DHPublicKey dhPublic = (DHPublicKey) publicKey;
        return dhPublic.getParams();
    }

    public static PublicKey generarClavePublica(byte[] encodedKey) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory kf = KeyFactory.getInstance("DH");
        return kf.generatePublic(keySpec);
    }

    public static SecretKey generarLlaveCompartida(PrivateKey privKey, PublicKey pubKey) throws Exception {
        KeyAgreement acuerdo = KeyAgreement.getInstance("DH");
        acuerdo.init(privKey);
        acuerdo.doPhase(pubKey, true);
        byte[] secretBytes = acuerdo.generateSecret();

        // Create AES key directly using SecretKeySpec
        return new SecretKeySpec(secretBytes, 0, 16, "AES");
    }
}
