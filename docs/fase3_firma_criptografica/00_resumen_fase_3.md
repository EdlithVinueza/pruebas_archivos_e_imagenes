# Fase 3: Recepción y Validación de la Firma Criptográfica

## Introducción
Una vez validada la autenticidad técnica de los archivos (Fase 1) y recopilados los datos humanos y contextuales del artista (Fase 2), el sistema requiere asegurar que el autor legítimo aprueba estos datos de forma irrefutable. Para ello, entra a la **Fase 3: Firma Criptográfica**.

En esta etapa, el artista debe cargar su certificado digital o firma electrónica en formato `.p12` (PKCS#12) junto con su contraseña.

## ¿Por qué el archivo .p12 tiene una clave o contraseña?

El archivo `.p12` es un contenedor seguro cifrado (Keystore) estandarizado. Su propósito principal no es solo guardar un "nombre", sino almacenar una **Llave Privada (Private Key)** matemática. 

Si un atacante obtuviese el archivo `.p12` sin contraseña, podría firmar digitalmente cualquier documento haciéndose pasar por el artista, cometiendo robo de identidad o fraude autoral. La contraseña actúa como la clave que desencripta el contenedor; sin ella, es matemáticamente imposible desbloquear el archivo para extraer la Llave Privada y realizar una firma digital legítima.

## Flujo de Validación y Extracción de Datos

Cuando el sistema recibe el archivo `.p12` y la contraseña, realiza los siguientes pasos utilizando la librería **Bouncy Castle**:

1. **Desbloqueo del Keystore:** Intenta abrir el archivo usando la contraseña proporcionada. Si la contraseña es incorrecta o el archivo está corrupto, la librería rechaza el acceso y lanza una excepción de seguridad.
2. **Validación de Vigencia:** A través del método `certificate.checkValidity()`, el sistema revisa internamente que la fecha y hora actual del servidor se encuentren dentro del periodo de validez del certificado. Si el certificado ya expiró (Expired) o su fecha de validez aún no inicia (Not Yet Valid), la firma es rechazada automáticamente.
3. **Extracción de Identidad:** Del certificado público `X509Certificate` se extrae el "Distinguished Name" (DN) o Nombre Distinguido del Sujeto (Propietario). El algoritmo fragmenta este texto buscando etiquetas específicas como `CN=` (Common Name / Nombre real), `OU=` (Organizational Unit) y el `SERIALNUMBER=` (Cédula o Número de Identificación Nacional).
4. **Firma Real:** Finalmente, usa la Llave Privada extraída para firmar matemáticamente los datos de la obra usando el algoritmo **ECDSA** (Elliptic Curve Digital Signature Algorithm), generando una cadena en Base64 que prueba que esa persona aprobó ese archivo específico.

### Código Completo (`AdaptadorCriptograficoBouncyCastle.java`)

A continuación, se presenta la implementación completa en Java que ejecuta la validación de vigencia, la extracción de datos y la generación de la firma.

```java
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
            // Desbloqueo del Keystore con la contraseña
            keystore.load(fis, password);
        }

        // El alias configurado en el emisor de la firma (Módulo A) es "firma_autor"
        String alias = "firma_autor";

        if (!keystore.containsAlias(alias)) {
            throw new IllegalArgumentException("El archivo P12 no contiene la llave de autor válida.");
        }

        PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, password);
        X509Certificate certificate = (X509Certificate) keystore.getCertificate(alias);

        // 1. Validar Vigencia (Fecha)
        try {
            // Verifica si el certificado es válido en la fecha y hora actuales
            certificate.checkValidity(new Date()); 
        } catch (Exception e) {
            // Si expira o es muy prematuro, lanza CertificateExpiredException o CertificateNotYetValidException
            throw new SecurityException("El certificado ha caducado o aún no es válido: " + e.getMessage());
        }

        // 2. Extraer datos importantes del certificado (Distinguished Name)
        String issuerFull = certificate.getIssuerX500Principal().getName();
        String subjectFull = certificate.getSubjectX500Principal().getName();

        // 3. Parsear etiquetas específicas del Sujeto (Nombre y Número de Serie/Cédula)
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

    // Clase interna (DTO) para devolver los resultados extraídos al caso de uso
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
            // Se utiliza el estándar robusto ECDSA para la firma digital
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
```

## Dependencias, JSON y el Proceso de Firma

En esta fase, la estructura JSON consolidada previamente juega su rol más importante en todo el flujo de certificación.

**El JSON como Insumo de la Firma:**
El algoritmo de firma digital requiere un "Mensaje" exacto para firmar. En nuestro sistema, este mensaje es la representación en texto puro (String) del JSON completo (`PayloadForense`). Al firmar el JSON, estamos asegurando criptográficamente que:
1. Ningún dato humano (autor, título de la obra) fue alterado.
2. Ningún hash técnico (SHA-256, pHash extraído en la Fase 1) fue modificado.

Si un atacante cambia una sola coma en el JSON o un bit de la imagen después de generado, la validación criptográfica (ECDSA) detectará la discrepancia y declarará la prueba como manipulada.

* **Dependencias Empleadas:** 
  - **Bouncy Castle Provider** (`org.bouncycastle:bcprov-jdk15on`): Librería de criptografía especializada y robusta utilizada para leer los contenedores `.p12`, extraer las llaves y generar la firma ECDSA.
  - **Jackson / Gson**: Fundamental para asegurar la serialización determinista del JSON antes de inyectar sus bytes a la función de firma.
