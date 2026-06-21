package analisis.adapters.extractors.imagen;

import com.drew.metadata.Metadata;
import analisis.adapters.extractors.MetadataExtractor;
import analisis.core.model.imagen.MetadatosImagen;

public class Icc implements MetadataExtractor<MetadatosImagen.MetadatosImagenBuilder> {
    @Override
    public void extraer(Metadata metadata, MetadatosImagen.MetadatosImagenBuilder builder) {
        com.drew.metadata.icc.IccDirectory dir = metadata.getFirstDirectoryOfType(com.drew.metadata.icc.IccDirectory.class);
        if (dir != null) {
            builder.tienePerfilIcc(true)
                    .descripcionPerfilIcc(dir.getDescription(com.drew.metadata.icc.IccDirectory.TAG_TAG_desc));
        }
    }
}
