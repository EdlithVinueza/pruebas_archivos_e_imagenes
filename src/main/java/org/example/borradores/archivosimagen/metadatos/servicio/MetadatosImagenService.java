package org.example.borradores.archivosimagen.metadatos.servicio;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import org.example.borradores.archivosimagen.metadatos.formatos.implementacion.*;
import org.example.borradores.archivosimagen.metadatos.formatos.interfaz.MetadataExtractor;
import org.example.borradores.archivosimagen.metadatos.modelo.MetadatosImagen;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MetadatosImagenService {

    private final List<MetadataExtractor> extractores;

    public MetadatosImagenService() {

        this.extractores = Arrays.asList(
                new FileType(), // Identifica formato y extensión
                new Png(),      // Datos PNG (pHYs, gAMA, sRGB)
                new Jpeg(),     // Dimensiones JPG
                new Jfif(),     // DPI de JPG
                new Exif(),     // Respaldo de DPI
                new Icc()       // Perfil de color
                );
    }

    public MetadatosImagen procesarArchivo(File archivo) {
        // Inicializamos el objeto vacío usando el patrón Builder de Lombok
        MetadatosImagen.MetadatosImagenBuilder builder = MetadatosImagen.builder();

        try {
            // Leemos todos los directorios del archivo de un solo golpe
            Metadata metadata = ImageMetadataReader.readMetadata(archivo);

            // Iteramos sobre todos nuestros extractores
            for (MetadataExtractor extractor : extractores) {
                try {
                    // Cada uno aportará su granito de arena al Builder.
                    // Si un dato no existe en su formato, simplemente no hará nada,
                    // dejando intacto lo que haya extraído un formato anterior.
                    extractor.extraer(metadata, builder);
                } catch (Exception ex) {
                    System.err.println("Advertencia: El extractor " + extractor.getClass().getSimpleName() +
                            " falló, pero el análisis continuará. Error: " + ex.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error crítico procesando la imagen " + archivo.getName() + ": " + e.getMessage());
        }

        // Retornamos el objeto final construido con la unión de todos los formatos
        return builder.build();
    }
}
