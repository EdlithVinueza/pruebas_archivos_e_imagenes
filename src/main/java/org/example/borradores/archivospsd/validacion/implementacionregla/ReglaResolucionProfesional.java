package org.example.borradores.archivospsd.validacion.implementacionregla;

import org.example.borradores.archivospsd.archivo.modelo.ArchivoPSD;
import org.example.borradores.reglavalidacion.interfaz.IReglaValidacion;
import org.example.borradores.reglavalidacion.modelo.ResultadoValidacion;

public class ReglaResolucionProfesional implements IReglaValidacion<ArchivoPSD>  {
    @Override
    public ResultadoValidacion validar(ArchivoPSD psd) {
        double dpi = psd.getMetadatos().getDpiHorizontal();
        boolean pasa = dpi >= 150.0;
        return ResultadoValidacion.builder()
                .nombreRegla("Calidad de Impresión")
                .esValido(pasa)
                .mensaje(pasa ? "Resolución OK" : "Resolución muy baja: " + dpi + " DPI")
                .build();
    }

    @Override
    public boolean esCritica() {
        return false;
    }
}