# Arquitectura Hexagonal

El presente proyecto está construido utilizando la **Arquitectura Hexagonal** (también conocida como patrón de Puertos y Adaptadores).

El **porqué** de esta decisión radica en la necesidad crítica de aislar completamente la lógica pura de nuestro análisis forense (el "Core" o núcleo de la aplicación) de las tecnologías externas, librerías de terceros o la forma en la que el usuario interactúa con el sistema.

Al separar el negocio de la tecnología, aseguramos que nuestro algoritmo forense no se rompa si el día de mañana cambiamos la librería que lee las imágenes o la base de datos, o si incluso llegáramos a cambiar de framework a futuro .

## Estructura de Carpetas

A continuación, se detallara qué contiene cada carpeta principal y el **porqué** de su existencia:

### 1. Capa `core/` (El Núcleo del Negocio)

Aquí se encuentra no solo la lógica de validación forense, sino también toda la lógica de **evaluación de firmas y generación de certificados**.
**Por qué existe:** Para garantizar que nuestras reglas de negocio sean puras y no dependan de ninguna librería externa (cero dependencias a bases de datos, frameworks web o librerías de metadatos específicas en el caso de nuestro proyecto).

* **`model/`**: Guarda las entidades principales del dominio (ej. `ArchivoPSD`, `ArchivoImagen`, `Certificado`, `FirmaDigital`). Representan los conceptos fundamentales de nuestro análisis y certificación, estructurados como datos puros.
* **`rules/`**: Guarda las reglas de validación forense (ej. coherencia de origen, validación de capas del PSD) y las reglas de evaluación de firmas (validación de hashes y extracción esteganográfica). Aquí reside la "inteligencia" del peritaje y la seguridad.
* **`ports/`**: Guarda las interfaces (Puertos). 
	* **Por qué existen:** Porque el `core` necesita comunicarse con el mundo exterior para obtener datos (ej. leer un archivo) o emitir resultados (ej. guardar un certificado), pero no le importa *cómo* se hace. Los puertos definen las "promesas" que la infraestructura deberá cumplir.
* **`service/`**: Contiene los casos de uso generales (ej. `ValidadorGenericoService`, `CertificacionService`), que orquestan el flujo de trabajo, evalúan las firmas y emiten el certificado final.

### 2. Capa `infrastructure/` (Los Detalles Tecnológicos)

Aquí vive todo lo que toca el "mundo exterior".
**Por qué existe:** Para concentrar todas las dependencias tecnológicas en un solo lugar. Si cambiamos la forma en la que leemos un archivo PNG o PSD, solo modificaremos esta carpeta; el `core` no se enterará de los cambios ni tampoco se vera afectado.

* **`adapters/`**: Contiene las implementaciones técnicas de los puertos definidos en el `core`.
  * **`extractors/`**: Guarda los adaptadores que interactúan con librerías de terceros (ej. extractores de metadatos Exif/XMP) para sacar información bruta del archivo.
  * **`processors/`**: Contiene la lógica específica de lectura de bajo nivel (archivos binarios a la memoria), como el `ArchivoImagenProcessor` y el `ArchivoPSDProcessor`.
  * **`outbound/`**: (Ej. `AdaptadorEsteganografia`) Implementaciones técnicas complejas. Aquí se manejan los algoritmos de inyección y extracción esteganográfica para la firma digital de las obras, así como la generación física de los documentos PDF de certificación. Utilizan manipulación de bytes a bajo nivel dependientes del formato (PNG/JPEG) y librerías criptográficas.

