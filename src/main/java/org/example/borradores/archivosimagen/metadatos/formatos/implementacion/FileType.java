package org.example.borradores.archivosimagen.metadatos.formatos.implementacion;

import com.drew.metadata.Metadata;
import org.example.borradores.archivosimagen.metadatos.formatos.interfaz.MetadataExtractor;
import org.example.borradores.archivosimagen.metadatos.modelo.MetadatosImagen;

public class FileType implements MetadataExtractor {
    @Override
    public void extraer(Metadata metadata, MetadatosImagen.MetadatosImagenBuilder builder) {
        com.drew.metadata.file.FileTypeDirectory dir = metadata.getFirstDirectoryOfType(com.drew.metadata.file.FileTypeDirectory.class);
        if (dir != null) {
            builder.tipoMime(dir.getString(com.drew.metadata.file.FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE))
                    .extensionReal(dir.getString(com.drew.metadata.file.FileTypeDirectory.TAG_EXPECTED_FILE_NAME_EXTENSION));
        }
    }
}