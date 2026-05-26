package org.example.analisis.archivosimagen.archivo;


import org.example.analisis.archivosimagen.archivo.modelo.ArchivoImage;
import org.example.analisis.archivosimagen.archivo.service.ArchivoImagenService;

import java.io.File;

public class AnalisisImagenMain {
    public static void main(String[] args) {
        ArchivoImagenService service = new ArchivoImagenService();

        // Rutas de prueba
        String rutaBase = "src/main/resources/imagenes_prueba/";
        File fOriginal = new File(rutaBase + "original.png");
        File fRecorte = new File(rutaBase + "recorte_de_pantalla.png");

        System.out.println("SISTEMA DE PERITAJE TÉCNICO DE IMÁGENES INICIADO...");

        // ANALIZAR ARCHIVO 1
        ArchivoImage img1 = service.construirAnalisisCompleto(fOriginal);
        System.out.println(img1);
        verificarCoherencia(img1);

        // ANALIZAR ARCHIVO 2
        ArchivoImage img2 = service.construirAnalisisCompleto(fRecorte);
        System.out.println(img2);
        verificarCoherencia(img2);
    }

    /**
     * Ejemplo de cómo compararías los dos pilares en el futuro
     */
    private static void verificarCoherencia(ArchivoImage img) {
        double dpiMeta = img.getMetadatos().getDpiCalculado();
        int dpiBinario = img.getEstructura().getDpiX();

        System.out.println(">>> COMPROBACIÓN DE COHERENCIA:");
        if (Math.abs(dpiMeta - dpiBinario) < 1.0) {
            System.out.println("✅ COHERENTE: Los metadatos coinciden con la estructura física.");
        } else {
            System.err.println("❌ ALERTA: Desviación entre metadatos y estructura (Posible manipulación).");
        }

        if (dpiBinario >= 300) {
            System.out.println("💎 ESTADO: ARCHIVO PROFESIONAL (ALTA CALIDAD)");
        } else {
            System.out.println("📺 ESTADO: ARCHIVO DE PANTALLA / WEB");
        }
        System.out.println("==================================================\n");
    }
}