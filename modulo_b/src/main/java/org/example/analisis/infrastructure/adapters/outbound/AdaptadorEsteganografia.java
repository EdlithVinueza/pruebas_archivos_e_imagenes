package org.example.analisis.infrastructure.adapters.outbound;

import org.example.analisis.core.ports.outbound.ServicioEsteganograficoPort;
import org.example.analisis.infrastructure.adapters.processors.NumerosMagicos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.CRC32;

public class AdaptadorEsteganografia implements ServicioEsteganograficoPort {

    // Marcador mágico para separar la imagen del PDF en la inyección EOF
    public static final byte[] EOF_MAGIC_MARKER = "---FORENSIC-PDF-START---".getBytes(StandardCharsets.UTF_8);

    @Override
    public byte[] procesar(byte[] renderOriginal, String jsonPayload) {
        String formato = NumerosMagicos.detectarFormatoReal(renderOriginal);

        if ("PNG".equals(formato)) {
            return inyectarMetadataPNG(renderOriginal, jsonPayload);
        } else if ("JPEG".equals(formato)) {
            return inyectarMetadataJPEG(renderOriginal, jsonPayload);
        } else {
            // Fallback genérico estructurado si no es ni PNG ni JPEG
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                baos.write(renderOriginal);
                baos.write("---JSON-PAYLOAD---".getBytes(StandardCharsets.UTF_8));
                baos.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                return baos.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Error en inyección genérica: " + e.getMessage(), e);
            }
        }
    }

    private byte[] inyectarMetadataPNG(byte[] imageBytes, String jsonPayload) {
        try {
            // Estructura de chunk tEXt: Keyword + nulo + Texto
            String keyword = "JSON-PAYLOAD";
            byte[] keywordBytes = keyword.getBytes(StandardCharsets.ISO_8859_1);
            byte[] textBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
            
            byte[] chunkData = new byte[keywordBytes.length + 1 + textBytes.length];
            System.arraycopy(keywordBytes, 0, chunkData, 0, keywordBytes.length);
            chunkData[keywordBytes.length] = 0; // Separador nulo
            System.arraycopy(textBytes, 0, chunkData, keywordBytes.length + 1, textBytes.length);
            
            byte[] chunkType = "tEXt".getBytes(StandardCharsets.ISO_8859_1);
            
            // Calcular CRC32
            CRC32 crc = new CRC32();
            crc.update(chunkType);
            crc.update(chunkData);
            int crcValue = (int) crc.getValue();
            
            // IEND es siempre los últimos 12 bytes del PNG
            if (imageBytes.length < 12) return imageBytes;
            int insertPosition = imageBytes.length - 12;
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(imageBytes, 0, insertPosition);
            
            // Escribir Chunk tEXt
            baos.write(ByteBuffer.allocate(4).putInt(chunkData.length).array()); // Longitud
            baos.write(chunkType); // Tipo
            baos.write(chunkData); // Datos
            baos.write(ByteBuffer.allocate(4).putInt(crcValue).array()); // CRC
            
            // Escribir el IEND original
            baos.write(imageBytes, insertPosition, 12);
            
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error inyectando chunk tEXt en PNG", e);
        }
    }

    private byte[] inyectarMetadataJPEG(byte[] imageBytes, String jsonPayload) {
        try {
            if (imageBytes.length < 2 || imageBytes[0] != (byte)0xFF || imageBytes[1] != (byte)0xD8) {
                return imageBytes;
            }
            
            byte[] payloadBytes = jsonPayload.getBytes(StandardCharsets.UTF_8);
            // Usaremos segmento APP11 (FF EB) para evitar conflictos con EXIF (APP1)
            int segmentLength = 2 + payloadBytes.length; // 2 bytes de longitud + datos
            if (segmentLength > 65535) {
                throw new IllegalArgumentException("Payload JSON demasiado grande para un segmento JPEG");
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(0xFF); // SOI
            baos.write(0xD8);
            
            baos.write(0xFF);
            baos.write(0xEB); // Marcador APP11
            
            baos.write((segmentLength >> 8) & 0xFF);
            baos.write(segmentLength & 0xFF);
            
            baos.write(payloadBytes);
            
            // Escribir el resto de la imagen
            baos.write(imageBytes, 2, imageBytes.length - 2);
            
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error inyectando metadata en JPEG", e);
        }
    }

    @Override
    public byte[] fusionarEOF(byte[] renderConEsteganografia, byte[] certificadoPdfBytes) {
        // La inyección End-Of-File (EOF) concatena el archivo PDF directamente 
        // después de los bytes finales de la imagen (por ejemplo, después del FFD9 en JPEG).
        // Las galerías y redes leen hasta el FFD9 y lo muestran como imagen. 
        // Nuestro sistema forense leerá desde el marcador para extraer el PDF.
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(renderConEsteganografia);
            baos.write(EOF_MAGIC_MARKER);
            baos.write(certificadoPdfBytes);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error en fusión EOF: " + e.getMessage(), e);
        }
    }

    /**
     * Utilidad para extraer el PDF inyectado en el EOF para el validador
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
        return Arrays.copyOfRange(archivoHibrido, startOfPdf, archivoHibrido.length);
    }
}

