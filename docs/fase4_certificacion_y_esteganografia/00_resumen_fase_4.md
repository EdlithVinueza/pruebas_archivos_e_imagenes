# Fase 4: Certificación, Esteganografía Forense y Archivo Híbrido

## Introducción

La Fase 4 es el último eslabón del proceso de peritaje. Una vez recopilados los análisis técnicos (Fase 1), los datos del autor (Fase 2) y su firma digital validada (Fase 3), todo debe ser empaquetado y sellado criptográficamente para emitir el certificado final.

El objetivo central es vincular indisolublemente el **Certificado Pericial en PDF** con la **Obra de Arte (PNG/JPEG)** exportada, creando un archivo inmutable que sirva como prueba forense autónoma en caso de plagio.

## Estructura del JSON Payload: La Fuente de Verdad

El corazón de la inyección esteganográfica es un bloque de datos estructurado en formato JSON. Este bloque funciona como la única "Fuente de Verdad" de todo el proceso forense.

```json
{
  "version": "1.1",
  "autor": { "nombre": "Edlith Vinueza", "cedula": "UCE-77765" },
  "obra": { "titulo": "Nombre", "software": "FireAlpaca", "herramientas": "Huion" },
  "analisisForense": { "hashSHA256": "...", "hashPerceptual": "..." },
  "datosCertificado": { "id": "VA-2026...", "entidad": "VerisArt", "estado": "VALIDO" },
  "firmaDigital": { "algoritmo": "SHA256withECDSA", "valorFirma": "MEYC..." }
}
```

**¿Por qué se eligieron exactamente estos campos?**
Porque construyen la trazabilidad completa y legal. Conectan de manera irrefutable la **Evidencia Técnica Matemática** (`hashSHA256`, `hashPerceptual` de la Fase 1) con la **Identidad Humana** (Autor y Firma digital de las Fases 2 y 3), bajo la estampa de tiempo y garantía de un **Tercero de Confianza** (Entidad Certificadora). Sin uno de estos pilares unidos en el mismo JSON, la prueba pericial carecería de validez legal en un tribunal.

## Detalle de Pasos de la Fase 4

Para lograr este cometido sin alterar la evidencia, el sistema divide el empaquetado en dos procesos técnicos forenses:

* **[1. Esteganografía Forense y Metadata no Destructiva](01_esteganografia_metadata.md)**
* **[2. Flujo de Fusión Física (EOF) e Inyección de PDF](02_generacion_pdf_y_fusion_eof.md)**
* **[3. Generación de Certificado PDF Minimalista (Autónomo)](03_generacion_pdf_minimalista.md)**

## Dependencias Utilizadas en la Fase 4

Para llevar a cabo la generación final del JSON, su inyección y la creación del PDF, se utilizan dependencias especializadas del estándar de la industria:
* **Jackson (`com.fasterxml.jackson.core:jackson-databind`):** Utilizada para convertir la estructura del `PayloadForense` en el texto JSON final determinista que sirve como "Fuente de Verdad" y que se incrustará en la metadata de la imagen.
* **iText 7 (`com.itextpdf:itext7-core` y `html2pdf`):** Para la generación del certificado legal en PDF basado en plantillas HTML y la inyección visual de los datos, evitando librerías obsoletas (como Flying Saucer).
* **Thymeleaf (`org.thymeleaf:thymeleaf`):** Para inyectar las variables del JSON como texto estructurado dentro de la plantilla HTML antes de convertirla a PDF.
* **ZXing (`com.google.zxing:core`):** Para la codificación de los datos esenciales del JSON en texto plano y la generación del Código QR Autónomo de validación offline.
