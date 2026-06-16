package org.example.analisis.infrastructure.adapters.outbound;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegXmpRewriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;

public class ImageMetadataInjectorAdapter {

    /**
     * Inyecta el JSON dentro de la imagen dependiendo de su formato.
     */
    public byte[] injectMetadata(byte[] originalImage, String format, String jsonPayload) throws Exception {
        if (format.equalsIgnoreCase("png")) {
            return injectPngTextChunk(originalImage, "VerisArt", jsonPayload);
        } else if (format.equalsIgnoreCase("jpeg") || format.equalsIgnoreCase("jpg")) {
            return injectJpegXmp(originalImage, jsonPayload);
        } else {
            throw new IllegalArgumentException("Formato no soportado para inyección forense: " + format);
        }
    }

    /**
     * Inyecta un bloque XMP en un JPEG sin alterar los píxeles visuales usando Apache Commons Imaging.
     */
    private byte[] injectJpegXmp(byte[] jpegBytes, String jsonPayload) throws IOException, ImageReadException, ImageWriteException {
        // Envolvemos el JSON en un formato XMP básico válido
        String xmpXml = "<?xpacket begin=\"\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n" +
                "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" +
                "  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" +
                "    <rdf:Description rdf:about=\"\"\n" +
                "        xmlns:verisart=\"http://verisart.org/schema/\">\n" +
                "      <verisart:ForensePayload><![CDATA[" + jsonPayload + "]]></verisart:ForensePayload>\n" +
                "    </rdf:Description>\n" +
                "  </rdf:RDF>\n" +
                "</x:xmpmeta>\n" +
                "<?xpacket end=\"w\"?>";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new JpegXmpRewriter().updateXmpXml(jpegBytes, baos, xmpXml);
        return baos.toByteArray();
    }

    /**
     * Inyecta un chunk tEXt en un PNG justo antes del IEND.
     */
    private byte[] injectPngTextChunk(byte[] pngBytes, String keyword, String text) throws IOException {
        // Formato del chunk tEXt: Keyword + null (1 byte) + Text
        byte[] keywordBytes = keyword.getBytes(StandardCharsets.ISO_8859_1);
        byte[] textBytes = text.getBytes(StandardCharsets.ISO_8859_1);
        byte[] chunkData = new byte[keywordBytes.length + 1 + textBytes.length];
        
        System.arraycopy(keywordBytes, 0, chunkData, 0, keywordBytes.length);
        chunkData[keywordBytes.length] = 0; // Null separator
        System.arraycopy(textBytes, 0, chunkData, keywordBytes.length + 1, textBytes.length);

        // Calcular CRC32 sobre Tipo ("tEXt") + Datos
        CRC32 crc = new CRC32();
        crc.update("tEXt".getBytes(StandardCharsets.US_ASCII));
        crc.update(chunkData);
        int crcValue = (int) crc.getValue();

        // Buscar el IEND (00 00 00 00 49 45 4E 44 AE 42 60 82) al final del archivo
        int iendIndex = -1;
        for (int i = pngBytes.length - 12; i >= 0; i--) {
            if (pngBytes[i+4] == 'I' && pngBytes[i+5] == 'E' && pngBytes[i+6] == 'N' && pngBytes[i+7] == 'D') {
                iendIndex = i;
                break;
            }
        }

        if (iendIndex == -1) {
            throw new IOException("No se encontró el chunk IEND en el PNG.");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Escribir todo hasta el inicio del IEND
        baos.write(pngBytes, 0, iendIndex);

        // Escribir nuevo chunk tEXt
        baos.write(ByteBuffer.allocate(4).putInt(chunkData.length).array()); // Longitud
        baos.write("tEXt".getBytes(StandardCharsets.US_ASCII)); // Tipo
        baos.write(chunkData); // Datos
        baos.write(ByteBuffer.allocate(4).putInt(crcValue).array()); // CRC

        // Escribir IEND original (los últimos 12 bytes)
        baos.write(pngBytes, iendIndex, 12);

        return baos.toByteArray();
    }
}
