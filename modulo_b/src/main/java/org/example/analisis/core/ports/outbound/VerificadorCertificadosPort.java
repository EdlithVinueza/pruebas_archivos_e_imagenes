package org.example.analisis.core.ports.outbound;

import java.security.PublicKey;

public interface VerificadorCertificadosPort {
    /**
     * Valida matemáticamente que el certificado fue emitido por la CA del sistema
     * y comprueba su fecha de vigencia y estado en la base de datos (OCSP interno).
     */
    boolean validarVigencia(PublicKey clavePublicaArtista);
}
