package org.example.analisis.core.service;

import org.example.analisis.core.model.base.ArchivoBase;
import org.example.analisis.core.ports.ArchivoProcessor;

import java.io.File;
import java.util.List;

public class ArchivoProcessorFactory {
    private final List<ArchivoProcessor<? extends ArchivoBase>> processors;

    public ArchivoProcessorFactory(List<ArchivoProcessor<? extends ArchivoBase>> processors) {
        this.processors = processors;
    }

    public ArchivoProcessor<? extends ArchivoBase> getProcessor(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("El archivo es nulo o no existe.");
        }
        return processors.stream()
                .filter(p -> p.soporta(file))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró un procesador para la firma binaria del archivo: " + file.getName()
                ));
    }
}
