package org.example.analisis.infrastructure.adapters.processors;

import org.example.analisis.core.ports.out.ArchivoProcessorPort;
import org.example.analisis.core.model.imagen.ArchivoImagen;
import org.example.analisis.core.model.imagen.MetadatosImagen;
import org.example.analisis.core.model.imagen.EstructuraImagen;

import java.io.File;

public class ArchivoImagenProcessor implements ArchivoProcessorPort<ArchivoImagen> {

    private final MetadatosImagenService metadatosService;
    private final EstructuraImagenService estructuraService;

    public ArchivoImagenProcessor() {
        this.metadatosService = new MetadatosImagenService();
        this.estructuraService = new EstructuraImagenService();
    }

    @Override
    public ArchivoImagen procesar(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("El archivo es inválido o no existe.");
        }

        // 1. Extraer metadatos
        MetadatosImagen metadatos = metadatosService.procesarArchivo(file);

        // 2. Extraer estructura binaria física
        EstructuraImagen estructura = estructuraService.construirAnalisis(file);

        // 3. Fusionar en el objeto del dominio
        return ArchivoImagen.builder()
                .nombreArchivo(file.getName())
                .rutaAbsoluta(file.getAbsolutePath())
                .tamanoBytes(file.length())
                .metadatos(metadatos)
                .estructura(estructura)
                .build();
    }

    @Override
    public boolean soporta(File file) {
        String formato = NumerosMagicos.detectarFormatoReal(file);
        return "PNG".equals(formato) || "JPEG".equals(formato);
    }
}
