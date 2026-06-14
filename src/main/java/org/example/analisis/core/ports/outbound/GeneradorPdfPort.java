package org.example.analisis.core.ports.outbound;

import org.example.analisis.core.model.forense.PayloadForense;

public interface GeneradorPdfPort {
    /**
     * Genera un certificado PDF/A-3 (con código QR de la firma electrónica)
     * a partir de los datos periciales de la obra.
     */
    byte[] generarCertificado(PayloadForense payload);
}
