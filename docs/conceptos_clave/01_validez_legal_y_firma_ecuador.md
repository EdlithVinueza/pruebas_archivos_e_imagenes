# Validez Legal y Firma Criptográfica: Comparativa con el Sistema Ecuatoriano

El **sistema realiza una firma electrónica real y matemáticamente válida**, con el mismo peso legal que los sistemas gubernamentales tradicionales. No se trata de una marca de agua ni de un texto oculto simple; es criptografía asimétrica aplicada a la prueba digital.

## 1. ¿Cómo se firma matemáticamente un documento? (El Estándar Universal)

Antes de hablar de leyes o sistemas específicos, debemos entender que "firmar electrónicamente" no es poner un dibujo de una firma en un PDF, sino un proceso puramente matemático y criptográfico que consta de dos pasos universales:

1. **El Cálculo del Hash:** El sistema toma el archivo original (sea un texto o una imagen) y lo pasa por un algoritmo (como SHA-256) que calcula su "huella digital". Si se cambia un solo byte del archivo, esta huella cambia por completo.
2. **La Encriptación Asimétrica:** El sistema toma esa huella digital y la encripta utilizando una **Llave Privada** matemática (que solo el dueño conoce y que suele venir en un archivo seguro como el `.p12`). El resultado de esta encriptación es una cadena de texto ininteligible (ej. `MEYCIQ...`). **Esa cadena de texto es la firma real.**

Cualquiera puede usar la "Llave Pública" del autor para desencriptar esa firma y verificar que la huella coincide con el documento, probando que el documento no fue alterado y que el autor legítimo lo aprobó irrevocablemente.

## 2. El Sistema Ecuatoriano (FirmaEC)

En Ecuador, el sistema oficial de firma electrónica (FirmaEC) está regulado por la **Ley de Comercio Electrónico, Firmas Electrónicas y Mensajes de Datos**. Este sistema nacional aplica el estándar matemático universal de la siguiente manera:

- **Certificados Legales:** El usuario solicita su certificado digital (`.p12` o Token) a una Entidad de Certificación autorizada (la entidad autorizadora es el ARCOTEL), como el Banco Central del Ecuador, Security Data o UANATACA.
- **Flujo en FirmaEC:** El usuario abre la aplicación FirmaEC, sube un documento y digita la contraseña de su certificado.
- **El Contenedor (PAdES):** FirmaEC calcula el Hash, lo firma con la Llave Privada, y finalmente inyecta esa firma matemática **dentro del código interno del PDF** usando un estándar llamado PAdES. Está diseñado de forma exclusiva para documentos ofimáticos (PDFs) o facturas electrónicas (XML).

**El problema para el Arte Digital:** Si un ilustrador quiere firmar legalmente su lienzo `.png` o `jpeg`, FirmaEC no sabe cómo inyectar la firma sin dañar imagen. El artista se vería forzado a "imprimir" o convertir su arte digital en un  documento PDF, destruyendo por completo la naturaleza visual y metadatos de su obra (ya no podría publicarla nativamente en portafolios como ArtStation o Instagram).

## 3. Nuestro Proyecto: El Sistema Forense de Certificación de Obra

Nuestro sistema opera bajo **el mismo principio legal y criptográfico** que el Ecuador, pero innova y evoluciona "el contenedor" para adaptarlo estrictamente a las necesidades del arte digital.

1. **Matemática Idéntica:** El sistema le solicita al artista su mismo archivo `.p12` legítimo ecuatoriano, extrae la llave y calcula matemáticamente la firma sobre la huella digital (Hash SHA-256) de su ilustración original. La validación matemática es exactamente idéntica a la que realiza FirmaEC.
2. **La Innovación del Contenedor (Esteganografía e Híbridos):** En lugar de forzar a la imagen a volverse un PDF ofimático, nuestro sistema almacena el resultado de la firma dentro de un bloque de datos estructurado (JSON).
3. Posteriormente, inyecta este JSON utilizando **Esteganografía Estructural no Destructiva** directamente en la metadata interna de la imagen nativa (en *Chunks* especiales de PNG o *Segmentos* de JPEG). 
4. Finalmente, anexa un certificado legal detrás del archivo visual (Fusión *End-Of-File* o EOF).

## Conclusión

Nuestro sistema soluciona un vacío técnico y legal para los artistas digitales: les permite obtener una certificación pericial con la máxima **validez legal** (usando los certificados `.p12` y algoritmos de cifrado aprobados del Ecuador), pero **sin destruir la naturaleza visual de su obra**. La ilustración certificada sigue comportándose como una imagen perfecta para redes sociales y galerías web, pero lleva integrada estructuralmente su prueba forense criptográfica inmutable.
