package org.example.analisis.core.rules.psd;

import org.example.analisis.core.rules.IReglaValidacion;
import org.example.analisis.core.model.psd.ArchivoPSD;
import org.example.analisis.core.model.validacion.ResultadoValidacion;

public class ReglaFormatoPsd implements IReglaValidacion<ArchivoPSD> {
    @Override
    public ResultadoValidacion validar(ArchivoPSD psd) {
        boolean esPsd = "PSD".equalsIgnoreCase(psd.getMetadatos().getNombreArchivoDetectado());
        return ResultadoValidacion.builder()
                .nombreRegla("Formato de Archivo")
                .esValido(esPsd)
                .mensaje(esPsd ? "OK" : "No es un archivo PSD real")
                .build();
    }
    @Override public boolean esCritica() { return true; }
}
