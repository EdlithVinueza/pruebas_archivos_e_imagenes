package org.example.borradores.archivosimagen.archivo.service;


import org.example.borradores.archivosimagen.archivo.modelo.ArchivoImage;
import org.example.borradores.archivosimagen.metadatos.servicio.MetadatosImagenService;
import org.example.borradores.archivosimagen.estructura.service.EstructuraImagenService;

import java.io.File;

public class ArchivoImagenService {

    private final MetadatosImagenService metadatosService;
    private final EstructuraImagenService estructuraService;

    public ArchivoImagenService() {
        this.metadatosService = new MetadatosImagenService();
        this.estructuraService = new EstructuraImagenService();
    }

    public ArchivoImage construirAnalisisCompleto(File archivo) {
        if (archivo == null || !archivo.exists()) {
            throw new RuntimeException("Archivo no válido para el análisis.");
        }

        // Ejecutamos ambos análisis en paralelo (cada uno por su lado)
        var meta = metadatosService.procesarArchivo(archivo);
        var estru = estructuraService.construirAnalisis(archivo);

        // Ensamblamos el objeto raíz
        return ArchivoImage.builder()
                .nombreArchivo(archivo.getName())
                .rutaAbsoluta(archivo.getAbsolutePath())
                .metadatos(meta)
                .estructura(estru)
                .build();
    }
}