package org.example.analisis.archivosimagen.validacion.service;

import org.example.analisis.archivosimagen.archivo.modelo.ArchivoImage;
import org.example.analisis.reglavalidacion.interfaz.IReglaValidacion;
import org.example.analisis.reglavalidacion.modelo.ResultadoValidacion;
import org.example.analisis.reglavalidacion.modelo.VeredictoFinal;

import java.util.ArrayList;
import java.util.List;

public class ValidadorArchivoImagenService {

    // Lista de reglas genéricas tipadas para ArchivoImage
    private final List<IReglaValidacion<ArchivoImage>> reglas = new ArrayList<>();

    public void registrarRegla(IReglaValidacion<ArchivoImage> regla) {
        this.reglas.add(regla);
    }

    public VeredictoFinal validar(ArchivoImage imagen) {
        VeredictoFinal veredicto = new VeredictoFinal();

        System.out.println("\n🔍 Iniciando peritaje forense: " + imagen.getNombreArchivo());

        for (IReglaValidacion<ArchivoImage> regla : reglas) {
            // 1. Ejecutar la validación individual
            ResultadoValidacion resultado = regla.validar(imagen);

            // 2. Agregar al veredicto (Tu clase ya maneja internamente el esRechazado y razonRechazo)
            veredicto.agregarResultado(resultado, regla.esCritica());

            // 3. Cortocircuito si tu veredicto marca rechazo
            if (veredicto.isEsRechazado()) {
                System.err.println("🛑 CORTOCIRCUITO: Análisis abortado por regla crítica [" + resultado.getNombreRegla() + "]");
                break;
            }
        }

        return veredicto;
    }
}