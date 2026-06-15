package org.example.analisis.fase4;

import org.example.analisis.core.model.forense.PayloadForense;
import org.example.analisis.core.service.CalculadorPHash;
import org.example.analisis.infrastructure.adapters.outbound.AdaptadorCriptograficoBouncyCastle;
import org.example.analisis.infrastructure.adapters.outbound.AdaptadorPdfIText;
import org.example.analisis.infrastructure.adapters.outbound.AdaptadorEsteganografia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * FASE 4: INSERCIÓN FINAL (HARD BINDING)
 * 
 * Esta clase se encarga de probar el último paso del proceso forense: tomar el
 * certificado PDF (generado en el Paso 5) y adjuntarlo físicamente al final de 
 * los bytes de la imagen original (técnica EOF - End Of File).
 * Esto asegura que la imagen y su certificado viajen juntos de manera inseparable
 * (Hard Binding), permitiendo que cualquier perito pueda extraer el PDF y validar
 * las firmas digitales integradas.
 */
public class Paso6_InsercionFinalTest {

    private static final String BASE_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/";
    private static final String P12_PATH_AUTOR = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/pruebas-firmas-digitales/firma_edlith_test.p12";
    private static final String P12_PATH_CA = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/pruebas-firmas-digitales/firma_final_azure_test.p12";

    private AdaptadorEsteganografia adaptadorEstego;
    private AdaptadorPdfIText adaptadorPdf;
    private AdaptadorCriptograficoBouncyCastle adaptadorCripto;
    private PayloadForense payloadMock;
    private byte[] imagenOriginal;

    @BeforeEach
    void setUp() throws Exception {
        adaptadorEstego = new AdaptadorEsteganografia();
        adaptadorPdf = new AdaptadorPdfIText();
        adaptadorCripto = new AdaptadorCriptograficoBouncyCastle();
        
        // 1. Cargar imagen original
        Path imagePath = Paths.get(BASE_PATH + "imagenes/girasol-original.png");
        imagenOriginal = Files.readAllBytes(imagePath);

        // 2. Extraer datos reales de la imagen (Hash SHA-256 y pHash)
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] sha256Bytes = digest.digest(imagenOriginal);
        StringBuilder shaHex = new StringBuilder();
        for (byte b : sha256Bytes) {
            shaHex.append(String.format("%02x", b));
        }
        String hashSha256 = shaHex.toString();
        
        CalculadorPHash calcPHash = new CalculadorPHash();
        String phashValue = calcPHash.generarHash(ImageIO.read(new ByteArrayInputStream(imagenOriginal)));

        // 3. Extraer llaves reales del P12 generado en el Módulo A
        AdaptadorCriptograficoBouncyCastle.ValidatedKeyPair keysAutor = adaptadorCripto.extraerYValidarP12(P12_PATH_AUTOR, "Tesis2026!".toCharArray());
        
        // 4. Firmar datos reales (Usaremos el id + el hash para la firma)
        String idForense = UUID.randomUUID().toString();
        String firmaAutorBase64 = adaptadorCripto.firmarDatos(idForense + hashSha256, keysAutor.privateKey);

        // TODO: En producción, los datos del Autor (nombre, id) deben ser extraídos del certificado .p12
        // del artista durante la firma, en lugar de ser mockeados aquí.
        PayloadForense.Autor autor = new PayloadForense.Autor(keysAutor.subject + " - C.C. 1712345678", "CyberArtist");
        
        PayloadForense.Obra obra = new PayloadForense.Obra("Titulo de Prueba", "2026-06-14T12:00:00Z", "FireAlpaca", "Huion", "Detalles");
        PayloadForense.AnalisisForenseDigital analisis = new PayloadForense.AnalisisForenseDigital(hashSha256, phashValue, "800x600");
        PayloadForense.DatosCertificado datosCert = new PayloadForense.DatosCertificado(idForense, "CA", "Auth", "2026-06-14", "VALIDO", "RSA", "HashRaiz");
        
        // TODO: En producción, el valor de la firma y el algoritmo deben obtenerse matemáticamente 
        // firmando el hash SHA-256 usando la clave privada contenida en el .p12.
        PayloadForense.FirmaDigital firma = new PayloadForense.FirmaDigital("SHA256withRSA", firmaAutorBase64);

        payloadMock = new PayloadForense("1.1", autor, obra, analisis, datosCert, firma);
    }

    @Test
    void debeInsertarCertificadoEnImagen() {
        System.out.println("=== FASE 4 - PASO 6: Inserción Final del Certificado (EOF) ===");
        
        byte[] imagenConStego = adaptadorEstego.procesar(imagenOriginal, payloadMock.toString());
        byte[] pdfBytes = adaptadorPdf.generarCertificado(payloadMock);
        
        byte[] archivoHibrido = adaptadorEstego.fusionarEOF(imagenConStego, pdfBytes);

        assertNotNull(archivoHibrido);
        
        try {
            File outputFile = new File("girasol-certificado-final.png");
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(archivoHibrido);
            }
            System.out.println("Éxito: Archivo final guardado en " + outputFile.getAbsolutePath() + " para validación visual.");
        } catch (Exception e) {
            System.err.println("Error al guardar archivo: " + e.getMessage());
        }

        byte[] pdfRecuperado = adaptadorEstego.extraerPdfDeEOF(archivoHibrido);
        assertEquals(pdfBytes.length, pdfRecuperado.length, "El PDF se debe poder extraer intacto");
    }
}

