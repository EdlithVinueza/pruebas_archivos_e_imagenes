package org.example.analisis.infrastructure.adapters.outbound;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

public class AdaptadorCriptograficoBouncyCastle {

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Extrae y valida el certificado desde un archivo P12.
     * Lanza excepciones si la contraseña es incorrecta o el certificado expiró.
     */
    public ValidatedKeyPair extraerYValidarP12(String filePath, char[] password) throws Exception {
        KeyStore keystore = KeyStore.getInstance("PKCS12", "BC");

        try (FileInputStream fis = new FileInputStream(filePath)) {
            keystore.load(fis, password);
        }

        // El alias que configuramos en el Módulo A es "firma_autor"
        String alias = "firma_autor";

        if (!keystore.containsAlias(alias)) {
            throw new IllegalArgumentException("El archivo P12 no contiene la llave de autor válida.");
        }

        PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, password);
        X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);

        // 1. Validar Vigencia (Fecha)
        try {
            certificate.checkValidity(new Date()); // Lanza CertificateExpiredException o
                                                   // CertificateNotYetValidException
        } catch (Exception e) {
            throw new SecurityException("El certificado ha caducado o aún no es válido: " + e.getMessage());
        }

        // 2. Extraer datos importantes del certificado (Solo el Common Name)
        String issuerFull = certificate.getIssuerX500Principal().getName();
        String subjectFull = certificate.getSubjectX500Principal().getName();

        String issuerInfo = extractField(issuerFull, "CN=");
        String subjectInfo = extractField(subjectFull, "CN=");
        String serialNumberInfo = extractField(subjectFull, "SERIALNUMBER=");
        String ouInfo = extractField(subjectFull, "OU=");

        return new ValidatedKeyPair(privateKey, certificate.getPublicKey(), issuerInfo, subjectInfo, serialNumberInfo, ouInfo);
    }

    private String extractField(String dn, String fieldPrefix) {
        for (String part : dn.split(",")) {
            if (part.trim().startsWith(fieldPrefix)) {
                return part.trim().substring(fieldPrefix.length());
            }
        }
        return dn; // Retorna todo el DN si no encuentra el campo específico
    }

    // Clase interna para devolver ambos resultados
    public static class ValidatedKeyPair {
        public final PrivateKey privateKey;
        public final PublicKey publicKey;
        public final String issuer;
        public final String subject;
        public final String serialNumber;
        public final String ou;

        public ValidatedKeyPair(PrivateKey privateKey, PublicKey publicKey, String issuer, String subject, String serialNumber, String ou) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
            this.issuer = issuer;
            this.subject = subject;
            this.serialNumber = serialNumber;
            this.ou = ou;
        }
    }

    /**
     * Genera una firma criptográfica real ECDSA (SHA256withECDSA) en Base64.
     */
    public String firmarDatos(String datos, PrivateKey privateKey) {
        try {
            java.security.Signature signature = java.security.Signature.getInstance("SHA256withECDSA", "BC");
            signature.initSign(privateKey);
            signature.update(datos.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] firmaBytes = signature.sign();
            return java.util.Base64.getEncoder().encodeToString(firmaBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error al firmar los datos con ECDSA: " + e.getMessage(), e);
        }
    }
}
