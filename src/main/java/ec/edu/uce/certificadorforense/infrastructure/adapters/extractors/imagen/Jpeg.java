package analisis.adapters.extractors.imagen;

import com.drew.metadata.Metadata;
import analisis.adapters.extractors.MetadataExtractor;
import analisis.core.model.imagen.MetadatosImagen;

public class Jpeg implements MetadataExtractor<MetadatosImagen.MetadatosImagenBuilder> {
    @Override
    public void extraer(Metadata metadata, MetadatosImagen.MetadatosImagenBuilder builder) {
        com.drew.metadata.jpeg.JpegDirectory dir = metadata.getFirstDirectoryOfType(com.drew.metadata.jpeg.JpegDirectory.class);
        if (dir != null) {
            builder.ancho(dir.getInteger(com.drew.metadata.jpeg.JpegDirectory.TAG_IMAGE_WIDTH))
                    .alto(dir.getInteger(com.drew.metadata.jpeg.JpegDirectory.TAG_IMAGE_HEIGHT));
        }
    }
}
