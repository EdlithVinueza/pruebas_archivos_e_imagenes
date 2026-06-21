# Migración del Generador de PDF a Thymeleaf + HTML2PDF y SHA-256 a SHA-512

Se modificarán dos aspectos principales en la arquitectura:
1. Generación de PDF: Se utilizará una plantilla HTML (Thymeleaf) en lugar de dibujado con iText puro.
2. Criptografía: Se reemplazará el algoritmo hash SHA-256 por SHA-512 en todo el ciclo de vida del expediente.

## Cambios Propuestos

### 1. Migración a SHA-512
Se refactorizarán las clases de dominio, modelo e infraestructura que hacen referencia a SHA-256 para utilizar SHA-512:
- **Modelo:** `HashesEvidencia.java`, `ContextoProceso.java`, `EventoAnalisisCompletado.java` (cambiar variables `sha256PSD`, `sha256Imagen` a `sha512PSD`, `sha512Imagen`).
- **Servicio:** Renombrar `HashSHA256Service.java` a `HashSHA512Service.java`.
- **Adaptadores:** Renombrar `SHA256Adapter.java` a `SHA512Adapter.java` y modificar el método de instanciación del MessageDigest para que use `MessageDigest.getInstance("SHA-512")`.
- **Runners y Tests:** Actualizar `ProcesoCertificacionRunner.java` y `ProcesoCertificacionTest.java` para utilizar los nuevos nombres de servicio y clases.
- **Firma:** Validar `FirmadorP12Adapter.java` y `FirmadorPDFAdapter.java` si es que requieren especificar SHA-512 como algoritmo de hashing interno.

### 2. Dependencias (build.gradle.kts)
#### [MODIFY] build.gradle.kts
- Añadir `implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")` para el motor de plantillas HTML.
- Añadir `implementation("com.itextpdf:html2pdf:4.0.5")` para convertir de HTML a PDF.

### 3. Recursos (Plantilla HTML)
#### [NEW] src/main/resources/templates/certificado.html
- Se creará este archivo utilizando la estructura HTML proporcionada por el usuario en la solicitud, ajustando la etiqueta `SHA-256` a `SHA-512`.

### 4. Adaptador de Infraestructura PDF
#### [MODIFY] src/main/java/ec/edu/uce/certificadorforense/infrastructure/adapters/pdf/GeneradorPDFAdapter.java
- Se inicializará `TemplateEngine` de Thymeleaf configurado con un `ClassLoaderTemplateResolver`.
- Se mapearán los datos del `Expediente` y `Certificado` en un `org.thymeleaf.context.Context`.
- Se procesará la plantilla HTML `certificado.html` obteniendo un `String`.
- Se convertirá el HTML resultante a PDF utilizando `com.itextpdf.html2pdf.HtmlConverter`.

## Verificación
Se ejecutará nuevamente el `ProcesoCertificacionTest` para comprobar que:
1. El proyecto compila con las nuevas dependencias y renombramientos a SHA-512.
2. Los hashes generados ahora corresponden a SHA-512.
3. El archivo `CERT-xxxxxx.pdf` se genera sin errores con el nuevo diseño HTML y especifica "SHA-512".

## Verificación
Se ejecutará nuevamente el `ProcesoCertificacionTest` para comprobar que:
1. El proyecto compila con las nuevas dependencias.
2. El HTML se carga y compila correctamente.
3. El archivo `CERT-xxxxxx.pdf` se genera sin errores con el nuevo diseño HTML.
