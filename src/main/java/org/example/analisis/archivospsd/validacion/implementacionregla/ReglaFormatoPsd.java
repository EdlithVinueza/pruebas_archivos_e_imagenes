package org.example.analisis.archivospsd.validacion.implementacionregla;

import org.example.analisis.archivospsd.archivo.modelo.ArchivoPSD;
import org.example.analisis.reglavalidacion.interfaz.IReglaValidacion;
import org.example.analisis.reglavalidacion.modelo.ResultadoValidacion;

public class ReglaFormatoPsd implements IReglaValidacion<ArchivoPSD>  {
    @Override
    public ResultadoValidacion validar(ArchivoPSD psd) {
        boolean esPsd = "PSD".equalsIgnoreCase(psd.getMetadatos().getNombreArchivoDetectado());
        return ResultadoValidacion.builder()
                .nombreRegla("Formato de Archivo")
                .esValido(esPsd)
                .mensaje(esPsd ? "OK" : "No es un archivo PSD real")
                .build();
    }
    @Override public boolean esCritica() { return true; } // DETIENE TODO
}