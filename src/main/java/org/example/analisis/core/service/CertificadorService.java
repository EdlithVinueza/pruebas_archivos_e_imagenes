package org.example.analisis.core.service;

import org.example.analisis.core.model.forense.Certificado;
import org.example.analisis.core.model.forense.PayloadForense;
import org.example.analisis.core.ports.inbound.CertificarObraUseCase;
import org.example.analisis.core.ports.outbound.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

public class CertificadorService implements CertificarObraUseCase {

    private final VerificadorCertificadosPort verificadorCertificados;
    private final ServicioCriptograficoCAPort servicioCriptoCA;
    private final SelloTiempoPort selloTiempoPort;
    private final GeneradorPdfPort generadorPdfPort;
    private final ServicioEsteganograficoPort esteganografiaPort;

    public CertificadorService(VerificadorCertificadosPort verificadorCertificados,
                               ServicioCriptograficoCAPort servicioCriptoCA,
                               SelloTiempoPort selloTiempoPort,
                               GeneradorPdfPort generadorPdfPort,
                               ServicioEsteganograficoPort esteganografiaPort) {
        this.verificadorCertificados = verificadorCertificados;
        this.servicioCriptoCA = servicioCriptoCA;
        this.selloTiempoPort = selloTiempoPort;
        this.generadorPdfPort = generadorPdfPort;
        this.esteganografiaPort = esteganografiaPort;
    }

    @Override
    public Certificado certificar(byte[] renderOriginal, String pHash, byte[] p12Bytes, char[] password) {
        // En una implementación completa del adaptador criptográfico, 
        // aquí usaríamos un adaptador intermedio para extraer las llaves en RAM.
        // Como estamos en el dominio, abstraeremos esa parte simulando que el Verificador nos aprueba el P12.
        
        // FASE 2: Validar Vigencia (simulamos que el adaptador valida el contenido del P12)
        boolean esValido = verificadorCertificados.validarVigencia(null); // Pasaremos la llave extraída
        if (!esValido) {
            throw new SecurityException("El certificado P12 es inválido, caducado o no emitido por la CA.");
        }

        // FASE 3: Hard Binding y Doble Firma
        String sha256 = calcularSHA256(renderOriginal);
        
        // Simulación: El adaptador de clave privada del artista firma el pHash y sha256
        String firmaArtista = "MEYCIQ_Simulada_Artista_" + System.currentTimeMillis(); 
        
        // Doble firma de la CA
        String paqueteAFirmar = pHash + "|" + sha256 + "|" + firmaArtista;
        String firmaCA = servicioCriptoCA.firmarPaquete(paqueteAFirmar);
        
        // Sello de tiempo
        long timestamp = selloTiempoPort.obtenerTimestamp(sha256);

        // FASE 4: Generación de Entregables Híbridos
        String certId = UUID.randomUUID().toString();
        
        PayloadForense payload = new PayloadForense(
                certId,
                "Artista Verificado", // Se extrae del P12
                "Alias",              // Se extrae del sistema
                pHash,
                sha256,
                firmaArtista,
                firmaCA,
                timestamp
        );
        
        String jsonPayload = payload.toString(); // En producción usar Jackson/Gson

        // Inyectar en Luminancia DCT (Esteganografía)
        byte[] renderEsteganografico = esteganografiaPort.procesar(renderOriginal, jsonPayload);

        // Generar Certificado PDF
        byte[] pdfBytes = generadorPdfPort.generarCertificado(payload);

        // Fusión Física (EOF)
        byte[] archivoHibrido = esteganografiaPort.fusionarEOF(renderEsteganografico, pdfBytes);

        // Construir Entidad Final
        Certificado certificado = new Certificado(certId);
        certificado.setPerceptualHash(pHash);
        certificado.setCryptographicHash(sha256);
        certificado.setArtistSignature(firmaArtista);
        certificado.setSystemSignature(firmaCA);
        certificado.setCertifiedAt(new Date(timestamp));
        certificado.setPayloadJsonBytes(jsonPayload.getBytes());
        certificado.setFinalImageBytes(renderEsteganografico);
        certificado.setForensicPdfBytes(pdfBytes);
        certificado.setHybridFileBytes(archivoHibrido);

        return certificado;
    }

    private String calcularSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(data);
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se encontró el algoritmo SHA-256", e);
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
