package org.example.borradores.archivospsd.validacion.implementacionregla;

import org.example.borradores.archivospsd.archivo.modelo.ArchivoPSD;
import org.example.borradores.reglavalidacion.interfaz.IReglaValidacion;
import org.example.borradores.reglavalidacion.modelo.ResultadoValidacion;

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