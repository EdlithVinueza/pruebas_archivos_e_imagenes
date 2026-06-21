package analisis.core.service;

import analisis.core.model.base.ArchivoBase;
import analisis.core.model.validacion.ResultadoValidacion;
import analisis.core.model.validacion.VeredictoFinal;
import analisis.core.rules.IReglaValidacion;

import java.util.ArrayList;
import java.util.List;

public class ValidadorGenericoService<T extends ArchivoBase> {
    private final List<IReglaValidacion<T>> reglas = new ArrayList<>();

    public void registrarRegla(IReglaValidacion<T> regla) {
        this.reglas.add(regla);
    }

    public VeredictoFinal validar(T objeto) {
        VeredictoFinal veredicto = new VeredictoFinal();

        System.out.println("\nIniciando peritaje forense: " + objeto.getNombreArchivo());

        for (IReglaValidacion<T> regla : reglas) {
            // 1. Ejecutar la validación individual
            ResultadoValidacion resultado = regla.validar(objeto);

            // 2. Agregar al veredicto
            veredicto.agregarResultado(resultado, regla.esCritica());

            // 3. Cortocircuito si tu veredicto marca rechazo
            if (veredicto.isEsRechazado()) {
                System.err.println(" CORTOCIRCUITO: Análisis abortado por regla crítica [" + resultado.getNombreRegla() + "]");
                break;
            }
        }

        return veredicto;
    }
}
