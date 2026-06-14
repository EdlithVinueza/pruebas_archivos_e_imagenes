package org.example.analisis.core.ports.outbound;

public interface ServicioCriptograficoCAPort {
    /**
     * Aplica la doble firma (co-firma) de la Autoridad Certificadora (Sistema)
     * sobre los datos del artista y la obra.
     */
    String firmarPaquete(String dataToSign);
}
