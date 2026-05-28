package org.example.analisis.archivosimagen.metadatos.formatos.implementacion;

import com.drew.metadata.Metadata;
import org.example.analisis.archivosimagen.metadatos.formatos.interfaz.MetadataExtractor;
import org.example.analisis.archivosimagen.metadatos.modelo.MetadatosImagen;

public class Icc implements MetadataExtractor {
    @Override
    public void extraer(Metadata metadata, MetadatosImagen.MetadatosImagenBuilder builder) {
        com.drew.metadata.icc.IccDirectory dir = metadata.getFirstDirectoryOfType(com.drew.metadata.icc.IccDirectory.class);
        if (dir != null) {
            builder.tienePerfilIcc(true)
                    .descripcionPerfilIcc(dir.getDescription(com.drew.metadata.icc.IccDirectory.TAG_TAG_desc));
        }
    }
}