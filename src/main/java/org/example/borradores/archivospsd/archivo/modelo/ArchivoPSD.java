package org.example.borradores.archivospsd.archivo.modelo;

import lombok.*;
import org.example.borradores.archivospsd.estructura.modelo.CapaPSD;
import org.example.borradores.archivospsd.metadatos.modelo.MetadatosPSD;

import java.util.List;

@Data
@Builder
public class ArchivoPSD {
    private String nombreArchivo;
    private MetadatosPSD metadatos;
    private List<CapaPSD> capas;
}