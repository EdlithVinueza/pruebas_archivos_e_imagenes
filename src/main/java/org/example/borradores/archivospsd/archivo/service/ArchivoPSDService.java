package org.example.borradores.archivospsd.archivo.service;

import org.example.borradores.archivospsd.archivo.modelo.ArchivoPSD;
import org.example.borradores.archivospsd.estructura.extractor.ExtractorCapasPSD;
import org.example.borradores.archivospsd.estructura.modelo.CapaPSD;
import org.example.borradores.archivospsd.metadatos.modelo.MetadatosPSD;
import org.example.borradores.archivospsd.metadatos.servicio.MetadatosPSDService;

import java.io.File;
import java.util.List;

public class ArchivoPSDService {

    private final MetadatosPSDService metadatosService;

    public ArchivoPSDService() {
        // Inicializamos el servicio de metadatos
        this.metadatosService = new MetadatosPSDService();
    }

    /**
     * Este método es tu "Fábrica". Toma un archivo físico y
     * construye el objeto completo ArchivoPSD.
     */
    public ArchivoPSD construirArchivoCompleto(File file) {

        // 1. Extraer los metadatos (Ancho, Alto, DPI, Software...)
        MetadatosPSD metadatos = metadatosService.procesarArchivo(file);

        // 2. Extraer las capas (Lectura binaria)
        // Usamos el método estático de tu ExtractorCapasPSD
        List<CapaPSD> capas = ExtractorCapasPSD.extraer(file.getAbsolutePath());

        // 3. Unir todo en el objeto ArchivoPSD usando el Builder
        return ArchivoPSD.builder()
                .nombreArchivo(file.getName())
                .metadatos(metadatos)
                .capas(capas)
                .build();
    }
}