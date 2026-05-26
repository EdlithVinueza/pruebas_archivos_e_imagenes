package org.example.analisis.archivosimagen.metadatos.formatos.implementacion;

import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.example.analisis.archivosimagen.metadatos.formatos.interfaz.MetadataExtractor;
import org.example.analisis.archivosimagen.metadatos.modelo.MetadatosImagen;

public class Exif implements MetadataExtractor {
    @Override
    public void extraer(Metadata metadata, MetadatosImagen.MetadatosImagenBuilder builder) {
        ExifIFD0Directory ifd0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        // Verificamos que ifd0 exista antes de hacer nada
        if (ifd0 != null) {
            Long res = ifd0.getLongObject(ExifIFD0Directory.TAG_X_RESOLUTION);
            if (res != null) {
                builder.pixelesPorUnidadX(res);
                builder.unidadFisica("Inches");
            }
        }
    }
}