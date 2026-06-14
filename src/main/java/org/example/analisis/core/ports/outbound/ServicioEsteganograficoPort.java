package org.example.analisis.core.ports.outbound;

public interface ServicioEsteganograficoPort {
    /**
     * Inyecta la cadena JSON estructurada en la capa visual de la imagen (Luminancia DCT).
     * Garantiza la persistencia ante conversiones de formato o compresiones.
     */
    byte[] procesar(byte[] renderOriginal, String jsonPayload);
    
    /**
     * Fusiona el render con esteganografía y el certificado PDF/A-3
     * inyectando el PDF después del EOF del render.
     */
    byte[] fusionarEOF(byte[] renderConEsteganografia, byte[] certificadoPdfBytes);
}
