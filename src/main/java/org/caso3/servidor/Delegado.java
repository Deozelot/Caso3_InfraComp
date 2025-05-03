package org.caso3.servidor;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.caso3.seguridad.UtilCifrado;
import org.caso3.seguridad.UtilDH;
import org.caso3.seguridad.UtilRSA;

public class Delegado extends Thread {

    private final Socket socket;
    private final List<String> listaTiemposCifradoAES;
    private final List<String> listaTiemposCifradoRSA;
    private final List<String> listaTiemposFirma;
    private final List<String> listaTiemposCifradoTabla;
    private final List<String> listaTiemposVerificacion;


    public Delegado(Socket cliente,
                    List<String> listaTiemposCifradoAES,
                    List<String> listaTiemposCifradoRSA,
                    List<String> listaTiemposFirma,
                    List<String> listaTiemposCifradoTabla,
                    List<String> listaTiemposVerificacion) {
        this.socket = cliente;
        this.listaTiemposCifradoAES = listaTiemposCifradoAES;
        this.listaTiemposCifradoRSA = listaTiemposCifradoRSA;
        this.listaTiemposFirma = listaTiemposFirma;
        this.listaTiemposCifradoTabla = listaTiemposCifradoTabla;
        this.listaTiemposVerificacion = listaTiemposVerificacion;
    }

    @Override
    public void run() {
        try {
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

            // 0. Inicialización
            PrivateKey llavePrivadaServidor = UtilRSA.cargarllavePrivada("llave_privada.der");
            PublicKey llavePublicaServidor = UtilRSA.cargarllavePublica("llave_publica.der");
            String [][] tablaServicios = ServidorPrincipal.TABLA_SERVICIOS;

            // 1. Espera HELLO
            String saludo = entrada.readUTF();
            if (!saludo.equals("HELLO")) return;

            // 2b. Recibe reto
            int lenReto = entrada.readInt();
            byte[] reto = new byte[lenReto];
            entrada.readFully(reto);

            // 3. Cifrar reto y envía respuesta
            long inicioCifrarRSA = System.nanoTime();
            byte[] retoCifrado = UtilRSA.cifrarRSA(llavePrivadaServidor, reto);
            long finCifrarRSA = System.nanoTime();
            System.out.println("Tiempo firma (ns): " + (finCifrarRSA - inicioCifrarRSA));
            listaTiemposCifradoRSA.add(String.valueOf(finCifrarRSA - inicioCifrarRSA));
            salida.writeInt(retoCifrado.length);
            salida.write(retoCifrado);

            // 6. Espera OK o ERROR
            String validacion = entrada.readUTF();
            if (!validacion.equals("OK")) return;

            // 7-8. Genera parámetros DH y firma
            KeyPair parServidor = UtilDH.generarParDH();
            DHParameterSpec dhParams = UtilDH.obtenerParametros(parServidor.getPublic());

            byte[] pBytes = dhParams.getP().toByteArray();
            byte[] gBytes = dhParams.getG().toByteArray();
            byte[] gxBytes = parServidor.getPublic().getEncoded();

            byte[] datosParams = UtilRSA.concatenar(pBytes, gBytes, gxBytes);
            long inicioFirma = System.nanoTime();
            byte[] firmaParams = UtilRSA.firmarDatos(llavePrivadaServidor, datosParams);
            long finFirma = System.nanoTime();
            listaTiemposFirma.add(String.valueOf(finFirma - inicioFirma));
            System.out.println("Tiempo firma (ns): " + (finFirma - inicioFirma));

            salida.writeInt(pBytes.length);
            salida.write(pBytes);
            salida.writeInt(gBytes.length);
            salida.write(gBytes);
            salida.writeInt(gxBytes.length);
            salida.write(gxBytes);
            salida.writeInt(firmaParams.length);
            salida.write(firmaParams);

            // 10. Espera OK o ERROR
            String okDH = entrada.readUTF();
            if (!okDH.equals("OK")) return;

            // 11b. Recibe G^y y calcula claves
            int lenGy = entrada.readInt();
            byte[] gyBytes = new byte[lenGy];
            entrada.readFully(gyBytes);

            PublicKey pubCliente = UtilDH.generarClavePublica(gyBytes);

            SecretKey llaveMaestra = UtilDH.generarLlaveCompartida(parServidor.getPrivate(), pubCliente);
            MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
            byte[] digest = sha512.digest(llaveMaestra.getEncoded());
            SecretKey[] llavesSesion = UtilCifrado.derivarLlaves(digest);
            SecretKey aesKey = llavesSesion[0];
            SecretKey macKey = llavesSesion[1];

            // 12b. Recibe IV
            int lenIV = entrada.readInt();
            byte[] ivBytes = new byte[lenIV];
            entrada.readFully(ivBytes);

            // 13. Envía servicios cifrados + HMAC
            StringBuilder sb = new StringBuilder();
            for (String[] registro : tablaServicios) {
                sb.append(registro[0]).append(",").append(registro[1]).append(";");
            }
            long inicioCifradoTabla = System.nanoTime();
            byte[] cifrado = UtilCifrado.cifrarAES(sb.toString().getBytes(), aesKey, new IvParameterSpec(ivBytes));
            long finCifradoTabla = System.nanoTime();
            System.out.println("Tiempo cifrado tabla (ns): " + (finCifradoTabla - inicioCifradoTabla));
            listaTiemposCifradoTabla.add(String.valueOf(finCifradoTabla - inicioCifradoTabla));

            byte[] hmac = UtilCifrado.generarHMAC(cifrado, macKey);
            salida.writeInt(cifrado.length);
            salida.write(cifrado);
            salida.writeInt(hmac.length);
            salida.write(hmac);

            // 14. Recibe ID cifrado y HMAC
            int lenCifID = entrada.readInt();
            byte[] cifID = new byte[lenCifID];
            entrada.readFully(cifID);

            int lenHmacID = entrada.readInt();
            byte[] hmacID = new byte[lenHmacID];
            entrada.readFully(hmacID);

            long inicioHmac = System.nanoTime();
            boolean hmacIDOk = UtilCifrado.verificarHMAC(cifID, hmacID, macKey);
            long finHmac = System.nanoTime();
            System.out.println("Tiempo verificacion HMAC (ns): " + (finHmac - inicioHmac));
            listaTiemposVerificacion.add(String.valueOf(finHmac - inicioHmac));
            if (!hmacIDOk) return;


            long inicioCifradoAES = System.nanoTime();
            byte[] idPlano = UtilCifrado.descifrarAES(cifID, aesKey, new IvParameterSpec(ivBytes));
            long finCifradoAES = System.nanoTime();
            System.out.println("Tiempo cifrado tabla (ns): " + (finCifradoAES - inicioCifradoAES));
            listaTiemposCifradoAES.add(String.valueOf(finCifradoAES - inicioCifradoAES));
            System.out.println("Cliente solicita: " + new String(idPlano));

            // 15-16. Responde con IP cifrada y HMAC
            String respuesta = "";
            for (String[] registro : tablaServicios) {
                if (registro[0].equals(new String(idPlano))) {
                    respuesta = registro[2] + "," + registro[3];
                }
            }

            byte[] rptaCif = UtilCifrado.cifrarAES(respuesta.getBytes(), aesKey, new IvParameterSpec(ivBytes));
            byte[] hmacRpta = UtilCifrado.generarHMAC(rptaCif, macKey);

            salida.writeInt(rptaCif.length);
            salida.write(rptaCif);
            salida.writeInt(hmacRpta.length);
            salida.write(hmacRpta);

            // 18. Espera OK final
            String fin = entrada.readUTF();
            if (fin.equals("OK"))
                System.out.println("Protocolo completado con éxito.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

