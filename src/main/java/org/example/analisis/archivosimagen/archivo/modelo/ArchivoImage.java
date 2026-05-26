package org.example.analisis.archivosimagen.archivo.modelo;

import lombok.*;
import org.example.analisis.archivosimagen.estructura.modelo.EstructuraImagen;
import org.example.analisis.archivosimagen.metadatos.modelo.MetadatosImagen;
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

