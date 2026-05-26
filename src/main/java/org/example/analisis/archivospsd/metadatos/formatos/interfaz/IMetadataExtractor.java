package org.example.analisis.archivospsd.metadatos.formatos.interfaz;

import org.example.analisis.archivospsd.metadatos.modelo.MetadatosPSD;

import com.drew.metadata.Metadata;

public interface IMetadataExtractor {
    // Recibe el objeto Metadata (ya procesado) y el builder de tu DTO
    void extract(Metadata metadata, MetadatosPSD.MetadatosPSDBuilder builder);
}