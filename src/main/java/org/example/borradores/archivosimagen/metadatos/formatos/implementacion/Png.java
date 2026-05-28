package org.example.borradores.archivosimagen.metadatos.formatos.implementacion;


import com.drew.metadata.Metadata;
import org.example.borradores.archivosimagen.metadatos.formatos.interfaz.MetadataExtractor;
import org.example.borradores.archivosimagen.metadatos.modelo.MetadatosImagen;

public class Png implements MetadataExtractor {

    @Override
    public void extraer(Metadata metadata, MetadatosImagen.MetadatosImagenBuilder builder) {
        com.drew.metadata.png.PngDirectory ihdr = metadata.getFirstDirectoryOfType(com.drew.metadata.png.PngDirectory.class);
        if (ihdr != null) {
            builder.ancho(ihdr.getInteger(com.drew.metadata.png.PngDirectory.TAG_IMAGE_WIDTH))
                    .alto(ihdr.getInteger(com.drew.metadata.png.PngDirectory.TAG_IMAGE_HEIGHT))
                    .tipoColor(ihdr.getDescription(com.drew.metadata.png.PngDirectory.TAG_COLOR_TYPE));
        }

        for (com.drew.metadata.Directory dir : metadata.getDirectories()) {
            String name = dir.getName();
            if ("PNG-pHYs".equals(name)) {
                builder.pixelesPorUnidadX(dir.getLongObject(16))
                        .pixelesPorUnidadY(dir.getLongObject(17)) // Recuperamos la Y
                        .unidadFisica(dir.getDescription(18));
            } else if ("PNG-sRGB".equals(name)) {
                builder.intentoInterpretacion(dir.getDescription(10));
            } else if ("PNG-gAMA".equals(name)) {
                builder.valorGamma(dir.getDescription(11));
            }
        }
    }
}