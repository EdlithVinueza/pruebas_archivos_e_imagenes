package org.example.analisis.fase4;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.analisis.core.model.forense.PayloadForense;
import org.example.analisis.infrastructure.adapters.outbound.ImageMetadataInjectorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba la inyección de metadatos forenses en archivos JPEG y PNG
 * usando Chunks (tEXt) y bloques XMP/APP1, preservando los píxeles originales.
 */
public class Paso7_InyeccionMetadataTest {

    private static final String JPG_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/imagenes/girasol-original.jpg";
    private static final String PNG_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/imagenes/girasol-original.png";

    private ImageMetadataInjectorAdapter injector;
    private org.example.analisis.infrastructure.adapters.outbound.PdfEofCertificateAdapter pdfAdapter;
    private PayloadForense payloadMock;
    private String jsonPayloadMock;

    @BeforeEach
    void setUp() throws Exception {
        injector = new ImageMetadataInjectorAdapter();
        pdfAdapter = new org.example.analisis.infrastructure.adapters.outbound.PdfEofCertificateAdapter();

        // TODO: En producción, los datos del Autor (nombre, id) deben ser extraídos del certificado .p12
        // del artista durante la firma, en lugar de ser mockeados aquí.
        PayloadForense.Autor autor = new PayloadForense.Autor("Edlith Vinueza", "UCE-77765");
        
        PayloadForense.Obra obra = new PayloadForense.Obra("Reflejos de la Memoria", "2026-05-10T15:30:00Z", "FireAlpaca v2.11", "Huion Inspiroy H610PRO v2", "Uso de capas");
        PayloadForense.AnalisisForenseDigital analisis = new PayloadForense.AnalisisForenseDigital("sha256-hash-real", "phash-real", "4000x3000 px");
        PayloadForense.DatosCertificado cert = new PayloadForense.DatosCertificado("VA-123", "VerisArt", "ALCOTEL", "2026", "VALIDO", "RSA", "hash");
        
        // TODO: En producción, el valor de la firma y el algoritmo deben obtenerse matemáticamente 
        // firmando el hash SHA-256 usando la clave privada contenida en el .p12.
        PayloadForense.FirmaDigital firma = new PayloadForense.FirmaDigital("SHA256withRSA", "FIRMA_SIMULADA_BASE64_999");

        payloadMock = new PayloadForense("1.1", autor, obra, analisis, cert, firma);
        jsonPayloadMock = new ObjectMapper().writeValueAsString(payloadMock);
    }

    @Test
    public void debeInyectarMetadatosEnJpg() throws Exception {
        System.out.println("=== Inyectando Metadatos XMP en JPEG ===");
        File file = new File(JPG_PATH);
        if (!file.exists()) {
            System.out.println("Archivo JPG de prueba no encontrado, saltando test.");
            return;
        }

        byte[] originalBytes = Files.readAllBytes(Paths.get(JPG_PATH));
        byte[] modificadoBytes = injector.injectMetadata(originalBytes, "jpeg", jsonPayloadMock);

        assertTrue(modificadoBytes.length > originalBytes.length, "El archivo modificado debe pesar más debido al JSON");

        byte[] pdfBytes = pdfAdapter.generarCertificadoConEof(payloadMock);
        byte[] archivoFinal = new byte[modificadoBytes.length + pdfBytes.length];
        System.arraycopy(modificadoBytes, 0, archivoFinal, 0, modificadoBytes.length);
        System.arraycopy(pdfBytes, 0, archivoFinal, modificadoBytes.length, pdfBytes.length);

        // Guardamos el resultado para validación externa (ej. ver con ExifTool)
        File dirJpg = new File("build/result_test");
        if (!dirJpg.exists()) dirJpg.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(new File(dirJpg, "girasol-certificado.jpg"))) {
            fos.write(archivoFinal);
        }

        // Verificamos superficialmente que el XML/JSON esté dentro de los bytes
        String content = new String(modificadoBytes, StandardCharsets.UTF_8);
        assertTrue(content.contains("FIRMA_SIMULADA_BASE64_999"), "La firma debe estar en los bytes del JPEG");
        System.out.println("Éxito: JPEG inyectado correctamente en XMP (APP1).");
    }

    @Test
    public void debeInyectarMetadatosEnPng() throws Exception {
        System.out.println("=== Inyectando Chunk tEXt en PNG ===");
        File file = new File(PNG_PATH);
        if (!file.exists()) {
            System.out.println("Archivo PNG de prueba no encontrado, saltando test.");
            return;
        }

        byte[] originalBytes = Files.readAllBytes(Paths.get(PNG_PATH));
        byte[] modificadoBytes = injector.injectMetadata(originalBytes, "png", jsonPayloadMock);

        assertTrue(modificadoBytes.length > originalBytes.length, "El archivo modificado debe pesar más debido al JSON");

        byte[] pdfBytes = pdfAdapter.generarCertificadoConEof(payloadMock);
        byte[] archivoFinal = new byte[modificadoBytes.length + pdfBytes.length];
        System.arraycopy(modificadoBytes, 0, archivoFinal, 0, modificadoBytes.length);
        System.arraycopy(pdfBytes, 0, archivoFinal, modificadoBytes.length, pdfBytes.length);

        File dirPng = new File("build/result_test");
        if (!dirPng.exists()) dirPng.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(new File(dirPng, "girasol-certificado.png"))) {
            fos.write(archivoFinal);
        }

        String content = new String(modificadoBytes, StandardCharsets.ISO_8859_1);
        assertTrue(content.contains("tEXtVerisArt"), "Debe existir el chunk tEXt de VerisArt");
        assertTrue(content.contains("Reflejos de la Memoria"), "El título debe estar en los bytes del PNG");
        System.out.println("Éxito: PNG inyectado correctamente con Chunk tEXt.");
    }
}
