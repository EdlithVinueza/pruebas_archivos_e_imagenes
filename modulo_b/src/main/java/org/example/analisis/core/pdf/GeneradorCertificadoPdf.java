package org.example.analisis.core.pdf;

import com.itextpdf.html2pdf.HtmlConverter;
import org.example.analisis.core.model.forense.PayloadForense;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.File;
import java.io.FileOutputStream;

public class GeneradorCertificadoPdf {

    private final TemplateEngine templateEngine;

    public GeneradorCertificadoPdf() {
        // Configurar Thymeleaf para leer desde src/main/resources/templates/
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);
    }

    /**
     * Genera el certificado PDF final para la obra.
     *
     * @param payload El PayloadForense con todos los metadatos.
     * @param rutaDestino La ruta donde se guardará el archivo PDF (ej. "certificados/CERT-2026.pdf")
     * @param miniaturaBase64 (Opcional) La miniatura de la imagen en Base64. Puede ser null.
     */
    public void generarCertificado(PayloadForense payload, String rutaDestino, String miniaturaBase64) {
        try {
            // 1. Generar el QR Autónomo
            String qrBase64 = GeneradorQR.generarQrBase64(payload);

            // 2. Preparar el contexto (las variables) para Thymeleaf
            Context context = new Context();
            context.setVariable("idCertificado", payload.getDatosDelCertificado() != null ? payload.getDatosDelCertificado().getIdCertificado() : "N/A");
            context.setVariable("fechaEmision", payload.getDatosDelCertificado() != null ? payload.getDatosDelCertificado().getFechaEmision() : "N/A");
            context.setVariable("versionMetadatos", payload.getMetadataVersion() != null ? payload.getMetadataVersion() : "1.0");
            
            context.setVariable("autorObra", payload.getAutor() != null ? payload.getAutor().getNombre() : "N/A");
            context.setVariable("seudonimo", payload.getAutor() != null ? payload.getAutor().getSeudonimo() : null);
            context.setVariable("idInstitucional", payload.getAutor() != null ? payload.getAutor().getIdInstitucional() : "N/A");
            
            context.setVariable("tituloObra", payload.getObra() != null ? payload.getObra().getTitulo() : "N/A");
            context.setVariable("fechaCreacion", payload.getObra() != null ? payload.getObra().getFechaDeclaradaCreacion() : "N/A");
            context.setVariable("software", payload.getObra() != null && payload.getObra().getSoftwareOriginal() != null ? payload.getObra().getSoftwareOriginal() : "N/A");
            context.setVariable("hardware", payload.getObra() != null && payload.getObra().getHardwareAdicional() != null ? payload.getObra().getHardwareAdicional() : "N/A");
            context.setVariable("detallesTecnicos", payload.getObra() != null && payload.getObra().getDetallesTecnicos() != null ? payload.getObra().getDetallesTecnicos() : "N/A");
            
            context.setVariable("dimensiones", payload.getAnalisisForenseDigital() != null ? payload.getAnalisisForenseDigital().getDimensiones() : "N/A");
            context.setVariable("sha256", payload.getAnalisisForenseDigital() != null ? payload.getAnalisisForenseDigital().getSha256Criptografico() : "N/A");
            context.setVariable("phash", payload.getAnalisisForenseDigital() != null ? payload.getAnalisisForenseDigital().getPhashPerceptual() : "N/A");
            context.setVariable("estado", payload.getDatosDelCertificado() != null ? payload.getDatosDelCertificado().getEstadoInicial() : "N/A");
            
            // Inyectar imágenes Base64
            context.setVariable("qrBase64", qrBase64);
            context.setVariable("obraBase64", miniaturaBase64); // Si es null, Thymeleaf lo ignora

            // 3. Procesar el HTML
            String htmlRenderizado = templateEngine.process("certificado_minimalista", context);

            // 4. Convertir HTML a PDF usando iText html2pdf
            File archivoDestino = new File(rutaDestino);
            // Asegurarnos de que el directorio exista
            if (archivoDestino.getParentFile() != null) {
                archivoDestino.getParentFile().mkdirs();
            }

            HtmlConverter.convertToPdf(htmlRenderizado, new FileOutputStream(archivoDestino));
            
            System.out.println("✅ Certificado PDF generado con éxito en: " + archivoDestino.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("❌ Error al generar el certificado PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
