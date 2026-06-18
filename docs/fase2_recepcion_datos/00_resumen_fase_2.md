# Fase 2: Recepción de Datos Adicionales para Certificación

## Introducción y Objetivo
Una vez que los archivos han pasado el  filtro técnico y forense de la Fase 1 (garantizando su autenticidad estructural y visual), el sistema entra a la **Fase 2**. 

El objetivo central de esta fase es complementar la prueba pericial con información contextual, personal y de autoría proporcionada directamente por el artista mediante un **formulario de datos adicionales**.

## El Problema de la Metadata en Software de Terceros

¿Por qué se pide estos datos al artista en lugar de extraerlos automáticamente de la metadata del archivo analizado?

El proceso de peritaje analiza la estructura y metadata de archivos en formato `.psd` (Adobe Photoshop Document). Sin embargo, hoy en día los artistas digitales utilizan una enorme variedad de programas de ilustración para crear su arte (como Clip Studio Paint, Krita, Procreate, Paint Tool SAI, Medibang, entre otros). 

Aunque todos estos programas modernos tienen la capacidad de exportar sus lienzos al formato estándar `.psd` conservando sus capas intactas para el análisis, **tienen una limitación técnica crucial debido a derechos de propiedad**.

Dado que el formato PSD es propiedad exclusiva y de código cerrado de Adobe, cuando un software de dibujo de terceros exporta a este formato, frecuentemente **no inyecta su firma de origen o herramientas en la metadata profunda del archivo**. Esto significa que a nivel de bytes, es computacionalmente imposible determinar con 100% de certeza en qué programa se dibujó originalmente la obra o qué tableta se usó, a menos que el archivo provenga directamente de una licencia pagada de Adobe Photoshop.

## Formulario de Datos Adicionales

Para subsanar esta "ceguera de metadata" y construir un certificado de obra robusto, íntegro y detallado, el sistema le solicita al usuario llenar los siguientes campos fundamentales:

1. **Nombre de la Obra:** Título oficial bajo el cual se registrará y certificará la ilustración digital.
2. **Software de Ilustración Utilizado:** El programa exacto en el que fue dibujada (ej. Procreate, Clip Studio Paint, Krita). Esto resuelve explícitamente el vacío de metadata mencionado anteriormente.
3. **Herramientas o Hardware Empleado:** Información sobre el equipo físico utilizado (ej. Tableta Wacom, iPad Pro con Apple Pencil, Huion ).
4. **Descripción o Concepto (Opcional):** Un breve texto descriptivo donde el artista puede detallar la inspiración, el contexto o la historia detrás de la obra.

### Conclusión de la Fase 2

Esta información es importante por que  **asocia de forma irrevocable el veredicto técnico (la validación forense de la Fase 1) con la identidad autoral humana (Fase 2)**. Una vez recolectados, estos datos pasan a la Fase 3, donde serán firmados criptográficamente para volverlos inmutables.

## Dependencias y Consolidación del JSON de Información (Payload)

Todos los datos recopilados en esta fase se estructuran y consolidan en un objeto JSON (conocido como `PayloadForense`). 

**¿Por qué usar JSON y cómo ayuda a la firma?**
Al consolidar la información del artista (Nombre, Software, Herramientas) y los hashes de la Fase 1 en un archivo JSON estandarizado, creamos una cadena de texto predecible y uniforme. Esto es un requisito técnico obligatorio antes de firmar criptográficamente (Fase 3). Una firma digital requiere una "cadena de caracteres" exacta como mensaje a firmar; el JSON proporciona esta estructura inmutable y fácilmente procesable.

* **Dependencias Empleadas:** Para la creación, validación y manejo estructurado de estos JSON, el sistema emplea la librería **Jackson** (`com.fasterxml.jackson.core:jackson-databind`). Esto garantiza que la serialización del objeto a texto puro sea estricta, determinista y no sufra alteraciones invisibles que corromperían la validación de la firma.
