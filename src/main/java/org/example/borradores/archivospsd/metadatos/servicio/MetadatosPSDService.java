package org.example.borradores.archivospsd.metadatos.servicio;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.example.borradores.archivospsd.metadatos.formatos.implementacion.*;
import org.example.borradores.archivospsd.metadatos.formatos.interfaz.IMetadataExtractor;
import org.example.borradores.archivospsd.metadatos.modelo.MetadatosPSD;
public class MetadatosPSDService {
    private final List<IMetadataExtractor> extractors;

    public MetadatosPSDService() {
        // Ordenados de MENOS fiable a MÁS fiable (Last-Win)
        this.extractors = Arrays.asList(
                new Jfif(),      // Muy genérico (prioridad baja)
                new Exif(),      // Datos de cámara/motor (prioridad media)
                new Iptc(),      // Datos de prensa (prioridad media)
                new Xmp(),       // Datos Adobe (prioridad alta)
                new Photoshop(), // Recursos específicos (prioridad alta)
                new PsdHeader(), // LA VERDAD BINARIA (prioridad máxima)
                new FileType()   // Identificación final
        );
    }

    public MetadatosPSD procesarArchivo(File archivo) {
        MetadatosPSD.MetadatosPSDBuilder builder = MetadatosPSD.builder();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(archivo);
            for (IMetadataExtractor extractor : extractors) {
                // Cada extractor intenta llenar su parte.
                // Si el siguiente tiene un dato mejor, lo sobreescribirá en el builder.
                extractor.extract(metadata, builder);
            }
        } catch (Exception e) {
            System.err.println("Error crítico: " + e.getMessage());
        }
        return builder.build();
    }
}