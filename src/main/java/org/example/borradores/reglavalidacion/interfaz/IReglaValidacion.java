package org.example.borradores.reglavalidacion.interfaz;

import org.example.borradores.reglavalidacion.modelo.ResultadoValidacion;

public interface IReglaValidacion<T> {
    // T será ArchivoPSD, ArchivoImagen, etc.
    ResultadoValidacion validar(T objeto);

    boolean esCritica();
}