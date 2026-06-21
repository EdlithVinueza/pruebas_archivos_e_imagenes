package ec.edu.uce.certificadorforense.core.service;

import ec.edu.uce.certificadorforense.core.model.certificado.Certificado;
import ec.edu.uce.certificadorforense.core.ports.out.GeneradorHashPort;
import ec.edu.uce.certificadorforense.core.ports.out.GeneradorQRPort;

import java.time.Instant;
import java.time.Year;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio de dominio: generación del {@link Certificado}.
 * <p>
 * Genera el ID del certificado, el QR y construye el objeto {@link Certificado}
 * que será usado para producir el PDF en la Fase 4.
 * </p>
 */
public class CertificadoService {

    /** Tamaño del QR en píxeles. */
    private static final int QR_SIZE = 150;

    private static final AtomicInteger CONTADOR = new AtomicInteger(1);

    private final GeneradorQRPort generadorQR;
    private final GeneradorHashPort generadorHash;

    public CertificadoService(GeneradorQRPort generadorQR, GeneradorHashPort generadorHash) {
        this.generadorQR = generadorQR;
        this.generadorHash = generadorHash;
    }

    /**
     * Genera el certificado completo.
     *
     * @param idExpediente          ID del expediente asociado.
     * @param expedienteFirmadoJson JSON del expediente ya firmado.
     * @return {@link Certificado} con ID, QR y hashes.
     */
    public Certificado generar(String idExpediente, String expedienteFirmadoJson) {
        String idCertificado = generarId();

        // Contenido del QR — Opción A: solo el ID interno
        String qrContenido = idCertificado;
        byte[] qrBytes = generadorQR.generar(qrContenido, QR_SIZE, QR_SIZE);
        String qrBase64 = Base64.getEncoder().encodeToString(qrBytes);

        // Hash del expediente firmado
        String hashExpedienteFirmado = generadorHash.calcularSHA512(expedienteFirmadoJson);

        System.out.println("[CertificadoService] Certificado generado: " + idCertificado);

        return Certificado.builder()
                .idCertificado(idCertificado)
                .idExpediente(idExpediente)
                .fechaEmision(Instant.now())
                .hashExpedienteFirmado(hashExpedienteFirmado)
                .qrContenido(qrContenido)
                .qrBase64(qrBase64)
                .build();
    }

    /**
     * Genera un ID único para el certificado.
     * Formato: {@code CERT-YYYY-NNNNNN} (ej. {@code CERT-2026-000001}).
     */
    private String generarId() {
        int anio = Year.now().getValue();
        int numero = CONTADOR.getAndIncrement();
        return String.format("CERT-%d-%06d", anio, numero);
    }
}
