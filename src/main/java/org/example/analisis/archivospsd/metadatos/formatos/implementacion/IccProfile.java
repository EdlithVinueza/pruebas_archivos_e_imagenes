package org.example.analisis.archivospsd.metadatos.formatos.implementacion;

import org.example.analisis.archivospsd.metadatos.formatos.interfaz.IMetadataExtractor;
import org.example.analisis.archivospsd.metadatos.modelo.MetadatosPSD.MetadatosPSDBuilder;

import com.drew.metadata.Metadata;
import com.drew.metadata.icc.IccDirectory;

public class IccProfile implements IMetadataExtractor {

    @Override
    public void extract(Metadata metadata, MetadatosPSDBuilder builder) {
        IccDirectory directory = metadata.getFirstDirectoryOfType(IccDirectory.class);
        if (directory != null) {
            builder.tienePerfilIcc(true)
                   .descripcionPerfilIcc(directory.getDescription(IccDirectory.TAG_TAG_desc))
                   .clasePerfilIcc(directory.getDescription(IccDirectory.TAG_PROFILE_CLASS))
                   .espacioColorIcc(directory.getDescription(IccDirectory.TAG_COLOR_SPACE))
                   .copyrightIcc(directory.getDescription(IccDirectory.TAG_TAG_cprt));
        }
    }

}
