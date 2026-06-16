package org.example.analisis.fase3;

import org.example.analisis.core.model.forense.PayloadForense;
import org.example.analisis.core.service.CalculadorPHash;
import org.example.analisis.infrastructure.adapters.outbound.AdaptadorCriptograficoBouncyCastle;
import org.example.analisis.infrastructure.adapters.outbound.AdaptadorEsteganografia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FASE 3: FIRMA CRIPTOGRÁFICA
 * 
 * Esta clase se encarga de probar que el sistema puede tomar una imagen cruda, 
 * calcular sus hashes matemáticos reales (SHA-256 y pHash), y luego utilizar 
 * el certificado digital del artista (.p12) para firmar esos hashes.
 * El resultado es un "Payload Forense" que contiene la identidad vinculada
 * a la imagen mediante criptografía de curva elíptica (ECDSA).
 */
public class Paso4_FirmaCriptograficaTest {

    private static final String BASE_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/";
    private static final String P12_PATH_AUTOR = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/pruebas-firmas-digitales/firma_edlith_test.p12";
    private static final String P12_PATH_CA = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/pruebas-firmas-digitales/firma_final_azure_test.p12";

    private AdaptadorEsteganografia adaptadorEstego;
    private AdaptadorCriptograficoBouncyCastle adaptadorCripto;
    private PayloadForense payloadMock;
    private byte[] imagenOriginal; 

    @BeforeEach
    void setUp() throws Exception {
        adaptadorEstego = new AdaptadorEsteganografia();
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

        // Usamos los datos reales extraídos del certificado .p12 del artista
        PayloadForense.Autor autor = new PayloadForense.Autor(
                keysAutor.subject, 
                keysAutor.serialNumber + " (" + keysAutor.ou + ")"
        );
        
        PayloadForense.Obra obra = new PayloadForense.Obra("Titulo de Prueba", "2026-06-14T12:00:00Z", "FireAlpaca", "Huion", "Detalles");
        PayloadForense.AnalisisForenseDigital analisis = new PayloadForense.AnalisisForenseDigital(hashSha256, phashValue, "800x600");
        PayloadForense.DatosCertificado datosCert = new PayloadForense.DatosCertificado(idForense, "CA", "Auth", "2026-06-14", "VALIDO", "RSA", "HashRaiz");
        
        // TODO: En producción, el valor de la firma y el algoritmo deben obtenerse matemáticamente 
        // firmando el hash SHA-256 usando la clave privada contenida en el .p12.
        PayloadForense.FirmaDigital firma = new PayloadForense.FirmaDigital("SHA256withRSA", firmaAutorBase64);

        payloadMock = new PayloadForense("1.1", autor, obra, analisis, datosCert, firma);
    }

    @Test
    public void debeInyectarFirmaCriptograficaEnImagen() throws Exception {
        System.out.println("=== FASE 3 - PASO 4: Inyección de Firma (Esteganografía DCT) ===");

        // Ejecución: Convertimos el objeto Payload a un formato JSON de texto y lo inyectamos
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String payloadJson = mapper.writeValueAsString(payloadMock);
        byte[] imagenConStego = adaptadorEstego.procesar(imagenOriginal, payloadJson);

        // Verificaciones (Asserts)
        assertNotNull(imagenConStego, "La imagen con esteganografía no debe ser nula");
        assertTrue(imagenConStego.length > imagenOriginal.length, "La imagen debe crecer al inyectar la firma");

        // Intentamos verificar que el texto sobrevivió (simulando una extracción básica)
        String content = new String(imagenConStego, StandardCharsets.UTF_8);
        assertTrue(content.contains("Titulo de Prueba"), "La firma debe sobrevivir en los bytes de la imagen");
        System.out.println("Éxito: La firma ha sido incrustada en la imagen.");
    }
}

