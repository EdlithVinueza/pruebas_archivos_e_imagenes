package org.example.analisis.archivosimagen.metadatos.formatos.interfaz;
import com.drew.metadata.Metadata;
import org.example.analisis.archivosimagen.metadatos.modelo.MetadatosImagen;

public interface MetadataExtractor {
    // Fíjate bien en el tipo del segundo parámetro
    void extraer(Metadata metadata, MetadatosImagen.MetadatosImagenBuilder builder);
}

