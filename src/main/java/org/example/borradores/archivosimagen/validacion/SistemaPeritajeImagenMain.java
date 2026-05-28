package org.example.analisis.archivosimagen.validacion;

import org.example.analisis.archivosimagen.archivo.modelo.ArchivoImage;
import org.example.analisis.archivosimagen.archivo.service.ArchivoImagenService;
import org.example.analisis.archivosimagen.validacion.implementacionregla.ReglaAnalisisOrigen;
import org.example.analisis.archivosimagen.validacion.implementacionregla.ReglaCoherenciaDpi;
import org.example.analisis.archivosimagen.validacion.implementacionregla.ReglaFirmaEstructural;
import org.example.analisis.archivosimagen.validacion.service.ValidadorArchivoImagenService;
import org.example.analisis.reglavalidacion.modelo.VeredictoFinal;

import java.io.File;

public class SistemaPeritajeImagenMain    {
    public static void main(String[] args) {
    // 1. Instanciar Servicios
    ArchivoImagenService fabrica = new ArchivoImagenService();
    ValidadorArchivoImagenService validador = new ValidadorArchivoImagenService();

    // 2. Registrar reglas (El orden define el flujo de seguridad)
    validador.registrarRegla(new ReglaFirmaEstructural()); // ¿Son bytes de imagen reales?
    validador.registrarRegla(new ReglaCoherenciaDpi());    // ¿Los metadatos coinciden con los bytes?
    validador.registrarRegla(new ReglaAnalisisOrigen());   // ¿Es recorte, web o pro?

    // 3. Definir archivos a probar (Original vs Recorte)
    String rutaBase = "src/main/resources/imagenes_prueba/";
    File[] archivos = {
            new File(rutaBase + "original.png"),
            new File(rutaBase + "recorte_de_pantalla.png")
    };

    for (File f : archivos) {
        if (!f.exists()) {
            System.err.println("Archivo no encontrado: " + f.getName());
            continue;
        }

        // PASO A: CONSTRUCCIÓN INTEGRADA
        ArchivoImage imagenAnalizada = fabrica.construirAnalisisCompleto(f);

        // PASO B: VALIDACIÓN CON VEREDICTO
        VeredictoFinal veredicto = validador.validar(imagenAnalizada);

        // PASO C: MOSTRAR RESULTADOS DEL VEREDICTO
        imprimirResultadosFinaes(imagenAnalizada, veredicto);
    }
}

private static void imprimirResultadosFinaes(ArchivoImage img, VeredictoFinal v) {
    System.out.println(img.getMetadatos()); // Reporte técnico de metadatos
    System.out.println(img.getEstructura()); // Reporte técnico de bytes

    System.out.println("\n>>>> RESULTADO DEL PERITAJE:");
    System.out.println("• Reglas evaluadas: " + v.getTotalReglasEvaluadas());
    System.out.println("• Reglas exitosas:  " + v.getReglasExitosas());

    if (v.isEsRechazado()) {
        System.err.println(" ESTADO: ARCHIVO RECHAZADO");
        System.err.println(" MOTIVO: " + v.getRazonRechazo());
        // Imprimir solo los fallos
        v.getDetalles().stream()
                .filter(res -> !res.isEsValido())
                .forEach(res -> System.err.println("   └─ Detalle: " + res.getMensaje()));
    } else {
        System.out.println(" ESTADO: ARCHIVO AUTÉNTICO / PROFESIONAL");
        System.out.println(" El archivo cumple con todos los estándares de exportación.");
    }
    System.out.println("\n" + "=".repeat(50) + "\n");
}
}
