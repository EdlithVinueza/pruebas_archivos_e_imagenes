package org.example.borradores.archivospsd.validacion.implementacionregla;

import org.example.borradores.archivospsd.archivo.modelo.ArchivoPSD;
import org.example.borradores.archivospsd.estructura.modelo.CapaPSD;
import org.example.borradores.reglavalidacion.interfaz.IReglaValidacion;
import org.example.borradores.reglavalidacion.modelo.ResultadoValidacion;


public class ReglaComplejidadDiseno implements IReglaValidacion<ArchivoPSD>  {
    @Override
    public ResultadoValidacion validar(ArchivoPSD psd) {
        int totalCapas = psd.getCapas().size();

        // Contamos cuántas capas tienen "valor agregado"
        long conTrabajo = psd.getCapas().stream().filter(c ->
                c.isTieneMascaraCapa() || c.isTieneEfectos() || c.isEsClippingMask() ||
                        c.getTipo() == CapaPSD.Tipo.TEXTO || !c.getBlendModeKey().equals("norm") ||
                        c.getOpacidadRaw() < 255
        ).count();

        boolean esValido;
        String mensaje;

        if (totalCapas <= 3) {
            // Si tiene 3 o menos capas, EXIGIMOS que al menos 1 sea técnica
            esValido = conTrabajo >= 1;
            mensaje = "Pocas capas (" + totalCapas + "). Requiere al menos 1 capa técnica. Encontradas: " + conTrabajo;
        } else {
            // Si tiene más de 3, EXIGIMOS al menos 2 capas técnicas
            esValido = conTrabajo >= 2;
            mensaje = "Estructura compleja (" + totalCapas + " capas). Requiere al menos 2 capas técnicas. Encontradas: " + conTrabajo;
        }

        return ResultadoValidacion.builder()
                .nombreRegla("Autenticidad Técnica")
                .esValido(esValido)
                .mensaje(esValido ? "OK" : mensaje)
                .build();
    }

    @Override public boolean esCritica() { return true; } // DESCARTA AL INSTANTE
}