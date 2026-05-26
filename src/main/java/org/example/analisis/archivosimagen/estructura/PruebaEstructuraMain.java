package org.example.analisis.archivosimagen.estructura;

import org.example.analisis.archivosimagen.estructura.modelo.EstructuraImagen;
import org.example.analisis.archivosimagen.estructura.service.EstructuraImagenService;

import java.io.File;

public class PruebaEstructuraMain {
    public static void main(String[] args) {
        EstructuraImagenService service = new EstructuraImagenService();

        // Cambia estas rutas por las de tus archivos reales
        String rutaBase = "src/main/resources/imagenes_prueba/";
        File original = new File(rutaBase + "original.png");
        File recorte = new File(rutaBase + "recorte_de_pantalla.png");

        System.out.println("=== COMPROBACIÓN DE CREACIÓN DE OBJETO ESTRUCTURA ===");

        // --- PROBAR IMAGEN 1 ---
        validarYMostrar(service, original, "ARCHIVO ORIGINAL");

        // --- PROBAR IMAGEN 2 ---
        validarYMostrar(service, recorte, "RECORTE DE PANTALLA");
    }

    private static void validarYMostrar(EstructuraImagenService service, File f, String titulo) {
        System.out.println("\n>>> " + titulo);
        if (!f.exists()) {
            System.err.println("Archivo no encontrado en: " + f.getAbsolutePath());
            return;
        }

        // Llamada al objeto construido
        EstructuraImagen info = service.construirAnalisis(f);

        // IMPRESIÓN USANDO TU TOSTRING
        System.out.println(info.toString());

        // Comprobación de que el objeto no está vacío
        if (info.getDpiX() > 0 && info.getFirmaHex() != null) {
            System.out.println("✅ Objeto creado y guardado correctamente en memoria.");
        } else {
            System.out.println("❌ El objeto parece estar vacío o mal construido.");
        }
    }
}