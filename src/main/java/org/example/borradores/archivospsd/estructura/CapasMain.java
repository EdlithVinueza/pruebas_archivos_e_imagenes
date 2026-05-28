package org.example.borradores.archivospsd.estructura;

import org.example.borradores.archivospsd.estructura.extractor.ExtractorCapasPSD;
import org.example.borradores.archivospsd.estructura.modelo.CapaPSD;

import java.io.File;
import java.util.List;

public class CapasMain  {
public static void main(String[] args) {
    // 1. Definir la ruta del archivo (Asegúrate de que sea correcta)
    String rutaRelativa = "src/main/resources/archivos_psd_prueba/Chica de cabello y girasoles - 05-02-2026.psd";
    File archivo = new File(rutaRelativa);

    System.out.println("====================================================");
    System.out.println("   TEST DE EXTRACCIÓN BINARIA DE CAPAS");
    System.out.println("====================================================");
    System.out.println("Archivo: " + archivo.getAbsolutePath());
    System.out.println("¿Existe?: " + (archivo.exists() ? "SÍ ✅" : "NO ❌"));

    if (!archivo.exists()) return;

    // 2. Ejecutar la extracción
    System.out.println("\nLeyendo estructura de capas...");
    List<CapaPSD> capas = ExtractorCapasPSD.extraer(archivo.getAbsolutePath());

    // 3. Mostrar resultados detallados
    if (capas.isEmpty()) {
        System.out.println("\n⚠️ No se encontraron capas. El archivo podría estar:");
        System.out.println("   - Aplanado (solo fondo).");
        System.out.println("   - Guardado sin compatibilidad de capas.");
        System.out.println("   - Truncado (error EOF).");
    } else {
        System.out.println("\nSe detectaron " + capas.size() + " capas:\n");

        // Cabecera de la tabla
        System.out.printf("%-3s | %-20s | %-12s | %-8s | %-10s | %-5s%n",
                "Idx", "Nombre", "Tipo", "Visible", "Opacidad", "Tam.");
        System.out.println("--------------------------------------------------------------------------------");

        for (CapaPSD c : capas) {
            System.out.printf("%-3d | %-20s | %-12s | %-8s | %-10s | %dx%d%n",
                    c.getIndice(),
                    truncarNombre(c.getNombre(), 20),
                    c.getTipo(),
                    c.isVisible() ? "SÍ" : "NO",
                    c.getOpacidadRaw() + " (raw)",
                    c.getAncho(),
                    c.getAlto()
            );

            // Mostrar flags técnicos adicionales
            if (c.isTieneMascaraCapa() || c.isTieneEfectos() || c.isEsClippingMask()) {
                System.out.print("    └─ Detalle:");
                if (c.isEsClippingMask()) System.out.print(" [Recorte]");
                if (c.isTieneMascaraCapa()) System.out.print(" [Máscara]");
                if (c.isTieneEfectos()) System.out.print(" [Efectos FX]");
                System.out.println();
            }
        }
    }
    System.out.println("\n====================================================");
}

private static String truncarNombre(String nombre, int max) {
    if (nombre == null) return "n/a";
    return nombre.length() > max ? nombre.substring(0, max - 3) + "..." : nombre;
}
}