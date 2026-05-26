package org.example.analisis.archivosimagen.validacion.implementacionregla;

import org.example.analisis.archivosimagen.archivo.modelo.ArchivoImage;
import org.example.analisis.reglavalidacion.interfaz.IReglaValidacion;
import org.example.analisis.reglavalidacion.modelo.ResultadoValidacion;

public class ReglaCoherenciaDpi implements IReglaValidacion<ArchivoImage> {
    @Override
    public ResultadoValidacion validar(ArchivoImage img) {
        double dpiLibreria = img.getMetadatos().getDpiCalculado();
        int dpiBinario = img.getEstructura().getDpiX();

        // Margen de error de 1.0 por redondeos
        boolean coherente = Math.abs(dpiLibreria - dpiBinario) < 1.1;

        return ResultadoValidacion.builder()
                .nombreRegla("Coherencia de Resolución")
                .esValido(coherente)
                .mensaje(coherente ? "Metadatos y Estructura coinciden." :
                        "MANIPULACIÓN DETECTADA: Librería dice " + dpiLibreria + " pero los bytes reales son " + dpiBinario)
                .build();
    }
    @Override public boolean esCritica() { return true; }
}