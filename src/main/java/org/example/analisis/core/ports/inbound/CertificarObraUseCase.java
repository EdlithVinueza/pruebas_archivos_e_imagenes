package org.example.analisis.core.ports.inbound;

import org.example.analisis.core.model.forense.Certificado;

public interface CertificarObraUseCase {
    /**
     * Orquesta la Fase 2, 3 y 4 del proceso forense.
     * @param renderOriginal Imagen final (byte array).
     * @param pHash Hash perceptual previamente calculado.
     * @param p12Bytes Archivo P12 subido por el artista.
     * @param password Contraseña del archivo P12.
     * @return El Certificado con el archivo forense híbrido generado.
     */
    Certificado certificar(byte[] renderOriginal, String pHash, byte[] p12Bytes, char[] password);
}
