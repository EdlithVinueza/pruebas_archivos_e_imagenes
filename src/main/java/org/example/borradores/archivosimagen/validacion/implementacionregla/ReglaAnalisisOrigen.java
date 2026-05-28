package org.example.analisis.archivosimagen.validacion.implementacionregla;

import org.example.analisis.archivosimagen.archivo.modelo.ArchivoImage;
import org.example.analisis.reglavalidacion.interfaz.IReglaValidacion;
import org.example.analisis.reglavalidacion.modelo.ResultadoValidacion;

public class ReglaAnalisisOrigen implements IReglaValidacion<ArchivoImage> {
    @Override
    public ResultadoValidacion validar(ArchivoImage img) {
        double dpi = img.getMetadatos().getDpiCalculado();
        String gamma = img.getMetadatos().getValorGamma();
        String srgb = img.getMetadatos().getIntentoInterpretacion();

        // 1. DETECCIÓN DE RECORTE DE PANTALLA (96-120 DPI + Gamma/sRGB)
        if (dpi >= 90.0 && dpi <= 125.0) {
            if (gamma != null || srgb != null) {
                return ResultadoValidacion.builder()
                        .nombreRegla("Análisis Forense de Origen")
                        .esValido(false)
                        .mensaje("VEREDICTO: RECORTE DE PANTALLA. Resolución de monitor y perfiles de sistema detectados.")
                        .build();
            }
        }

        // 2. DETECCIÓN DE IMAGEN DE INTERNET (72 DPI + Sin Perfil ICC)
        if (dpi == 72.0 && !img.getMetadatos().isTienePerfilIcc()) {
            return ResultadoValidacion.builder()
                    .nombreRegla("Análisis Forense de Origen")
                    .esValido(false)
                    .mensaje("VEREDICTO: IMAGEN DE INTERNET. Resolución optimizada para web y sin gestión de color.")
                    .build();
        }

        // 3. DETECCIÓN DE ARCHIVO ORIGINAL / EXPORT
        if (dpi >= 300.0) {
            return ResultadoValidacion.builder()
                    .nombreRegla("Análisis Forense de Origen")
                    .esValido(true)
                    .mensaje("VEREDICTO: EXPORTACIÓN ORIGINAL. Alta densidad detectada (" + dpi + " DPI).")
                    .build();
        }

        return ResultadoValidacion.builder().nombreRegla("Análisis Forense de Origen").esValido(true).mensaje("Origen aceptable.").build();
    }
    @Override public boolean esCritica() { return true; }
}