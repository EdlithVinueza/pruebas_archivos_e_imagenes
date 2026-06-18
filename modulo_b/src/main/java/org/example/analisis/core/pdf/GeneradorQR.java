package org.example.analisis.core.pdf;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.example.analisis.core.model.forense.PayloadForense;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class GeneradorQR {

    /**
     * Genera un código QR en formato Base64 a partir del PayloadForense.
     * Este código contiene los datos en texto plano para ser escaneados de forma autónoma offline.
     *
     * @param payload El objeto con los datos del análisis forense.
     * @return Una cadena en Base64 con la imagen PNG del código QR.
     * @throws WriterException Si ocurre un error al codificar la matriz del QR.
     * @throws IOException Si ocurre un error de escritura del flujo de bytes.
     */
    public static String generarQrBase64(PayloadForense payload) throws WriterException, IOException {
        String textoForense = construirTextoForense(payload);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // 400x400 es un buen tamaño para un escaneo rápido
        BitMatrix bitMatrix = qrCodeWriter.encode(textoForense, BarcodeFormat.QR_CODE, 400, 400);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();

        return Base64.getEncoder().encodeToString(pngData);
    }

    /**
     * Construye el bloque de texto plano que irá incrustado dentro del QR.
     */
    private static String construirTextoForense(PayloadForense payload) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- CERTIFICADO FORENSE DE AUTENTICIDAD DIGITAL ---\n");
        sb.append("ID: ").append(payload.getDatosDelCertificado() != null ? payload.getDatosDelCertificado().getIdCertificado() : "N/A").append("\n");
        sb.append("Fecha: ").append(payload.getDatosDelCertificado() != null ? payload.getDatosDelCertificado().getFechaEmision() : "N/A").append("\n");
        
        sb.append("\n1. AUTORÍA\n");
        sb.append("Autor: ").append(payload.getAutor() != null ? payload.getAutor().getNombre() : "N/A").append("\n");
        sb.append("ID Inst: ").append(payload.getAutor() != null ? payload.getAutor().getIdInstitucional() : "N/A").append("\n");
        
        sb.append("\n2. OBRA\n");
        sb.append("Título: ").append(payload.getObra() != null ? payload.getObra().getTitulo() : "N/A").append("\n");
        sb.append("Creación: ").append(payload.getObra() != null ? payload.getObra().getFechaDeclaradaCreacion() : "N/A").append("\n");
        
        sb.append("\n3. ANÁLISIS DE INTEGRIDAD\n");
        sb.append("SHA-256: ").append(payload.getAnalisisForenseDigital() != null ? payload.getAnalisisForenseDigital().getSha256Criptografico() : "N/A").append("\n");
        sb.append("pHash: ").append(payload.getAnalisisForenseDigital() != null ? payload.getAnalisisForenseDigital().getPhashPerceptual() : "N/A").append("\n");
        sb.append("Validación: ").append(payload.getDatosDelCertificado() != null ? payload.getDatosDelCertificado().getEstadoInicial() : "N/A").append("\n");
        sb.append("---------------------------------------------------");
        return sb.toString();
    }
}
