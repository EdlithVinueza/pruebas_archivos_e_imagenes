package org.example.analisis.archivospsd.metadatos.formatos.implementacion;

import com.drew.metadata.Metadata;
import com.drew.metadata.file.FileTypeDirectory;
import org.example.analisis.archivospsd.metadatos.formatos.interfaz.IMetadataExtractor;
import org.example.analisis.archivospsd.metadatos.modelo.MetadatosPSD.MetadatosPSDBuilder;

public class FileType implements IMetadataExtractor {

    @Override
    public void extract(Metadata metadata, MetadatosPSDBuilder builder) {
        FileTypeDirectory directory = metadata.getFirstDirectoryOfType(FileTypeDirectory.class);
        if (directory == null) {
            return;
        }

        builder.nombreArchivoDetectado(directory.getDescription(FileTypeDirectory.TAG_DETECTED_FILE_TYPE_NAME))
               .nombreLargoArchivoDetectado(directory.getDescription(FileTypeDirectory.TAG_DETECTED_FILE_TYPE_LONG_NAME))
             .tipoMimeDetectado(directory.getDescription(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE))
               .extensionEsperada(directory.getDescription(FileTypeDirectory.TAG_EXPECTED_FILE_NAME_EXTENSION));
    }
}