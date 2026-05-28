package org.example.analisis.reglavalidacion.interfaz;

import org.example.analisis.archivospsd.archivo.modelo.ArchivoPSD;
import org.example.analisis.reglavalidacion.modelo.ResultadoValidacion;

public interface IReglaValidacion<T> {
    // T será ArchivoPSD, ArchivoImagen, etc.
    ResultadoValidacion validar(T objeto);

    boolean esCritica();
}