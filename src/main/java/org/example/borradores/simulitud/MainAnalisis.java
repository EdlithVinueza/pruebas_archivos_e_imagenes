package org.example.borradores.simulitud;

import org.example.borradores.simulitud.service.ServicioComparador;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MainAnalisis {

    public static void main(String[] args) {
        try {
            ServicioComparador servicio = new ServicioComparador();

            // 1. Definir rutas
            // 'imagenOriginal' es el exportado de tu PSD (usa PNG para no perder calidad antes de la prueba)
            String rutaOriginal = "src/main/resources/archivos_psd_prueba/Chica de cabello y girasoles - 05-02-2026.psd";
            // 'imagenFinal' es la que ya tiene los datos ocultos (la que creaste en el escritorio)
            String rutaFinal = "src/main/resources/imagenes_prueba/original.png";

            // 2. Detectar el formato de la imagen final (.png o .jpg)
            String formatoFinal = obtenerExtension(rutaFinal);
            System.out.println("Detectado formato de salida: " + formatoFinal);

            // 3. Cargar imágenes
            BufferedImage imgOriginalPura = ImageIO.read(new File(rutaOriginal));
            BufferedImage imgEstenografiada = ImageIO.read(new File(rutaFinal));

            // 4. NORMALIZACIÓN:
            // Si la imagen final es JPG, convertimos la original a JPG en memoria
            // para que la comparación sea justa (comprimir ambas por igual).
            BufferedImage imgOriginalNormalizada = normalizarFormato(imgOriginalPura, formatoFinal);

            // 5. EJECUTAR ANÁLISIS
            System.out.println("\nIniciando análisis comparativo...");
            servicio.imprimirAnalisisCompleto(imgOriginalNormalizada, imgEstenografiada);

        } catch (IOException e) {
            System.err.println("Error al cargar las imágenes: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Extrae la extensión del archivo para saber si es png o jpg
     */
    private static String obtenerExtension(String ruta) {
        if (ruta.toLowerCase().endsWith(".png")) return "png";
        if (ruta.toLowerCase().endsWith(".jpg") || ruta.toLowerCase().endsWith(".jpeg")) return "jpg";
        return "png"; // Por defecto
    }

    /**
     * Este método simula guardar y volver a cargar la imagen en el formato destino.
     * Esto asegura que si comparamos contra un JPG, el "ruido" de la compresión JPG
     * esté presente en ambas imágenes y no afecte negativamente al MSE/PSNR.
     */
    private static BufferedImage normalizarFormato(BufferedImage img, String formato) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Simulamos el guardado en el formato destino (jpg o png)
        ImageIO.write(img, formato, baos);
        // Volvemos a cargar los bytes a un BufferedImage
        return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
    }
}