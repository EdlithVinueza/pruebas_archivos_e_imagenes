package org.example.analisis.infrastructure.adapters.outbound;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import org.example.analisis.core.model.forense.PayloadForense;
import org.example.analisis.core.ports.outbound.GeneradorPdfPort;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class AdaptadorPdfIText implements GeneradorPdfPort {

    @Override
    public byte[] generarCertificado(PayloadForense payload) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Generar los Códigos QR en Base64
            String qrBase64Autor = generarQrBase64("Firma Autor: " + payload.getAut() + "\npHash: " + payload.getPh() + "\nFirma Criptográfica: " + payload.getSig_a());
            String qrBase64CA = generarQrBase64("Emitido por: Veriart CA\nFecha: " + payload.getTs() + "\nFirma Criptográfica CA: " + payload.getSig_s());

            // HTML con el CSS ajustado y los QRs inyectados como etiquetas <img>
            String htmlTemplate = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "<meta charset=\"UTF-8\">\n" +
                    "<style>\n" +
                    "  @page { size: A4 landscape; margin: 15mm; }\n" +
                    "  body {\n" +
                    "    font-family: 'Times New Roman', serif;\n" +
                    "    margin: 0;\n" +
                    "    padding: 0;\n" +
                    "    background: white;\n" +
                    "  }\n" +
                    "  .certificado {\n" +
                    "    width: 100%;\n" +
                    "    height: 100%;\n" +
                    "    margin: 0 auto;\n" +
                    "    border: 10px double #c9a03d;\n" +
                    "    padding: 30px;\n" +
                    "    text-align: center;\n" +
                    "    position: relative;\n" +
                    "    box-sizing: border-box;\n" +
                    "  }\n" +
                    "  .titulo {\n" +
                    "    font-size: 38px;\n" +
                    "    letter-spacing: 3px;\n" +
                    "    color: #8b5a2b;\n" +
                    "    margin-top: 10px;\n" +
                    "    font-weight: bold;\n" +
                    "  }\n" +
                    "  .subtitulo {\n" +
                    "    font-size: 16px;\n" +
                    "    color: #555;\n" +
                    "    border-bottom: 1px solid #aaa;\n" +
                    "    display: inline-block;\n" +
                    "    padding-bottom: 5px;\n" +
                    "    margin-bottom: 20px;\n" +
                    "  }\n" +
                    "  .cuerpo {\n" +
                    "    font-size: 14px;\n" +
                    "    margin: 15px 0;\n" +
                    "    color: #333;\n" +
                    "  }\n" +
                    "  .linea-blanca {\n" +
                    "    background: white;\n" +
                    "    border: none;\n" +
                    "    border-bottom: 1px solid #000;\n" +
                    "    width: 80%;\n" +
                    "    margin: 15px auto;\n" +
                    "    height: 15px;\n" +
                    "  }\n" +
                    "  .hash-text {\n" +
                    "    font-family: monospace;\n" +
                    "    font-size: 11px;\n" +
                    "  }\n" +
                    "  .firma {\n" +
                    "    margin-top: 30px;\n" +
                    "    display: table;\n" +
                    "    width: 100%;\n" +
                    "  }\n" +
                    "  .firma-col {\n" +
                    "    display: table-cell;\n" +
                    "    width: 50%;\n" +
                    "    vertical-align: bottom;\n" +
                    "    text-align: center;\n" +
                    "  }\n" +
                    "  .qr-container {\n" +
                    "    margin-bottom: 10px;\n" +
                    "  }\n" +
                    "  .firma-linea {\n" +
                    "    border-top: 1px solid #000;\n" +
                    "    width: 200px;\n" +
                    "    margin: 0 auto;\n" +
                    "    padding-top: 5px;\n" +
                    "    font-size: 12px;\n" +
                    "  }\n" +
                    "  .sello {\n" +
                    "    position: absolute;\n" +
                    "    bottom: 20px;\n" +
                    "    right: 20px;\n" +
                    "    width: 80px;\n" +
                    "    height: 80px;\n" +
                    "    border-radius: 50%;\n" +
                    "    border: 2px solid #c9a03d;\n" +
                    "    color: #c9a03d;\n" +
                    "    font-size: 10px;\n" +
                    "    display: block;\n" +
                    "    text-align: center;\n" +
                    "    line-height: 80px;\n" +
                    "  }\n" +
                    "</style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<div class=\"certificado\">\n" +
                    "  <div class=\"titulo\">CERTIFICADO FORENSE</div>\n" +
                    "  <div class=\"subtitulo\">emitido por Veriart</div>\n" +
                    "  <div class=\"cuerpo\">\n" +
                    "    <p>Se certifica que la obra de arte digital con hash pHash: <b>" + payload.getPh() + "</b></p>\n" +
                    "    <p>ha sido registrada en el sistema por el autor(a): <b>" + payload.getAut() + " (" + payload.getArt() + ")</b></p>\n" +
                    "    <div class=\"linea-blanca\"></div>\n" +
                    "    <p>Código criptográfico de integridad (SHA-256):</p>\n" +
                    "    <p class=\"hash-text\">" + payload.getSha() + "</p>\n" +
                    "    <div class=\"linea-blanca\"></div>\n" +
                    "    <p>Fecha de certificación (RFC 3161): <b>" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(payload.getTs())) + "</b></p>\n" +
                    "  </div>\n" +
                    "  <div class=\"firma\">\n" +
                    "    <div class=\"firma-col\">\n" +
                    "       <div class=\"qr-container\"><img src=\"data:image/png;base64," + qrBase64Autor + "\" width=\"80\" height=\"80\" /></div>\n" +
                    "       <div class=\"firma-linea\">Firma Autorizada: " + payload.getAut() + "</div>\n" +
                    "    </div>\n" +
                    "    <div class=\"firma-col\">\n" +
                    "       <div class=\"qr-container\"><img src=\"data:image/png;base64," + qrBase64CA + "\" width=\"80\" height=\"80\" /></div>\n" +
                    "       <div class=\"firma-linea\">Sello: Veriart CA</div>\n" +
                    "    </div>\n" +
                    "  </div>\n" +
                    "  <div class=\"sello\">Veriart</div>\n" +
                    "</div>\n" +
                    "</body>\n" +
                    "</html>";

            // Inicializar PDF
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4.rotate());
            
            Document document = new Document(pdf);
            document.setMargins(0, 0, 0, 0);

            // Convertir directamente
            HtmlConverter.convertToPdf(htmlTemplate, pdf, new ConverterProperties());

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el certificado PDF forense desde HTML: " + e.getMessage(), e);
        }
    }

    /**
     * Utilidad para generar el QR en memoria usando ZXing y pasarlo a Base64
     */
    private String generarQrBase64(String contenido) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(contenido, BarcodeFormat.QR_CODE, 200, 200);
        
        try (ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();
            return Base64.getEncoder().encodeToString(pngData);
        }
    }
}

