package org.example.analisis.fase1;

import org.example.analisis.core.model.base.ArchivoBase;
import org.example.analisis.core.model.imagen.ArchivoImagen;
import org.example.analisis.core.model.psd.ArchivoPSD;
import org.example.analisis.core.model.validacion.VeredictoFinal;
import org.example.analisis.core.ports.out.ArchivoProcessorPort;
import org.example.analisis.core.service.ArchivoProcessorFactory;
import org.example.analisis.core.service.ValidadorGenericoService;
import org.example.analisis.core.service.CalculadorPHash;
import org.example.analisis.infrastructure.adapters.processors.ArchivoImagenProcessor;
import org.example.analisis.infrastructure.adapters.processors.ArchivoPSDProcessor;
import org.example.analisis.infrastructure.adapters.processors.ImageLoader;
import org.example.analisis.core.rules.imagen.*;
import org.example.analisis.core.rules.psd.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Paso1y2_ForenseIntegracionTest {

    private static final String BASE_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/";
    private ArchivoProcessorFactory factory;
    private ValidadorGenericoService<ArchivoImagen> validadorImagen;
    private ValidadorGenericoService<ArchivoPSD> validadorPSD;
    private CalculadorPHash calculadorPHash;

    @BeforeEach
    public void setUp() {
        // 1. Inicializar la fábrica de procesadores
        List<ArchivoProcessorPort<? extends ArchivoBase>> procesadores = Arrays.asList(
                new ArchivoImagenProcessor(),
                new ArchivoPSDProcessor());
        factory = new ArchivoProcessorFactory(procesadores);

        // 2. Inicializar los validadores forenses
        validadorImagen = new ValidadorGenericoService<>();
        validadorImagen.registrarRegla(new ReglaFirmaEstructural());
        validadorImagen.registrarRegla(new ReglaCoherenciaDpi());
        validadorImagen.registrarRegla(new ReglaAnalisisOrigen());

        validadorPSD = new ValidadorGenericoService<>();
        validadorPSD.registrarRegla(new ReglaFormatoPsd());
        validadorPSD.registrarRegla(new ReglaResolucionProfesional());
        validadorPSD.registrarRegla(new ReglaImagenPegada());
        validadorPSD.registrarRegla(new ReglaComplejidadDiseno());

        // 3. Inicializar el calculador pHash
        calculadorPHash = new CalculadorPHash();
    }

    @Test
    public void testFlujoForenseCompleto() {

        File archivoPSD = new File(BASE_PATH + "psd/girasoles-original.psd");
        File archivoPNG = new File(BASE_PATH + "imagenes/girasol-original.png");

        // Verificar existencia de archivos locales antes de correr
        Assumptions.assumeTrue(archivoPSD.exists() && archivoPNG.exists(),
                "Faltan archivos de prueba en: " + BASE_PATH);

        System.out.println("\nANALIZANDO ARCHIVOS FORENSES...");
        System.out.println(
                "• Archivo PSD: " + archivoPSD.getName() + " (" + (archivoPSD.length() / (1024 * 1024)) + " MB)");
        System.out.println(
                "• Archivo PNG: " + archivoPNG.getName() + " (" + (archivoPNG.length() / (1024 * 1024)) + " MB)");

        long globalStart = System.currentTimeMillis();

        // 1. Procesar y validar PSD
        System.out.println("\n--- [A. PROCESANDO PSD] ---");
        long startPSD = System.currentTimeMillis();

        ArchivoProcessorPort<ArchivoPSD> processorPSD = (ArchivoProcessorPort<ArchivoPSD>) factory
                .getProcessor(archivoPSD);
        ArchivoPSD psd = processorPSD.procesar(archivoPSD);
        assertNotNull(psd, "El procesamiento de PSD falló");

        VeredictoFinal veredictoPSD = validadorPSD.validar(psd);
        assertNotNull(veredictoPSD);
        assertFalse(veredictoPSD.isEsRechazado(),
                "El PSD original no debería ser rechazado por las reglas críticas: " + veredictoPSD.getRazonRechazo());

        System.out.println(psd.getMetadatos());
        System.out.println("• Capas binarias extraídas: " + psd.getCapas().size());

        long endPSD = System.currentTimeMillis();
        System.out.println(" Tiempo PSD: " + (endPSD - startPSD) + " ms");

        // 2. Procesar y validar PNG
        System.out.println("\n--- [B. PROCESANDO PNG] ---");
        long startPNG = System.currentTimeMillis();

        ArchivoProcessorPort<ArchivoImagen> processorPNG = (ArchivoProcessorPort<ArchivoImagen>) factory
                .getProcessor(archivoPNG);
        ArchivoImagen png = processorPNG.procesar(archivoPNG);
        assertNotNull(png, "El procesamiento de PNG falló");

        VeredictoFinal veredictoPNG = validadorImagen.validar(png);
        assertNotNull(veredictoPNG);
        assertFalse(veredictoPNG.isEsRechazado(),
                "La imagen PNG original no debería ser rechazada por las reglas críticas: "
                        + veredictoPNG.getRazonRechazo());

        System.out.println(png.getMetadatos());
        System.out.println(png.getEstructura());

        long endPNG = System.currentTimeMillis();
        System.out.println("Tiempo PNG: " + (endPNG - startPNG) + " ms");

        // 3. Similitud Perceptual Forense
        System.out.println("\n--- [C. COMPARACIÓN DE SIMILITUD PERCEPTUAL FORENSE (p-Hash)] ---");
        long startSim = System.currentTimeMillis();
        System.out.println("Extrayendo e indexando firmas visuales de manera optimizada en memoria...");

        // Carga submuestreada de alto rendimiento
        BufferedImage imgPSD = ImageLoader.loadWithSubsampling(archivoPSD);
        BufferedImage imgPNG = ImageLoader.loadWithSubsampling(archivoPNG);
        assertNotNull(imgPSD, "La carga del composite del PSD falló");
        assertNotNull(imgPNG, "La carga del PNG falló");

        // Generar hash perceptual (pHash)
        String hashPSD = calculadorPHash.generarHash(imgPSD);
        String hashPNG = calculadorPHash.generarHash(imgPNG);
        assertNotNull(hashPSD);
        assertNotNull(hashPNG);

        // Comparar similitud de hashes por distancia Hamming
        double porcentajeSimilitud = calculadorPHash.compararSimilitud(hashPSD, hashPNG);
        long endSim = System.currentTimeMillis();

        System.out.println("\nREPORTE DE SIMILITUD FORENSE:");
        System.out.println("Hash Perceptual PSD composite: " + hashPSD);
        System.out.println("Hash Perceptual PNG subida   : " + hashPNG);
        System.out.printf("Porcentaje de Coincidencia  : %.2f%%%n", porcentajeSimilitud);
        System.out.println("Tiempo Extracción/Comparación: " + (endSim - startSim) + " ms");

        // Aserción de similitud de autenticidad (debería ser alta para el original)
        assertTrue(porcentajeSimilitud >= 95.0,
                "La similitud visual de los archivos originales debería ser excelente (>= 95%)");

        long globalEnd = System.currentTimeMillis();
        System.out.println("\n=========================================================");
        System.out.printf(" PROCESAMIENTO FORENSE INTEGRAL COMPLETADO EN: %d ms%n", (globalEnd - globalStart));
        System.out.println("=========================================================");
    }
}
