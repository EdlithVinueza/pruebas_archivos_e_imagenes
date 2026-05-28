package org.example.borradores.archivospsd.validacion.service;

import org.example.borradores.archivospsd.archivo.modelo.ArchivoPSD;
import org.example.borradores.reglavalidacion.interfaz.IReglaValidacion;
import org.example.borradores.reglavalidacion.modelo.ResultadoValidacion;
import org.example.borradores.reglavalidacion.modelo.VeredictoFinal;

import java.util.ArrayList;
import java.util.List;

public class ValidadorArchivoPSDService {
    private final List<IReglaValidacion> reglas = new ArrayList<>();

    /**
     * Permite añadir reglas al motor de validación.
     * El orden en que las añadas es el orden en que se ejecutarán.
     */
    public void registrarRegla(IReglaValidacion regla) {
        this.reglas.add(regla);
    }

    /**
     * Ejecuta todas las reglas sobre el archivo.
     * Si una regla es CRÍTICA y falla, el proceso se detiene inmediatamente.
     */
    public VeredictoFinal validar(ArchivoPSD psd) {
        VeredictoFinal veredicto = new VeredictoFinal();

        System.out.println("--- Iniciando validación de: " + psd.getNombreArchivo() + " ---");

        for (IReglaValidacion regla : reglas) {
            ResultadoValidacion resultado = regla.validar(psd);

            // Registramos el resultado en el veredicto
            veredicto.agregarResultado(resultado, regla.esCritica());

            // LOGICA DE CORTOCIRCUITO
            if (!resultado.isEsValido() && regla.esCritica()) {
                System.err.println("!! DETENIDO: Falló regla crítica: " + resultado.getNombreRegla());
                break; // No evaluamos nada más
            }
        }

        return veredicto;
    }

}
