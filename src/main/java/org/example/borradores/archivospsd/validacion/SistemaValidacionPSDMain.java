package org.example.analisis.archivospsd.validacion;

import org.example.analisis.archivospsd.archivo.modelo.ArchivoPSD;
import org.example.analisis.archivospsd.archivo.service.ArchivoPSDService;
import org.example.analisis.archivospsd.validacion.implementacionregla.ReglaComplejidadDiseno;
import org.example.analisis.archivospsd.validacion.implementacionregla.ReglaFormatoPsd;
import org.example.analisis.archivospsd.validacion.implementacionregla.ReglaImagenPegada;
import org.example.analisis.archivospsd.validacion.implementacionregla.ReglaResolucionProfesional;
import org.example.analisis.archivospsd.validacion.service.ValidadorArchivoPSDService;
import org.example.analisis.reglavalidacion.modelo.VeredictoFinal;

import java.io.File;

public class SistemaValidacionPSDMain {
    public static void main(String[] args) {
        // 1. EL ARCHIVO FÍSICO
        //File miArchivo = new File("src/main/resources/archivos_psd_prueba/Chica de cabello y girasoles - 05-02-2026.psd");
        //File miArchivo = new File("src/main/resources/archivos_psd_prueba/copiado_de_internet_directo.psd");
        File miArchivo = new File("src/main/resources/archivos_psd_prueba/copiado_de_internet_directo_redimencionado.psd");

        //copiado_de_internet_directo_redimencionado
        // 2. CONSTRUCCIÓN (El service que acabamos de crear)
        ArchivoPSDService archivoService = new ArchivoPSDService();
        ArchivoPSD psd = archivoService.construirArchivoCompleto(miArchivo);

        // 3. VALIDACIÓN
        ValidadorArchivoPSDService validador = new ValidadorArchivoPSDService();

        // Registramos tus reglas críticas
        validador.registrarRegla(new ReglaFormatoPsd());      // ¿Es PSD?
        validador.registrarRegla(new ReglaResolucionProfesional()); // ¿Es resolución profesional?
        validador.registrarRegla(new ReglaImagenPegada());    // ¿Es fraude?
        validador.registrarRegla(new ReglaComplejidadDiseno()); // ¿Tiene técnica?

        // 4. EJECUCIÓN Y RESULTADO
        VeredictoFinal veredicto = validador.validar(psd);

        // Imprimir reporte
        System.out.println(psd.getMetadatos()); // Reporte de metadatos
        System.out.println("\n======= RESULTADO DE LA AUDITORÍA =======");
        if (veredicto.isEsRechazado()) {
            System.err.println("❌ ARCHIVO DESCARTADO POR: " + veredicto.getRazonRechazo());
        } else {
            System.out.println("✅ ARCHIVO ACEPTADO: Pasó todos los controles técnicos.");
        }
    }
}