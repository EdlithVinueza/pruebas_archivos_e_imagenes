package org.example.analisis.archivospsd.archivo.modelo;

import lombok.*;
import org.example.analisis.archivospsd.estructura.modelo.CapaPSD;
import org.example.analisis.archivospsd.metadatos.modelo.MetadatosPSD;

import java.util.List;

@Data
@Builder
public class ArchivoPSD {
    private String nombreArchivo;
    private MetadatosPSD metadatos;
    private List<CapaPSD> capas;
}