package org.example.analisis.fase2;

import org.example.analisis.infrastructure.adapters.outbound.AdaptadorCriptograficoBouncyCastle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Paso3_ValidacionFirmaTest {

    private AdaptadorCriptograficoBouncyCastle adaptadorCripto;
    private final String p12Path = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/pruebas-firmas-digitales/firma_edlith_test.p12";
    private final char[] password = "Tesis2026!".toCharArray();

    @BeforeEach
    void setUp() {
        adaptadorCripto = new AdaptadorCriptograficoBouncyCastle();
    }

    @Test
    void debeExtraerYValidarElP12Exitosamente() {
        try {
            AdaptadorCriptograficoBouncyCastle.ValidatedKeyPair result = adaptadorCripto.extraerYValidarP12(p12Path, password);
            
            assertNotNull(result);
            assertNotNull(result.privateKey, "La llave privada debe poder extraerse para firmar.");
            assertNotNull(result.publicKey, "La llave pública debe poder extraerse para validar en BD.");
            
            System.out.println("====== RESULTADO DE VALIDACIÓN P12 ======");
            System.out.println("Sujeto (Autor): " + result.subject);
            System.out.println("Identificación (SERIALNUMBER): " + result.serialNumber);
            System.out.println("Pseudónimo (OU): " + result.ou);
            System.out.println("Emisor (CA): " + result.issuer);
            System.out.println("Algoritmo Llave Pública: " + result.publicKey.getAlgorithm());
            System.out.println("=========================================");

            assertTrue(result.issuer.contains("ARCOTEL") || result.issuer.contains("Autoridad de Certificacion"), 
                    "El certificado debe haber sido emitido por nuestra CA autorizada.");
            
        } catch (Exception e) {
            fail("No se esperaba una excepción al validar una firma correcta: " + e.getMessage());
        }
    }

    @Test
    void debeFallarSiLaContrasenaEsIncorrecta() {
        char[] badPassword = "WrongPassword123!".toCharArray();
        
        Exception exception = assertThrows(Exception.class, () -> {
            adaptadorCripto.extraerYValidarP12(p12Path, badPassword);
        });
        
        System.out.println("Excepción esperada capturada: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("keystore password was incorrect") || exception.getMessage().contains("password"));
    }
}

