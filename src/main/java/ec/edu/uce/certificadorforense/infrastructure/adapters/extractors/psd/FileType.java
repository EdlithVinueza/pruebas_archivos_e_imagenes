package analisis.adapters.extractors.psd;

import com.drew.metadata.Metadata;
import com.drew.metadata.file.FileTypeDirectory;
import analisis.adapters.extractors.MetadataExtractor;
import analisis.core.model.psd.MetadatosPSD;

public class FileType implements MetadataExtractor<MetadatosPSD.MetadatosPSDBuilder> {

    @Override
    public void extraer(Metadata metadata, MetadatosPSD.MetadatosPSDBuilder builder) {
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
