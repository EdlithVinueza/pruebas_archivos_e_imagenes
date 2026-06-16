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
        String certId = "VA-2026-UCE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        PayloadForense.Autor autor = new PayloadForense.Autor("Edlith Vinueza", "UCE-77765");
        PayloadForense.Obra obra = new PayloadForense.Obra(
                "Nombre de la Ilustracion",
                "2026-06-14T12:59:43Z",
                "FireAlpaca",
                "Huion Inspiroy H610PRO v2",
                "Uso de capas de opacidad múltiple"
        );
        PayloadForense.AnalisisForenseDigital analisis = new PayloadForense.AnalisisForenseDigital(
                sha256,
                pHash,
                "Desconocido" // Se puede extraer luego de los metadatos reales
        );
        PayloadForense.DatosCertificado datosCert = new PayloadForense.DatosCertificado(
                certId,
                "VerisArt - Laboratorio de Certificación Pericial",
                "Dirección de Control de Telecomunicaciones (ALCOTEL / SENADI)",
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date(timestamp)),
                "VALIDO",
                "RSA-2048",
                "8f4a39b2-simulacion-raiz"
        );
        PayloadForense.FirmaDigital firma = new PayloadForense.FirmaDigital(
                "SHA256withRSA",
                firmaArtista
        );

        PayloadForense payload = new PayloadForense(
                "1.1",
                autor,
                obra,
                analisis,
                datosCert,
                firma
        );
        
        String jsonPayload;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            jsonPayload = mapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando PayloadForense a JSON", e);
        }

        // Aquí deberíamos llamar a ImageMetadataInjectorAdapter y PdfEofCertificateAdapter
        // Por compatibilidad temporal con el puerto existente, simulamos el render
        byte[] renderEsteganografico = renderOriginal; // Ya no alteramos píxeles
        if (esteganografiaPort != null) {
            // Si el puerto sigue existiendo, lo llamamos pero idealmente esto se inyecta con XMP/tEXt
            // renderEsteganografico = esteganografiaPort.procesar(renderOriginal, jsonPayload);
        }

        // Generar Certificado PDF (Se usará el PdfEofCertificateAdapter en la inyección de dependencias)
        byte[] pdfBytes = generadorPdfPort.generarCertificado(payload);

        // Fusión Física (EOF)
        byte[] archivoHibrido = new byte[renderEsteganografico.length + pdfBytes.length];
        System.arraycopy(renderEsteganografico, 0, archivoHibrido, 0, renderEsteganografico.length);
        System.arraycopy(pdfBytes, 0, archivoHibrido, renderEsteganografico.length, pdfBytes.length);

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
