package org.example.analisis.archivosimagen.metadatos.formatos.implementacion;

import com.drew.metadata.Metadata;
import com.drew.metadata.jfif.JfifDirectory;
import org.example.analisis.archivosimagen.metadatos.formatos.interfaz.MetadataExtractor;
import org.example.analisis.archivosimagen.metadatos.modelo.MetadatosImagen;

public class Jfif implements MetadataExtractor {
    @Override
    public void extraer(Metadata metadata, MetadatosImagen.MetadatosImagenBuilder builder) {
        JfifDirectory dir = metadata.getFirstDirectoryOfType(JfifDirectory.class);

        if (dir != null) {
            // Usamos getInteger para evitar las "Unhandled exceptions"
            Integer x = dir.getInteger(JfifDirectory.TAG_RESX);
            Integer y = dir.getInteger(JfifDirectory.TAG_RESY);

            if (x != null) builder.pixelesPorUnidadX(x.longValue());
            if (y != null) builder.pixelesPorUnidadY(y.longValue());

            // Unidades: 1 = Pulgadas (Inches), 2 = Centímetros
            builder.unidadFisica(dir.getDescription(JfifDirectory.TAG_UNITS));
        }
    }
}