package org.example.analisis.core.model.forense;

import java.util.Date;

/**
 * Entidad de dominio que representa el Certificado final emitido tras la validación de la obra.
 */
public class Certificado {
    private String id;
    private String artistKeyId; // Referencia a la llave del artista
    private String systemKeyId; // Referencia a la llave de la CA
    private String perceptualHash;
    private String cryptographicHash;
    private String artistSignature;
    private String systemSignature;
    private Date certifiedAt;
    
    // Archivos resultantes
    private byte[] payloadJsonBytes;
    private byte[] finalImageBytes; // Imagen con esteganografía
    private byte[] forensicPdfBytes; // Archivo PDF generado
    private byte[] hybridFileBytes; // Archivo final a entregar (Imagen + PDF EOF)

    public Certificado(String id) {
        this.id = id;
    }

    // Getters y Setters básicos para el dominio
    public String getId() { return id; }
    public String getArtistKeyId() { return artistKeyId; }
    public void setArtistKeyId(String artistKeyId) { this.artistKeyId = artistKeyId; }
    public String getSystemKeyId() { return systemKeyId; }
    public void setSystemKeyId(String systemKeyId) { this.systemKeyId = systemKeyId; }
    public String getPerceptualHash() { return perceptualHash; }
    public void setPerceptualHash(String perceptualHash) { this.perceptualHash = perceptualHash; }
    public String getCryptographicHash() { return cryptographicHash; }
    public void setCryptographicHash(String cryptographicHash) { this.cryptographicHash = cryptographicHash; }
    public String getArtistSignature() { return artistSignature; }
    public void setArtistSignature(String artistSignature) { this.artistSignature = artistSignature; }
    public String getSystemSignature() { return systemSignature; }
    public void setSystemSignature(String systemSignature) { this.systemSignature = systemSignature; }
    public Date getCertifiedAt() { return certifiedAt; }
    public void setCertifiedAt(Date certifiedAt) { this.certifiedAt = certifiedAt; }

    public byte[] getPayloadJsonBytes() { return payloadJsonBytes; }
    public void setPayloadJsonBytes(byte[] payloadJsonBytes) { this.payloadJsonBytes = payloadJsonBytes; }
    public byte[] getFinalImageBytes() { return finalImageBytes; }
    public void setFinalImageBytes(byte[] finalImageBytes) { this.finalImageBytes = finalImageBytes; }
    public byte[] getForensicPdfBytes() { return forensicPdfBytes; }
    public void setForensicPdfBytes(byte[] forensicPdfBytes) { this.forensicPdfBytes = forensicPdfBytes; }
    public byte[] getHybridFileBytes() { return hybridFileBytes; }
    public void setHybridFileBytes(byte[] hybridFileBytes) { this.hybridFileBytes = hybridFileBytes; }
}
