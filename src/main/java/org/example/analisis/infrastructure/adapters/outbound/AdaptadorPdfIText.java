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
            String qrBase64Autor = generarQrBase64("Firma Autor: " + payload.getAutor().getNombre() + "\npHash: " + payload.getAnalisisForenseDigital().getPhashPerceptual() + "\nFirma Criptográfica: " + payload.getFirmaDigital().getValorFirma());
            String qrBase64CA = generarQrBase64("Emitido por: " + payload.getDatosDelCertificado().getEntidadEmisora() + "\nFecha: " + payload.getDatosDelCertificado().getFechaEmision() + "\nHash CA Raíz: " + payload.getDatosDelCertificado().getHashCertificadoRaiz());

            // HTML con el CSS ajustado y los QRs inyectados como etiquetas <img>
            String htmlTemplate = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "<meta charset=\"UTF-8\">\n" +
                    "<style>\n" +
                    "  @page { size: A4 landscape; margin: 12mm; }\n" +
                    "  * { box-sizing: border-box; }\n" +
                    "  body {\n" +
                    "    font-family: 'Times New Roman', Times, serif;\n" +
                    "    margin: 0;\n" +
                    "    padding: 0;\n" +
                    "    background: #ffffff;\n" +
                    "    color: #1a1a1a;\n" +
                    "  }\n" +
                    "  .certificado-borde {\n" +
                    "    width: 100%;\n" +
                    "    height: 100%;\n" +
                    "    border: 3px solid #8b5a2b;\n" +
                    "    padding: 4px;\n" +
                    "  }\n" +
                    "  .certificado-interno {\n" +
                    "    width: 100%;\n" +
                    "    height: 100%;\n" +
                    "    border: 8px double #c9a03d;\n" +
                    "    padding: 40px 50px;\n" +
                    "    text-align: center;\n" +
                    "    position: relative;\n" +
                    "    background: #fdfcf7;\n" +
                    "  }\n" +
                    "  .encabezado-institucional {\n" +
                    "    font-size: 11px;\n" +
                    "    text-transform: uppercase;\n" +
                    "    letter-spacing: 2px;\n" +
                    "    color: #555;\n" +
                    "    margin-bottom: 5px;\n" +
                    "  }\n" +
                    "  .delegacion-legal {\n" +
                    "    font-size: 9px;\n" +
                    "    font-style: italic;\n" +
                    "    color: #666;\n" +
                    "    margin-bottom: 25px;\n" +
                    "  }\n" +
                    "  .titulo {\n" +
                    "    font-size: 34px;\n" +
                    "    letter-spacing: 4px;\n" +
                    "    color: #8b5a2b;\n" +
                    "    margin: 0 0 5px 0;\n" +
                    "    font-weight: bold;\n" +
                    "    text-transform: uppercase;\n" +
                    "  }\n" +
                    "  .subtitulo {\n" +
                    "    font-size: 14px;\n" +
                    "    color: #444;\n" +
                    "    letter-spacing: 1px;\n" +
                    "    margin-bottom: 30px;\n" +
                    "  }\n" +
                    "  .declaracion {\n" +
                    "    font-size: 15px;\n" +
                    "    line-height: 1.6;\n" +
                    "    margin: 0 auto 25px auto;\n" +
                    "    max-width: 85%;\n" +
                    "    text-align: justify;\n" +
                    "    text-justify: inter-word;\n" +
                    "  }\n" +
                    "  .obra-resaltado {\n" +
                    "    font-size: 18px;\n" +
                    "    font-weight: bold;\n" +
                    "    color: #8b5a2b;\n" +
                    "    display: block;\n" +
                    "    margin: 8px 0;\n" +
                    "    text-align: center;\n" +
                    "  }\n" +
                    "  .tabla-metadatos {\n" +
                    "    width: 90%;\n" +
                    "    margin: 0 auto 30px auto;\n" +
                    "    border-collapse: collapse;\n" +
                    "    font-size: 11px;\n" +
                    "    text-align: left;\n" +
                    "  }\n" +
                    "  .tabla-metadatos td {\n" +
                    "    padding: 6px 10px;\n" +
                    "    border-bottom: 1px solid #e0e0e0;\n" +
                    "  }\n" +
                    "  .tabla-metadatos td.label {\n" +
                    "    font-weight: bold;\n" +
                    "    color: #555;\n" +
                    "    width: 25%;\n" +
                    "  }\n" +
                    "  .mono {\n" +
                    "    font-family: monospace;\n" +
                    "    font-size: 10.5px;\n" +
                    "    color: #222;\n" +
                    "  }\n" +
                    "  .seccion-firmas {\n" +
                    "    margin-top: 20px;\n" +
                    "    width: 100%;\n" +
                    "    display: table;\n" +
                    "  }\n" +
                    "  .firma-bloque {\n" +
                    "    display: table-cell;\n" +
                    "    width: 50%;\n" +
                    "    text-align: center;\n" +
                    "    vertical-align: bottom;\n" +
                    "  }\n" +
                    "  .qr-box {\n" +
                    "    margin-bottom: 8px;\n" +
                    "  }\n" +
                    "  .linea-autorizacion {\n" +
                    "    border-top: 1px solid #777;\n" +
                    "    width: 220px;\n" +
                    "    margin: 0 auto;\n" +
                    "    padding-top: 4px;\n" +
                    "    font-size: 11px;\n" +
                    "    font-weight: bold;\n" +
                    "  }\n" +
                    "  .linea-subtexto {\n" +
                    "    font-size: 9.5px;\n" +
                    "    color: #666;\n" +
                    "    margin-top: 2px;\n" +
                    "  }\n" +
                    "  .identificador-certificado {\n" +
                    "    position: absolute;\n" +
                    "    top: 20px;\n" +
                    "    right: 25px;\n" +
                    "    font-family: monospace;\n" +
                    "    font-size: 10px;\n" +
                    "    color: #777;\n" +
                    "  }\n" +
                    "  .sello-agua {\n" +
                    "    position: absolute;\n" +
                    "    bottom: 25px;\n" +
                    "    left: 25px;\n" +
                    "    width: 75px;\n" +
                    "    height: 75px;\n" +
                    "    border: 2px dashed #c9a03d;\n" +
                    "    border-radius: 50%;\n" +
                    "    color: #c9a03d;\n" +
                    "    font-size: 9px;\n" +
                    "    font-weight: bold;\n" +
                    "    text-transform: uppercase;\n" +
                    "    line-height: 71px;\n" +
                    "    letter-spacing: 1px;\n" +
                    "    opacity: 0.7;\n" +
                    "  }\n" +
                    "</style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "<div class=\"certificado-borde\">\n" +
                    "  <div class=\"certificado-interno\">\n" +
                    "    <div class=\"identificador-certificado\">REGISTRO ID: " + payload.getDatosDelCertificado().getIdCertificado() + "</div>\n" +
                    "    <div class=\"encabezado-institucional\">" + payload.getDatosDelCertificado().getEntidadEmisora() + "</div>\n" +
                    "    <div class=\"delegacion-legal\">" + payload.getDatosDelCertificado().getAutoridadDelegatoria() + "</div>\n" +
                    "    \n" +
                    "    <div class=\"titulo\">Certificado de Autenticidad</div>\n" +
                    "    <div class=\"subtitulo\">Auditoría Forense Estructural de Obra Digital</div>\n" +
                    "    \n" +
                    "    <div class=\"declaracion\">\n" +
                    "      Se hace constar bajo fe de registro técnico que la ilustración digital titulada \n" +
                    "      <span class=\"obra-resaltado\">\"" + payload.getObra().getTitulo() + "\"</span>\n" +
                    "      ha sido analizada y firmada digitalmente, garantizando su integridad binaria y su no repudio de origen en la fecha declarada por el autor(a):\n" +
                    "      <strong>" + payload.getAutor().getNombre() + "</strong> (ID: " + payload.getAutor().getIdInstitucional() + ").\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <table class=\"tabla-metadatos\">\n" +
                    "      <tr>\n" +
                    "        <td class=\"label\">Hash Perceptual (pHash):</td>\n" +
                    "        <td class=\"mono\"><b>" + payload.getAnalisisForenseDigital().getPhashPerceptual() + "</b> (Invariabilidad visual ante compresión)</td>\n" +
                    "        <td class=\"label\">Software Declarado:</td>\n" +
                    "        <td>" + payload.getObra().getSoftwareOriginal() + "</td>\n" +
                    "      </tr>\n" +
                    "      <tr>\n" +
                    "        <td class=\"label\">Integridad Binaria (SHA-256):</td>\n" +
                    "        <td class=\"mono\">" + payload.getAnalisisForenseDigital().getSha256Criptografico() + "</td>\n" +
                    "        <td class=\"label\">Hardware Empleado:</td>\n" +
                    "        <td>" + payload.getObra().getHardwareAdicional() + "</td>\n" +
                    "      </tr>\n" +
                    "      <tr>\n" +
                    "        <td class=\"label\">Sello de Tiempo (Emisión):</td>\n" +
                    "        <td>" + payload.getDatosDelCertificado().getFechaEmision() + "</td>\n" +
                    "        <td class=\"label\">Fecha de Creación:</td>\n" +
                    "        <td>" + payload.getObra().getFechaDeclaradaCreacion() + "</td>\n" +
                    "      </tr>\n" +
                    "    </table>\n" +
                    "    \n" +
                    "    <div class=\"seccion-firmas\">\n" +
                    "      <div class=\"firma-bloque\">\n" +
                    "        <div class=\"qr-box\"><img src=\"data:image/png;base64," + qrBase64Autor + "\" width=\"75\" height=\"75\" /></div>\n" +
                    "        <div class=\"linea-autorizacion\">Firma Digital del Autor (" + payload.getFirmaDigital().getAlgoritmo() + ")</div>\n" +
                    "        <div class=\"linea-subtexto\">" + payload.getAutor().getIdInstitucional() + "</div>\n" +
                    "      </div>\n" +
                    "      <div class=\"firma-bloque\">\n" +
                    "        <div class=\"qr-box\"><img src=\"data:image/png;base64," + qrBase64CA + "\" width=\"75\" height=\"75\" /></div>\n" +
                    "        <div class=\"linea-autorizacion\">Sello de Confianza: " + payload.getDatosDelCertificado().getEntidadEmisora() + "</div>\n" +
                    "        <div class=\"linea-subtexto\">" + payload.getDatosDelCertificado().getAutoridadDelegatoria() + "</div>\n" +
                    "      </div>\n" +
                    "    </div>\n" +
                    "    \n" +
                    "    <div class=\"sello-agua\">VERISART</div>\n" +
                    "  </div>\n" +
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

