package org.example.borradores.archivosimagen.archivo.modelo;

import lombok.*;
import org.example.borradores.archivosimagen.estructura.modelo.EstructuraImagen;
import org.example.borradores.archivosimagen.metadatos.modelo.MetadatosImagen;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoImage {
    private String nombreArchivo;
    private String rutaAbsoluta;

    // Los dos pilares del análisis
    private MetadatosImagen metadatos;   // Datos de librerías
    private EstructuraImagen estructura; // Datos binarios manuales


}

