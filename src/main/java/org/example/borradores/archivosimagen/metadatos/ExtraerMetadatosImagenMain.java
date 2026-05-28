package org.example.analisis.archivosimagen.metadatos;

import org.example.analisis.archivosimagen.metadatos.servicio.MetadatosImagenService;
import org.example.analisis.archivosimagen.metadatos.modelo.MetadatosImagen;

import java.io.File;

public class ExtraerMetadatosImagenMain {
    public static void main(String[] args) {

        // La ruta de la imagen que quieres probar

        // File archivo = new File("src/main/resources/imagenes_prueba/Entre cabellos rojos y girasoles.jpg");

        //File archivo = new File("src/main/resources/imagenes_prueba/f0fcd63ef1688bc1c6c8c3414975781c.jpg");

         File archivo = new File("src/main/resources/imagenes_prueba/original.png");

        //File archivo = new File("src/main/resources/imagenes_prueba/recorte_de_pantalla.png");

        if (!archivo.exists()) {
            System.err.println("No se encontró el archivo en: " + archivo);
            return;
        }

        System.out.println("Iniciando análisis forense de la imagen: " + archivo.getName());
        System.out.println("---------------------------------------------------------");

        try {
            // Instanciamos nuestro servicio
            MetadatosImagenService servicio = new MetadatosImagenService();

            // Procesamos la imagen pasando por todas las estrategias
            MetadatosImagen resultado = servicio.procesarArchivo(archivo);

            // Imprimimos el resultado (que usará el toString() formateado de la clase)
            System.out.println(resultado.toString());

        } catch (Exception e) {
            System.err.println("Hubo un error en la ejecución:");
            e.printStackTrace();
        }
    }
}
