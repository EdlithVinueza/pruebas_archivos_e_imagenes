package org.example.analisis.fase4;

import org.example.analisis.core.model.forense.PayloadForense;
import org.example.analisis.core.service.CalculadorPHash;
import org.example.analisis.infrastructure.adapters.outbound.AdaptadorCriptograficoBouncyCastle;
import org.example.analisis.infrastructure.adapters.outbound.AdaptadorPdfIText;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FASE 4: GENERACIÓN DEL CERTIFICADO PDF
 * 
 * Esta clase se encarga de probar que el sistema puede tomar el Payload Forense
 * (los datos del artista, la obra, los hashes y las firmas criptográficas) y
 * renderizarlos visualmente en un documento PDF de alta calidad.
 * El PDF incluye un Código QR nativo que contiene el Payload JSON, permitiendo
 * que un perito escanee el documento impreso o digital para validar las firmas.
 */
public class Paso5_GeneracionPdfTest {

    private static final String BASE_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/";
    private static final String P12_PATH_AUTOR = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/pruebas-firmas-digitales/firma_edlith_test.p12";
    private static final String P12_PATH_CA = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/pruebas-firmas-digitales/firma_final_azure_test.p12";

    private AdaptadorPdfIText adaptadorPdf;
    private AdaptadorCriptograficoBouncyCastle adaptadorCripto;
    private PayloadForense payloadMock;

    @BeforeEach
    void setUp() throws Exception {
        adaptadorPdf = new AdaptadorPdfIText();
        adaptadorCripto = new AdaptadorCriptograficoBouncyCastle();

        // 1. Cargar imagen original
        Path imagePath = Paths.get(BASE_PATH + "imagenes/girasol-original.png");
        byte[] imagenOriginal = Files.readAllBytes(imagePath);

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
        System.out.println("Extrayendo datos de los certificados P12...");
        AdaptadorCriptograficoBouncyCastle.ValidatedKeyPair keysAutor = 
                adaptadorCripto.extraerYValidarP12(P12_PATH_AUTOR, "Tesis2026!".toCharArray());

        payloadMock = new PayloadForense();
        payloadMock.setMetadataVersion("1.1");
        
        PayloadForense.DatosCertificado datosCertificado = new PayloadForense.DatosCertificado();
        datosCertificado.setIdCertificado("CERT-2026-9941A");
        datosCertificado.setFechaEmision(LocalDateTime.now().toString());
        datosCertificado.setEstadoInicial("EMITIDO_VALIDO");
        datosCertificado.setEntidadEmisora("UNIVERSIDAD CENTRAL DEL ECUADOR");
        datosCertificado.setAutoridadDelegatoria("UNIDAD DE INVESTIGACIÓN");
        payloadMock.setDatosDelCertificado(datosCertificado);
        
        PayloadForense.Autor autor = new PayloadForense.Autor();
        autor.setNombre(keysAutor.subject);
        autor.setSeudonimo("Edith Vinueza"); // Seudónimo de prueba
        autor.setIdInstitucional(keysAutor.serialNumber);
        payloadMock.setAutor(autor);
        
        PayloadForense.Obra obra = new PayloadForense.Obra();
        obra.setTitulo("El Eco del Mañana");
        obra.setSoftwareOriginal("Photoshop");
        obra.setHardwareAdicional("Wacom");
        payloadMock.setObra(obra);
        
        PayloadForense.AnalisisForenseDigital analisis = new PayloadForense.AnalisisForenseDigital();
        analisis.setSha256Criptografico("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        analisis.setPhashPerceptual("8f3c3c4f5a6b7d8e"); // Valor Hexadecimal simulado
        payloadMock.setAnalisisForenseDigital(analisis);

        String firmaBase64 = adaptadorCripto.firmarDatos(datosCertificado.getIdCertificado() + analisis.getSha256Criptografico(), keysAutor.privateKey);
        payloadMock.setFirmaDigital(new PayloadForense.FirmaDigital("SHA256withECDSA", firmaBase64));
    }

    @Test
    void debeGenerarCertificadoPdfDesdeHtml() {
        System.out.println("=== FASE 4 - PASO 5: Generación de Certificado PDF (HTML2PDF) ===");

        byte[] pdfBytes = adaptadorPdf.generarCertificado(payloadMock);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "El PDF debe haberse generado con la plantilla HTML");

        try {
            File dir = new File("build/result_test");
            if (!dir.exists()) dir.mkdirs();
            File outputFile = new File(dir, "certificado-test.pdf");
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(pdfBytes);
            }
            System.out.println("Éxito: PDF guardado en " + outputFile.getAbsolutePath() + " para validación visual.");
        } catch (Exception e) {
            System.err.println("Error al guardar archivo: " + e.getMessage());
        }
    }
}
