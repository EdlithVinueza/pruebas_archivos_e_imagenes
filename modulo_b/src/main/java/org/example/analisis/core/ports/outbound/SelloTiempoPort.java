package org.example.analisis.core.ports.outbound;

public interface SelloTiempoPort {
    /**
     * Obtiene un timestamp confiable (RFC 3161) basado en el hash del documento.
     */
    long obtenerTimestamp(String hash);
}
