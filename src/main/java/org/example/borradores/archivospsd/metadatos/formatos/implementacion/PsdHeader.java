package org.example.borradores.archivospsd.metadatos.formatos.implementacion;

import org.example.borradores.archivospsd.metadatos.formatos.interfaz.IMetadataExtractor;
import org.example.borradores.archivospsd.metadatos.modelo.MetadatosPSD.MetadatosPSDBuilder;

import com.drew.metadata.Metadata;
import com.drew.metadata.photoshop.PsdHeaderDirectory;

public class PsdHeader implements IMetadataExtractor {
  
    @Override
    public void extract(Metadata metadata, MetadatosPSDBuilder builder) {
      PsdHeaderDirectory directory = metadata.getFirstDirectoryOfType(PsdHeaderDirectory.class);
        if (directory != null) {
            builder.cantidadCanales(directory.getInteger(PsdHeaderDirectory.TAG_CHANNEL_COUNT))
                   .altoImagen(directory.getInteger(PsdHeaderDirectory.TAG_IMAGE_HEIGHT))
                   .anchoImagen(directory.getInteger(PsdHeaderDirectory.TAG_IMAGE_WIDTH))
                   .bitsPorCanal(directory.getInteger(PsdHeaderDirectory.TAG_BITS_PER_CHANNEL))
                   .modoColor(directory.getDescription(PsdHeaderDirectory.TAG_COLOR_MODE));
        }
    }
}
