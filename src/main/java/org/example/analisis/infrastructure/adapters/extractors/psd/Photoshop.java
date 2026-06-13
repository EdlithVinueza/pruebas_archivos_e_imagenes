package org.example.analisis.infrastructure.adapters.extractors.psd;

import com.drew.metadata.Metadata;
import com.drew.metadata.photoshop.PhotoshopDirectory;
import org.example.analisis.infrastructure.adapters.extractors.MetadataExtractor;
import org.example.analisis.core.model.psd.MetadatosPSD;

public class Photoshop implements MetadataExtractor<MetadatosPSD.MetadatosPSDBuilder> {
    
    @Override
    public void extraer(Metadata metadata, MetadatosPSD.MetadatosPSDBuilder builder) {
        PhotoshopDirectory directory = metadata.getFirstDirectoryOfType(PhotoshopDirectory.class);
        if (directory != null) {
            builder.infoResolucion(directory.getDescription(PhotoshopDirectory.TAG_RESOLUTION_INFO))
                   .infoEstadoCapas(directory.getDescription(PhotoshopDirectory.TAG_LAYER_STATE_INFORMATION))
                   .datosMiniatura(directory.getDescription(PhotoshopDirectory.TAG_THUMBNAIL_OLD));
        }
    }
}
