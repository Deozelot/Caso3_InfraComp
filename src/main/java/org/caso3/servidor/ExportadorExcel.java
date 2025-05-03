package org.caso3.servidor;

import java.io.FileWriter;
import java.util.List;

public class ExportadorExcel {
    public static void exportarCSV(String rutaCSV,
                                   List<String> listaFirma,
                                   List<String> listaCifradoTabla,
                                   List<String> listaVerificacion,
                                   List<String> listaCifradoRSA,
                                   List<String> listaCifradoAES) {

        try (FileWriter writer = new FileWriter(rutaCSV)) {
            writer.write("Firma,CifradoTabla,Verificacion,CifradoRSA,CifradoAES\n");
            int maxSize = Math.max(listaFirma.size(),
                    Math.max(listaCifradoTabla.size(),
                            Math.max(listaVerificacion.size(),
                                    Math.max(listaCifradoRSA.size(), listaCifradoAES.size()))));
            for (int i = 0; i < maxSize; i++) {
                String f = i < listaFirma.size() ? listaFirma.get(i) : "";
                String ct = i < listaCifradoTabla.size() ? listaCifradoTabla.get(i) : "";
                String v = i < listaVerificacion.size() ? listaVerificacion.get(i) : "";
                String rsa = i < listaCifradoRSA.size() ? listaCifradoRSA.get(i) : "";
                String aes = i < listaCifradoAES.size() ? listaCifradoAES.get(i) : "";
                writer.write(f + "," + ct + "," + v + "," + rsa + "," + aes + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
