# 2. Flujo de Generación de PDF y Fusión EOF

> [!NOTE]
> **Referencia de Diseño y Estructura:** La estructura visual, disposición de datos y estilo probatorio del certificado PDF se inspiró en los estándares de certificaciones profesionales presentados en este [video de referencia (YouTube)](https://www.youtube.com/watch?v=7YjN06ySwl8).

## ¿Por qué primero generamos el PDF y luego fusionamos?

El proceso lógico dictado por el sistema obedece a un estricto orden matemático:

1. Se recopilan todos los hashes, firmas, datos de la obra y autor.
2. Se renderiza y genera el **Certificado Visual en PDF**. 
3. Se **Inyecta el JSON Payload** en los metadatos de la imagen original PNG/JPEG (como se vio en el paso anterior).
4. Se ejecuta la **Fusión EOF (End-Of-File)**: Se toman los bytes brutos del PDF recién creado y se pegan físicamente al final de la imagen.

Se debe hacer en este orden porque el certificado legal PDF (que contiene los hashes impresos visualmente y sellos) debe existir y finalizarse físicamente para poder anexarlo íntegramente detrás de la imagen como una "mochila" adjunta.

## El Archivo Híbrido o Políglota

El resultado de esta técnica EOF es un **Archivo Híbrido** (técnicamente llamado archivo políglota). 

- Si el usuario sube la imagen a Instagram, Twitter o la abre en la galería de Windows, el sistema lee hasta el marcador `IEND` (PNG) o `FF D9` (JPEG) y descarta el resto de bytes silenciosamente. **Se ve la ilustración perfectamente**.
- Si el usuario toma el mismo archivo `.png` y simplemente le cambia la extensión a `.pdf`, el lector de PDF ignorará los bytes "basura" del inicio (la imagen), encontrará la cabecera PDF `%PDF-1.4` en la zona EOF y **abrirá el documento legal**.

Esta es la forma definitiva de tener evidencia indivisible: es matemáticamente imposible perder el certificado si la imagen en sí misma *es* el certificado.

## Algoritmo de Fusión y Extracción (`AdaptadorEsteganografia.java` Parte 2)

A continuación, se detalla cómo se concatenan y extraen estos datos usando un *Magic Marker* para separar claramente dónde termina la imagen y empieza el PDF incrustado.

```java
// Fragmentos de AdaptadorEsteganografia.java relacionados a EOF
package org.example.analisis.infrastructure.adapters.outbound;

import org.example.analisis.core.ports.outbound.ServicioEsteganograficoPort;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class AdaptadorEsteganografia implements ServicioEsteganograficoPort {

    // Marcador mágico para separar la imagen del PDF en la inyección EOF
    public static final byte[] EOF_MAGIC_MARKER = "---FORENSIC-PDF-START---".getBytes(StandardCharsets.UTF_8);

    @Override
    public byte[] fusionarEOF(byte[] renderConEsteganografia, byte[] certificadoPdfBytes) {
        // La inyección End-Of-File (EOF) concatena el archivo PDF directamente 
        // después de los bytes finales de la imagen (por ejemplo, después del FFD9 en JPEG o IEND en PNG).
        // Las galerías y redes leen hasta el marcador de fin y lo muestran como imagen. 
        // Nuestro sistema forense leerá desde el marcador EOF_MAGIC_MARKER para extraer el PDF.
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(renderConEsteganografia);
            baos.write(EOF_MAGIC_MARKER);
            baos.write(certificadoPdfBytes); // Pegamos el binario del PDF al final
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error en fusión EOF: " + e.getMessage(), e);
        }
    }

    /**
     * Utilidad para extraer el PDF inyectado en el EOF para el validador o juez forense.
     */
    public byte[] extraerPdfDeEOF(byte[] archivoHibrido) {
        // Buscar el marcador mágico desde el final hacia el inicio
        String hexContent = new String(archivoHibrido, StandardCharsets.ISO_8859_1);
        String marker = new String(EOF_MAGIC_MARKER, StandardCharsets.ISO_8859_1);
        
        int markerIndex = hexContent.lastIndexOf(marker);
        if (markerIndex == -1) {
            throw new IllegalArgumentException("No se encontró evidencia PDF incrustada por EOF en este archivo.");
        }
        
        int startOfPdf = markerIndex + EOF_MAGIC_MARKER.length;
        // Retornar solo el sub-arreglo de bytes que representa al PDF
        return Arrays.copyOfRange(archivoHibrido, startOfPdf, archivoHibrido.length);
    }
}
```
