package org.example.analisis;

import org.example.analisis.core.service.CalculadorPHash;
import org.example.analisis.infrastructure.adapters.processors.ImageLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.awt.image.BufferedImage;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class PHashTest {

    private static final String BASE_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/";
    private CalculadorPHash calculadorPHash;

    @BeforeEach
    public void setUp() {
        calculadorPHash = new CalculadorPHash();
    }

    @Test
    public void testGenerarHashPerceptual() {
        File filePNG = new File(BASE_PATH + "imagenes/girasol-original.png");
        Assumptions.assumeTrue(filePNG.exists(),
                "El archivo de prueba de imagen no existe: " + filePNG.getAbsolutePath());

        BufferedImage img = ImageLoader.loadWithSubsampling(filePNG);
        assertNotNull(img, "La imagen no debería cargarse como nula");

        String hash = calculadorPHash.generarHash(img);
        assertNotNull(hash, "El hash perceptual generado no debe ser nulo");
        assertFalse(hash.isEmpty(), "El hash perceptual generado no debe estar vacío");
    }

    @Test
    public void testSimilitudEntreCompositePHash() {
        File filePSD = new File(BASE_PATH + "psd/girasol-original-una-capa.psd");
        File filePNG = new File(BASE_PATH + "imagenes/girasol-original.png");
        Assumptions.assumeTrue(filePSD.exists() && filePNG.exists(), "Faltan archivos para comparar similitud pHash");

        BufferedImage imgPSD = ImageLoader.loadWithSubsampling(filePSD);
        BufferedImage imgPNG = ImageLoader.loadWithSubsampling(filePNG);
        assertNotNull(imgPSD, "La imagen composite del PSD no debe ser nula");
        assertNotNull(imgPNG, "La imagen PNG no debe ser nula");

        String hashPSD = calculadorPHash.generarHash(imgPSD);
        String hashPNG = calculadorPHash.generarHash(imgPNG);

        double porcentajeSimilitud = calculadorPHash.compararSimilitud(hashPSD, hashPNG);
        assertTrue(porcentajeSimilitud >= 0.0 && porcentajeSimilitud <= 100.0,
                "La similitud debe estar entre 0% y 100%");
        System.out.println("Porcentaje de similitud perceptual obtenido: " + porcentajeSimilitud + "%");
    }
}
