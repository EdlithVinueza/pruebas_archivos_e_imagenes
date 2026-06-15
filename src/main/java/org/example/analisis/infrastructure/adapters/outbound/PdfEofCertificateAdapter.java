package org.example.analisis.infrastructure.adapters.outbound;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.analisis.core.model.forense.PayloadForense;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class PdfEofCertificateAdapter {

    private final AdaptadorPdfIText generadorPdfBase;
    private final ObjectMapper objectMapper;

    public PdfEofCertificateAdapter() {
        this.generadorPdfBase = new AdaptadorPdfIText();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Genera el PDF base a partir de la plantilla HTML y le inyecta el JSON firmado al final (EOF).
     */
    public byte[] generarCertificadoConEof(PayloadForense payload) {
        try {
            // 1. Generar el PDF normal inmutable
            byte[] pdfOriginalBytes = generadorPdfBase.generarCertificado(payload);

            // 2. Serializar el Payload a JSON
            String jsonFirmado = objectMapper.writeValueAsString(payload);

            // 3. Preparar la inyección EOF
            String eofMarker = "\n||VERISART_START||\n" + jsonFirmado + "\n||VERISART_END||\n";
            byte[] eofBytes = eofMarker.getBytes(StandardCharsets.UTF_8);

            // 4. Concatenar
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(pdfOriginalBytes);
            baos.write(eofBytes);

            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error inyectando el JSON en el EOF del PDF: " + e.getMessage(), e);
        }
    }
}
