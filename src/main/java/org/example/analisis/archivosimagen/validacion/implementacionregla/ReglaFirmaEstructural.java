package org.example.analisis.archivosimagen.validacion.implementacionregla;

import org.example.analisis.archivosimagen.archivo.modelo.ArchivoImage;
import org.example.analisis.reglavalidacion.interfaz.IReglaValidacion;
import org.example.analisis.reglavalidacion.modelo.ResultadoValidacion;

public class ReglaFirmaEstructural implements IReglaValidacion<ArchivoImage> {
    @Override
    public ResultadoValidacion validar(ArchivoImage img) {
        String formatoBinario = img.getEstructura().getFormatoReal();
        String extension = img.getNombreArchivo().toLowerCase();

        boolean esImagenValida = "PNG".equals(formatoBinario) || "JPEG".equals(formatoBinario);
        boolean coincideConExtension = extension.endsWith(formatoBinario.toLowerCase()) ||
                (formatoBinario.equals("JPEG") && extension.endsWith("jpg"));

        boolean pasa = esImagenValida && coincideConExtension;

        return ResultadoValidacion.builder()
                .nombreRegla("Integridad de Firma Binaria")
                .esValido(pasa)
                .mensaje(pasa ? "Estructura válida: " + formatoBinario :
                        "FRAUDE: Firma binaria (" + formatoBinario + ") no coincide con extensión.")
                .build();
    }

    @Override public boolean esCritica() { return true; }
}
