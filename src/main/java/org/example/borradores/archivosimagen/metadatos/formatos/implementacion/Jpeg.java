package org.example.borradores.archivosimagen.metadatos.formatos.implementacion;

import com.drew.metadata.Metadata;
import org.example.borradores.archivosimagen.metadatos.formatos.interfaz.MetadataExtractor;
import org.example.borradores.archivosimagen.metadatos.modelo.MetadatosImagen;

public class Jpeg implements MetadataExtractor {
    @Override
    public void extraer(Metadata metadata, MetadatosImagen.MetadatosImagenBuilder builder) {
        com.drew.metadata.jpeg.JpegDirectory dir = metadata.getFirstDirectoryOfType(com.drew.metadata.jpeg.JpegDirectory.class);
        if (dir != null) {
            builder.ancho(dir.getInteger(com.drew.metadata.jpeg.JpegDirectory.TAG_IMAGE_WIDTH))
                    .alto(dir.getInteger(com.drew.metadata.jpeg.JpegDirectory.TAG_IMAGE_HEIGHT));
        }
    }
}