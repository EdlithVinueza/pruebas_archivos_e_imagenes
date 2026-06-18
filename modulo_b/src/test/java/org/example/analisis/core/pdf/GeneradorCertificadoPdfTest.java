package org.example.analisis.core.pdf;

import org.example.analisis.core.model.forense.PayloadForense;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorCertificadoPdfTest {

    @Test
    void testGenerarCertificadoFisico() {
        // 1. Crear un PayloadForense de prueba con datos simulados (Mock)
        PayloadForense payload = new PayloadForense();
        payload.setMetadataVersion("1.1");
        
        PayloadForense.DatosCertificado datosCertificado = new PayloadForense.DatosCertificado();
        datosCertificado.setIdCertificado("CERT-2026-9941A");
        datosCertificado.setFechaEmision("2026-06-17 19:20:00 UTC");
        datosCertificado.setEstadoInicial("EMITIDO_VALIDO");
        payload.setDatosDelCertificado(datosCertificado);
        
        PayloadForense.Autor autor = new PayloadForense.Autor();
        autor.setNombre("Sophia Vance");
        autor.setIdInstitucional("ID-AN-2026-XYZ");
        payload.setAutor(autor);
        
        PayloadForense.Obra obra = new PayloadForense.Obra();
        obra.setTitulo("El Eco del Mañana");
        obra.setFechaDeclaradaCreacion("2026-06-15 14:30:00 UTC");
        obra.setSoftwareOriginal("FireAlpaca v2.11.3 / MediBang Paint Pro");
        obra.setHardwareAdicional("Huion Inspiroy H610PRO v2");
        obra.setDetallesTecnicos("Formato original .mdp, exportado a PNG sin compresión.");
        payload.setObra(obra);
        
        PayloadForense.AnalisisForenseDigital analisis = new PayloadForense.AnalisisForenseDigital();
        analisis.setDimensiones("4000 x 3000 píxeles");
        analisis.setSha256Criptografico("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        analisis.setPhashPerceptual("8f3c3c3c3c3c3c3c");
        payload.setAnalisisForenseDigital(analisis);

        // 2. Definir la ruta donde se guardará el PDF de prueba
        String rutaSalida = "build/resultados_test/Certificado_Prueba.pdf";
        File archivoPdf = new File(rutaSalida);

        // Si el archivo ya existía de una prueba anterior, lo borramos
        if (archivoPdf.exists()) {
            archivoPdf.delete();
        }

        // Leer la imagen de prueba para inyectarla en el certificado
        String imagenBase64 = null;
        try {
            File imgFile = new File("C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/imagenes/girasol-original.png");
            if (imgFile.exists()) {
                byte[] fileContent = java.nio.file.Files.readAllBytes(imgFile.toPath());
                imagenBase64 = java.util.Base64.getEncoder().encodeToString(fileContent);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de prueba: " + e.getMessage());
        }

        // 3. Ejecutar el generador con la imagen real en base64
        GeneradorCertificadoPdf generador = new GeneradorCertificadoPdf();
        generador.generarCertificado(payload, rutaSalida, imagenBase64);

        // 4. Validar que el archivo se haya creado exitosamente
        assertTrue(archivoPdf.exists(), "El archivo PDF debería haberse creado en el disco duro.");
        assertTrue(archivoPdf.length() > 0, "El archivo PDF no debería estar vacío.");
        
        System.out.println("TEST EXITOSO: Puedes ver tu certificado PDF en: " + archivoPdf.getAbsolutePath());
    }
}
