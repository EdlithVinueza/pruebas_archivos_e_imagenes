package org.example.borradores.archivospsd.metadatos.formatos.implementacion;

import org.example.borradores.archivospsd.metadatos.formatos.interfaz.IMetadataExtractor;
import org.example.borradores.archivospsd.metadatos.modelo.MetadatosPSD;

import com.drew.metadata.Metadata;
import com.drew.metadata.photoshop.PhotoshopDirectory;

public class Photoshop implements IMetadataExtractor {
    
    @Override
    public void extract(Metadata metadata, MetadatosPSD.MetadatosPSDBuilder builder) {
        PhotoshopDirectory directory = metadata.getFirstDirectoryOfType(PhotoshopDirectory.class);
        if (directory != null) {
            builder.infoResolucion(directory.getDescription(PhotoshopDirectory.TAG_RESOLUTION_INFO))
                   .infoEstadoCapas(directory.getDescription(PhotoshopDirectory.TAG_LAYER_STATE_INFORMATION))
                   .datosMiniatura(directory.getDescription(PhotoshopDirectory.TAG_THUMBNAIL_OLD));
        }
    }
}