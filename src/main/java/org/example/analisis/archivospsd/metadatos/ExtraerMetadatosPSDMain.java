package org.example.analisis.archivospsd.metadatos;

import org.example.analisis.archivospsd.metadatos.modelo.MetadatosPSD;
import org.example.analisis.archivospsd.metadatos.servicio.MetadatosPSDService;

import java.io.File;

public class ExtraerMetadatosPSDMain {
    public static void main(String[] args) {
        MetadatosPSDService service = new MetadatosPSDService();

        // 1. QUITA LA PRIMERA BARRA.
        // Si la carpeta "archivos_psd_prueba" está en la raíz de tu proyecto:
        File miArchivo = new File(
                "src/main/resources/archivos_psd_prueba/Chica de cabello y girasoles - 05-02-2026.psd");

        // 2. VERIFICACIÓN ANTES DE PROCESAR
        if (!miArchivo.exists()) {
            System.err.println("ERROR: El archivo no existe en la ruta: " + miArchivo.getAbsolutePath());
            System.err.println(
                    "Asegúrate de que la carpeta 'archivos_psd_prueba' esté en: " + System.getProperty("user.dir"));
            return;
        }

        // 3. PROCESAR
        MetadatosPSD m = service.procesarArchivo(miArchivo);

        // 4. IMPRIMIR
        System.out.println(m);

        // 5. Verificación de lógica
        System.out.println("¿Resolución Pro?: " + (m.tieneResolucionProfesional() ? "SÍ" : "NO"));
        System.out.println("Software usado: " + (m.getSoftware() != null ? m.getSoftware() : "N/A"));
    }
}