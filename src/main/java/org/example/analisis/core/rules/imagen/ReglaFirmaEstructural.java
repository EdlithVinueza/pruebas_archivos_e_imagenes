package org.example.analisis.core.rules.imagen;

import org.example.analisis.core.ports.IReglaValidacion;
import org.example.analisis.core.model.imagen.ArchivoImagen;
import org.example.analisis.core.model.validacion.ResultadoValidacion;

public class ReglaFirmaEstructural implements IReglaValidacion<ArchivoImagen> {
    @Override
    public ResultadoValidacion validar(ArchivoImagen img) {
        String formatoBinario = img.getEstructura().getFormatoReal();
        String extension = img.getNombreArchivo().toLowerCase();

        boolean esImagenValida = "PNG".equals(formatoBinario) || "JPEG".equals(formatoBinario);
        boolean coincideConExtension = extension.endsWith(formatoBinario.toLowerCase()) ||
                (formatoBinario.equals("JPEG") && (extension.endsWith("jpg") || extension.endsWith("jpeg")));

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
