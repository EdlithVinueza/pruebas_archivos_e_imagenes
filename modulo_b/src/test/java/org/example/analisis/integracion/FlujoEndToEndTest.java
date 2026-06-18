package org.example.analisis.integracion;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.analisis.core.model.forense.PayloadForense;
import org.example.analisis.core.pdf.GeneradorCertificadoPdf;
import org.example.analisis.core.service.CalculadorPHash;
import org.example.analisis.infrastructure.adapters.outbound.AdaptadorCriptograficoBouncyCastle;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de Integración End-to-End (De principio a fin)
 * Simula el flujo completo de un usuario certificando su obra.
 */
public class FlujoEndToEndTest {

    private static final String BASE_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/";
    private static final String P12_PATH_AUTOR = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/pruebas-firmas-digitales/firma_girasol.p12";
    private byte[] imagenOriginalPngBytes;
    private byte[] imagenOriginalJpgBytes;
    private AdaptadorCriptograficoBouncyCastle adaptadorCripto;
    private AdaptadorEsteganografia adaptadorEstego;

    @BeforeEach
    void setUp() throws Exception {
        adaptadorCripto = new AdaptadorCriptograficoBouncyCastle();
        adaptadorEstego = new AdaptadorEsteganografia();

        // 1. CARGA DE IMÁGENES ORIGINALES
        Path imagePathPng = Paths.get(BASE_PATH + "imagenes/girasol-original.png");
        imagenOriginalPngBytes = Files.readAllBytes(imagePathPng);

        Path imagePathJpg = Paths.get(BASE_PATH + "imagenes/girasol-original.jpg");
        imagenOriginalJpgBytes = Files.readAllBytes(imagePathJpg);
    }

    @Test
    void testFlujoCompletoDeCertificacion_PNG() throws Exception {
        System.out.println("\n*********************************************************");
        System.out.println(" INICIANDO TEST PARA FORMATO PNG ");
        System.out.println("*********************************************************");
        ejecutarFlujo(imagenOriginalPngBytes, "png", "Girasol_Certificado_E2E.png");
    }

    @Test
    void testFlujoCompletoDeCertificacion_JPEG() throws Exception {
        System.out.println("\n*********************************************************");
        System.out.println(" INICIANDO TEST PARA FORMATO JPEG ");
        System.out.println("*********************************************************");
        ejecutarFlujo(imagenOriginalJpgBytes, "jpeg", "Girasol_Certificado_E2E.jpg");
    }

    private void ejecutarFlujo(byte[] imagenOriginalBytes, String formato, String nombreArchivoSalida)
            throws Exception {
        String imagenBase64 = Base64.getEncoder().encodeToString(imagenOriginalBytes);

        // --- FASE 1: HASHES ---
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] sha256Bytes = digest.digest(imagenOriginalBytes);
        StringBuilder shaHex = new StringBuilder();
        for (byte b : sha256Bytes) {
            shaHex.append(String.format("%02x", b));
        }
        String hashSha256 = shaHex.toString();

        CalculadorPHash calcPHash = new CalculadorPHash();
        String phashValue = calcPHash.generarHash(ImageIO.read(new ByteArrayInputStream(imagenOriginalBytes)));

        // --- FASE 2: EXTRACCIÓN DE IDENTIDAD DEL P12 ---
        AdaptadorCriptograficoBouncyCastle.ValidatedKeyPair keysAutor = adaptadorCripto
                .extraerYValidarP12(P12_PATH_AUTOR, "Tesis2026!".toCharArray());

        System.out.println("=========================================================");
        System.out.println("DATOS EXTRAÍDOS DE LA FIRMA DIGITAL (CÉDULA) [" + formato.toUpperCase() + "]:");
        System.out.println("  -> CN (Nombre Legal)    : " + keysAutor.subject);
        System.out.println("  -> SERIALNUMBER (Cédula): " + keysAutor.serialNumber);
        System.out.println("  -> OU (Apodo/Org Unit)  : " + keysAutor.ou);
        System.out.println("\nDATOS INGRESADOS POR EL USUARIO (OBRA):");
        System.out.println("  -> Título   : Chica de los Girasoles (" + formato.toUpperCase() + ")");
        System.out.println("  -> Software : FireAlpaca");
        System.out.println("  -> Hardware : Huion Inspiroy H610PRO v2");
        System.out.println("\nHUELLAS DIGITALES CALCULADAS:");
        System.out.println("  -> SHA-256  : " + hashSha256);
        System.out.println("  -> pHash    : " + phashValue);
        System.out.println("=========================================================\n");

        PayloadForense payload = new PayloadForense();
        payload.setMetadataVersion("1.1");

        String idCertificado = "CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String fechaEmision = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC";

        PayloadForense.DatosCertificado datosCert = new PayloadForense.DatosCertificado(
                idCertificado, "UNIVERSIDAD CENTRAL DEL ECUADOR", "UNIDAD DE INVESTIGACIÓN",
                fechaEmision, "VALIDO_EMITIDO", "RSA_2048", "Hash_Raiz_Mock");
        payload.setDatosDelCertificado(datosCert);

        String phashHex = new java.math.BigInteger(phashValue, 2).toString(16);

        String seudonimoFinal = keysAutor.ou.equals("No Especificado") ? null : keysAutor.ou;
        PayloadForense.Autor autor = new PayloadForense.Autor(keysAutor.subject, seudonimoFinal,
                keysAutor.serialNumber);
        payload.setAutor(autor);

        PayloadForense.Obra obra = new PayloadForense.Obra(
                "Chica de los Girasoles", "2026-06-15 14:30:00 UTC",
                "FireAlpaca", "Huion Inspiroy H610PRO v2",
                "Formato original, certificado en " + formato.toUpperCase() + ".");
        payload.setObra(obra);

        PayloadForense.AnalisisForenseDigital analisis = new PayloadForense.AnalisisForenseDigital(
                hashSha256, phashHex, "Resolución Original");
        payload.setAnalisisForenseDigital(analisis);

        // --- FASE 3: FIRMA DIGITAL Y VALIDACIÓN ---
        String firmaBase64 = adaptadorCripto.firmarDatos(idCertificado + hashSha256, keysAutor.privateKey);
        payload.setFirmaDigital(new PayloadForense.FirmaDigital("SHA256withECDSA", firmaBase64));
        System.out.println(" Firma Generada exitosamente (" + formato.toUpperCase() + ").");

        java.security.Signature verifier = java.security.Signature.getInstance("SHA256withECDSA", "BC");
        verifier.initVerify(keysAutor.publicKey);
        verifier.update((idCertificado + hashSha256).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        boolean firmaValida = verifier.verify(java.util.Base64.getDecoder().decode(firmaBase64));

        if (firmaValida) {
            System.out.println(" Firma validada matemáticamente contra la llave pública del P12.");
        } else {
            throw new RuntimeException("Error: La firma no es válida.");
        }

        System.out.println("\nJSON PAYLOAD FORENSE GENERADO:");
        String jsonForense = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(payload);
        System.out.println(jsonForense);

        // --- FASE 4: EMISIÓN DEL CERTIFICADO PDF FÍSICO ---
        GeneradorCertificadoPdf generadorPdf = new GeneradorCertificadoPdf();
        File dirOut = new File("build/resultados_test");
        if (!dirOut.exists())
            dirOut.mkdirs();

        String rutaPdf = "build/resultados_test/Certificado_Final_E2E_" + formato.toUpperCase() + ".pdf";
        generadorPdf.generarCertificado(payload, rutaPdf, imagenBase64);

        File archivoPdf = new File(rutaPdf);
        assertTrue(archivoPdf.exists(), "El PDF debe haberse generado");

        // --- FASE 5: INYECCIÓN ESTEGANOGRÁFICA (EOF) EN LA IMAGEN ---
        byte[] pdfBytes = Files.readAllBytes(archivoPdf.toPath());

        String jsonMinificado = new ObjectMapper().writeValueAsString(payload);
        byte[] imagenConStegoString = adaptadorEstego.procesar(imagenOriginalBytes, jsonMinificado);
        byte[] archivoHibridoFinal = adaptadorEstego.fusionarEOF(imagenConStegoString, pdfBytes);

        File dirFormato = new File("build/resultados_test/" + formato);
        if (!dirFormato.exists())
            dirFormato.mkdirs();

        String rutaImagenHibrida = "build/resultados_test/" + formato + "/" + nombreArchivoSalida;
        try (FileOutputStream fos = new FileOutputStream(rutaImagenHibrida)) {
            fos.write(archivoHibridoFinal);
        }

        System.out
                .println("\n pdf y imagen estenografiado acabado revisa la carpeta de buil resutados-test/" + formato);
    }
}
