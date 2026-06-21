# Plan de Implementación — Sistema de Certificación Forense Verisart

## Contexto y Estado Actual

El proyecto ya tiene construida una **base sólida** de análisis forense (Fase 1):

- ✅ Modelos: `ArchivoBase`, `ArchivoPSD`, `ArchivoImagen`, `MetadatosPSD`, `MetadatosImagen`, `VeredictoFinal`
- ✅ Reglas de validación: `ReglaFormatoPsd`, `ReglaResolucionProfesional`, `ReglaImagenPegada`, `ReglaComplejidadDiseno`, `ReglaFirmaEstructural`, `ReglaCoherenciaDpi`, `ReglaAnalisisOrigen`
- ✅ Servicios: `ValidadorGenericoService`, `CalculadorPHash`, `ArchivoProcessorFactory`
- ✅ Infraestructura: `ArchivoPSDProcessor`, `ArchivoImagenProcessor`, `ExtractorCapasPSD`, etc.
- ✅ Dependencias: `metadata-extractor`, `TwelveMonkeys`, `Lombok`, `JUnit 5`

---

## Decisiones de Arquitectura Confirmadas

| Decisión | Resolución |
|---|---|
| Interfaz de usuario | **REST API futura** (Spring Boot o Quarkus, a definir). El `core` nunca debe depender de la capa web. |
| Certificado institucional | **`root_ca.p12`** en `documentos/certificados/root_ca.p12` — firma el PDF. Usa `iText 7 PdfSigner`. |
| Certificado del autor | **Archivo `.p12` del autor** — firma el expediente JSON en Fase 3. |
| QR Code | **Opción A — ID interno** (`CERT-000001`). Texto plano, sin URL. Auto-contenido y verificable offline. |
| PDF | **iText 7** — soporta `PdfSigner` nativo para firma digital real del PDF. Template basado en el HTML Thymeleaf definido. |
| JSON | **Gson** — ligero, sin anotaciones obligatorias. |
| Base de datos | **PostgreSQL** — motor elegido, pero el driver y adaptador concreto se añaden al elegir el framework REST (Spring Boot o Quarkus). Por ahora solo existe el puerto `ExpedienteRepositoryPort`. |

---

## Principio Guía: Arquitectura Hexagonal Pura

```
          ┌──────────────────────────────────────────┐
          │              CORE (dominio)               │
          │  model / state / observer / service /     │
          │  ports/in / ports/out / rules             │
          │  ← Cero dependencias de Spring/Quarkus →  │
          └──────────────┬───────────────────────────┘
                         │ implementa puertos
          ┌──────────────▼───────────────────────────┐
          │          INFRASTRUCTURE                   │
          │  adapters/pdf / firma / esteganografia /  │
          │  json / hash / qr                        │
          └──────────────┬───────────────────────────┘
                         │ será reemplazado por
          ┌──────────────▼───────────────────────────┐
          │   ENTRYPOINT  (hoy: Main.java CLI)        │
          │   (futuro: Spring Boot / Quarkus REST)    │
          │   Cero lógica de negocio aquí             │
          └──────────────────────────────────────────┘
```

> [!IMPORTANT]
> El `Main.java` actual actúa como **adaptador de entrada CLI temporal**. Cuando se migre a REST, solo se añade un nuevo adaptador `rest/` sin tocar nada en `core/`.

---

## Propuesta de Cambios

### Parte 0 — Reestructuración de Paquetes

```
core/
 ├── model/
 │    ├── base/         ← ya existe
 │    ├── imagen/       ← ya existe
 │    ├── psd/          ← ya existe
 │    ├── validacion/   ← ya existe
 │    ├── autor/        ← NUEVO
 │    ├── obra/         ← NUEVO
 │    ├── expediente/   ← NUEVO
 │    ├── certificado/  ← NUEVO
 │    └── firma/        ← NUEVO
 ├── state/             ← NUEVO — patrón State
 ├── observer/          ← NUEVO — patrón Observer
 ├── ports/
 │    ├── in/           ← NUEVO — use cases
 │    └── out/          ← ya existe + nuevos puertos
 ├── service/           ← ya existe + nuevos servicios
 └── rules/             ← ya existe

infrastructure/
 └── adapters/
      ├── processors/   ← ya existe
      ├── extractors/   ← ya existe
      ├── firma/        ← NUEVO — P12 autor + root_ca
      ├── pdf/          ← NUEVO — iText 7
      ├── esteganografia/ ← NUEVO — PNG chunk / JPEG APP11
      ├── json/         ← NUEVO — Gson
      ├── hash/         ← NUEVO — SHA256
      └── qr/           ← NUEVO — ZXing

entrypoint/             ← NUEVO (temporal)
 └── cli/
      └── ProcesoCertificacionRunner.java
```

---

### Parte 1 — Patrón State

#### [NEW] `core/state/EstadoProceso.java`
Interfaz central:
```java
public interface EstadoProceso {
    void ejecutar(ContextoProceso ctx);
    void avanzar(ContextoProceso ctx);
    void retroceder(ContextoProceso ctx);
    boolean validar(ContextoProceso ctx);
    String getNombre();
}
```

#### [NEW] `core/state/ContextoProceso.java`
Objeto compartido entre todos los estados. Acumula:
- `ArchivoPSD`, `ArchivoImagen` → entrada Fase 1
- `VeredictoFinal veredictoPSD`, `VeredictoFinal veredictoImagen`
- `String sha256PSD`, `String sha256Imagen`, `String pHash`
- `Autor autor`, `Obra obra`, `Declaraciones declaraciones`
- `Expediente expediente`, `String expedienteJsonFirmado`, `String firmaAutorBase64`
- `Certificado certificado`
- `byte[] pdfCertificado`, `byte[] pngCertificada`
- `EstadoProceso estadoActual`

#### [NEW] 4 implementaciones de `EstadoProceso`
| Clase | Transición |
|---|---|
| `AnalisisForenseState` | → `DatosObraState` si APROBADO |
| `DatosObraState` | → `FirmaAutorState` si declaraciones aceptadas |
| `FirmaAutorState` | → `CertificacionState` si firma válida |
| `CertificacionState` | → (terminal) genera PDF + PNG |

---

### Parte 2 — Patrón Observer

#### [NEW] `core/observer/EventoSistema.java`
Interfaz marker base para todos los eventos del sistema.

#### [NEW] Eventos concretos `core/observer/eventos/`
| Evento | Se dispara cuando |
|---|---|
| `EventoAnalisisIniciado` | Se cargan PSD + imagen en Fase 1 |
| `EventoAnalisisCompletado` | Fase 1 termina con resultado APROBADO |
| `EventoFirmaRealizada` | El autor firma el expediente en Fase 3 |
| `EventoCertificadoEmitido` | Fase 4 genera el PDF firmado |

#### [NEW] `core/observer/EventPublisher.java`
Registro y notificación de listeners. Genérico por tipo de evento.

#### [NEW] `core/observer/EventListener.java`
Interfaz `void onEvento(EventoSistema evento)`.

#### [NEW] Observers concretos `core/observer/listeners/`
| Observer | Suscrito a | Acción |
|---|---|---|
| `HashGeneratorListener` | `EventoAnalisisIniciado` | Calcula SHA256 PSD + imagen vía port |
| `MetadataExtractorListener` | `EventoAnalisisIniciado` | Extrae metadatos (reutiliza lógica existente) |
| `CapasExtractorListener` | `EventoAnalisisIniciado` | Extrae capas PSD (reutiliza lógica existente) |
| `ExpedienteListener` | `EventoFirmaRealizada` | Persiste expediente-firmado.json |
| `AuditoriaListener` | Todos los eventos | Escribe log de auditoría |
| `PDFGeneratorListener` | `EventoFirmaRealizada` | Dispara la generación del PDF |

---

### Parte 3 — Nuevos Modelos de Dominio

#### [NEW] `core/model/autor/Autor.java`
```java
// nombres, apellidos, cedula, correo, seudonimo (opcional, @Nullable)
```

#### [NEW] `core/model/obra/Obra.java`
```java
// titulo, descripcion, software, fechaCreacion (LocalDate), categoria (CategoriaObra)
```

#### [NEW] `core/model/obra/CategoriaObra.java` (enum)
```java
ILUSTRACION, DISENO_GRAFICO, ARTE_CONCEPTUAL, FOTOMANIPULACION, OTRO
```

#### [NEW] `core/model/obra/Declaraciones.java`
```java
// titularDerechos: boolean
// entiendeCertificacionTecnica: boolean
// aceptaTerminos: boolean
// isCompletas(): los 3 deben ser true
```

#### [NEW] `core/model/expediente/Expediente.java`

Estructura JSON exacta que se persiste y firma:
```json
{
  "idExpediente": "EXP-2026-000001",
  "fechaRegistro": "2026-06-21T18:00:00Z",

  "autor": {
    "nombres": "",
    "apellidos": "",
    "cedula": "",
    "correo": "",
    "seudonimo": ""
  },

  "obra": {
    "titulo": "",
    "descripcion": "",
    "software": "",
    "categoria": "",
    "fechaCreacion": ""
  },

  "analisis": {
    "resultado": "",
    "capasPSD": 0,
    "metadatosDetectados": true
  },

  "hashes": {
    "sha256PSD": "",
    "sha256Imagen": "",
    "pHash": ""
  }
}
```

#### [NEW] `core/model/expediente/AnalisisResumen.java`
```java
// resultado (String: "APROBADO"/"RECHAZADO"), capasPSD (int), metadatosDetectados (boolean)
```

#### [NEW] `core/model/expediente/HashesEvidencia.java`
```java
// sha256PSD, sha256Imagen, pHash
```

#### [NEW] `core/model/firma/FirmaAutor.java`
```java
// hashExpediente, firmaBase64, algoritmo ("SHA256withRSA"), fechaFirma
```

#### [NEW] `core/model/certificado/Certificado.java`
```java
// idCertificado ("CERT-YYYY-NNNNNN"), idExpediente, fechaEmision,
// hashExpedienteFirmado, qrContenido ("CERT-NNNNNN"), qrBase64
```

---

### Parte 4 — Puertos Hexagonales

#### [NEW] Puertos de entrada `core/ports/in/`
```java
IniciarAnalisisUseCase       // execute(File psd, File imagen)
RegistrarDatosObraUseCase    // execute(Autor, Obra, Declaraciones)
FirmarExpedienteUseCase      // execute(File p12Autor, String contrasena)
EmitirCertificadoUseCase     // execute() → ResultadoCertificacion
```

#### [NEW] Puertos de salida `core/ports/out/`
```java
// Ya existe:
ArchivoProcessorPort<T>

// Nuevos:
GeneradorHashPort            // calcularSHA256(byte[]) → String
FirmadorExpedientePort       // firmar(String json, File p12, String pass) → FirmaAutor
FirmadorPDFPort              // firmarPDF(byte[] pdf, File rootCaP12, String pass) → byte[]
GeneradorPDFPort             // generar(Certificado, Expediente, String jsonFirmado) → byte[]
GeneradorQRPort              // generar(String contenido) → byte[]
EsteganografiaPort           // inyectar(byte[] imagen, String json) → byte[]
ExpedienteRepositoryPort     // guardar(Expediente), buscarPorId(String) → Optional<Expediente>
```

---

### Parte 5 — Servicios de Dominio

#### [NEW] `core/service/ExpedienteService.java`
Construye `Expediente` a partir del `ContextoProceso`. Genera `idExpediente` (`EXP-YYYY-NNNNNN`). Delega la serialización JSON a `ExpedienteRepositoryPort`.

#### [NEW] `core/service/FirmaAutorService.java`
Orquesta la firma del expediente usando `FirmadorExpedientePort`. Valida que el P12:
- existe en disco
- contraseña es correcta (`KeyStore.load()`)
- no está expirado (`X509Certificate.checkValidity()`)
- es legible (alias recuperable)

#### [NEW] `core/service/CertificadoService.java`
- Genera `idCertificado` (`CERT-YYYY-NNNNNN`)
- Genera el QR con contenido `CERT-NNNNNN` via `GeneradorQRPort`
- Construye el objeto `Certificado`

#### [NEW] `core/service/HashSHA256Service.java`
Calcula SHA256 de `File` o `byte[]` via `GeneradorHashPort`.

---

### Parte 6 — Adaptadores de Infraestructura

#### [NEW] `infrastructure/adapters/hash/SHA256Adapter.java`
Implementa `GeneradorHashPort` con `MessageDigest.getInstance("SHA-256")`. Sin dependencias externas.

#### [NEW] `infrastructure/adapters/qr/QRGeneratorAdapter.java`
Implementa `GeneradorQRPort` con **ZXing**.
- Contenido del QR: `CERT-NNNNNN` (Opción A — ID interno)
- Retorna `byte[]` PNG del QR para embeber en el PDF

#### [NEW] `infrastructure/adapters/firma/FirmadorP12Adapter.java`
Implementa `FirmadorExpedientePort`. Usa el `.p12` del **autor** (proporcionado en Fase 3):
```
KeyStore.getInstance("PKCS12")
    ↓
keyStore.load(FileInputStream, password)
    ↓
PrivateKey + X509Certificate
    ↓
Signature.getInstance("SHA256withRSA")
    ↓
signature.initSign(privateKey)
signature.update(expedienteJson.getBytes(UTF_8))
    ↓
firmaBase64 = Base64.encode(signature.sign())
```

#### [NEW] `infrastructure/adapters/firma/FirmadorPDFAdapter.java`
Implementa `FirmadorPDFPort` usando **iText 7 `PdfSigner`**. Usa el certificado institucional:

```
Ruta fija: documentos/certificados/root_ca.p12
    ↓
KeyStore.getInstance("PKCS12").load()
    ↓
Extrae PrivateKey + Certificate[]
    ↓
PdfSigner.signDetached(...)
    ↓
certificado_firmado.pdf
```

Flujo dentro del adaptador:
1. `KeyStore.getInstance("PKCS12")` → carga `root_ca.p12` desde `documentos/certificados/root_ca.p12`
2. `IExternalSignature` implementado con la `PrivateKey` extraída del CA
3. `PdfSigner` crea campo de firma digital real (CMS/CAdES)
4. `signDetached(digest, signature, chain, null, null, null, 0, CryptoStandard.CMS)`

#### [NEW] `infrastructure/adapters/pdf/GeneradorPDFAdapter.java`
Implementa `GeneradorPDFPort` con **iText 7**. Replica el template HTML del certificado. Mapeo de variables Thymeleaf → campos del modelo:

| Variable Thymeleaf | Fuente en el modelo |
|---|---|
| `${idCertificado}` | `Certificado.idCertificado` |
| `${fechaEmision}` | `Certificado.fechaEmision` |
| `${autorObra}` | `Expediente.autor.nombres + apellidos` |
| `${seudonimo}` | `Expediente.autor.seudonimo` (opcional) |
| `${idInstitucional}` | `Expediente.autor.cedula` |
| `${tituloObra}` | `Expediente.obra.titulo` |
| `${fechaCreacion}` | `Expediente.obra.fechaCreacion` |
| `${software}` | `Expediente.obra.software` |
| `${hardware}` | Campo adicional de `Obra` (a definir) |
| `${detallesTecnicos}` | Generado: categoría + DPI + modo color |
| `${dimensiones}` | Extraído del análisis forense Fase 1 |
| `${sha256}` | `Expediente.hashes.sha256Imagen` |
| `${phash}` | `Expediente.hashes.pHash` |
| `${estado}` | `Expediente.analisis.resultado` |
| `${obraBase64}` | Imagen PNG certificada en Base64 |
| `${qrBase64}` | `Certificado.qrBase64` (QR del ID interno) |

El adaptador:
- Construye el PDF página A4 landscape programáticamente con iText 7 (sin motor de plantillas)
- Embebe el `expediente-firmado.json` como archivo adjunto (`PdfFileSpec`)
- Agrega metadata XMP: `idCertificado`, `idExpediente`, `hashExpediente`
- Produce un `byte[]` PDF sin firmar → lo entrega al `FirmadorPDFAdapter`

#### [NEW] `infrastructure/adapters/esteganografia/EsteganografiaPNGAdapter.java`
Implementa `EsteganografiaPort` para PNG:
```
Lee bytes del PNG
    ↓
Localiza chunk IEND (últimos 12 bytes)
    ↓
Inserta chunk tEXt personalizado ANTES de IEND
    ↓
Escribe nuevo PNG con el JSON {"id":"CERT-...","hash":"..."}
```

#### [NEW] `infrastructure/adapters/esteganografia/EsteganografiaJPEGAdapter.java`
Implementa `EsteganografiaPort` para JPEG/JPG:
- Inserta segmento `APP11` (`FF EB`) con el JSON de certificación

#### [NEW] `infrastructure/adapters/json/ExpedienteJsonAdapter.java`
Implementa `ExpedienteRepositoryPort` de forma temporal en archivo JSON con **Gson**.
Cuando se elija el framework REST, este adaptador se reemplaza por uno PostgreSQL sin tocar el `core`.

---

### Parte 7 — Dependencias a agregar en `build.gradle.kts`

| Librería | Versión | Para |
|---|---|---|
| `com.google.code.gson:gson` | `2.11.0` | Serialización JSON del expediente |
| `com.google.zxing:core` | `3.5.3` | Generación QR (Opción A: ID interno) |
| `com.google.zxing:javase` | `3.5.3` | Renderizado QR como PNG |
| `com.itextpdf:itext7-core` | `7.2.6` | Generación PDF + `PdfSigner` digital |
| `com.itextpdf:sign` | `7.2.6` | Módulo de firma digital iText 7 |
| `org.bouncycastle:bcpkix-jdk15on` | `1.70` | Proveedor criptográfico para iText 7 PdfSigner |

> [!NOTE]
> iText 7 requiere **BouncyCastle** como proveedor criptográfico para `PdfSigner`. Sin él, la firma del PDF no funcionará.

> [!IMPORTANT]
> El driver de **PostgreSQL y cualquier dependencia de persistencia** se añadirá cuando se elija el framework REST (Spring Boot o Quarkus), en su propio módulo. **No se añade aquí.**

---

### Parte 8 — Entrypoint CLI temporal

#### [NEW] `entrypoint/cli/ProcesoCertificacionRunner.java`
Reemplaza la lógica de `Main.java`. Actúa como **adaptador de entrada CLI**:
- Inicializa todos los adaptadores de infraestructura
- Los inyecta en los servicios via constructores (no Spring DI por ahora)
- Construye el `ContextoProceso` y arranca la máquina de estados
- Cuando se migre a REST → este archivo se reemplaza por `@RestController` sin tocar el `core`

---

## Flujo Completo Implementado

```
[ProcesoCertificacionRunner / futuro REST Controller]
      │
      ▼
ContextoProceso ──── estadoActual: AnalisisForenseState
      │
      ├─► [FASE 1] AnalisisForenseState.ejecutar()
      │         │
      │         ├── EventPublisher → EventoAnalisisIniciado
      │         │       ├── HashGeneratorListener   → sha256PSD, sha256Imagen
      │         │       ├── MetadataExtractorListener
      │         │       └── CapasExtractorListener
      │         ├── ValidadorGenericoService<PSD>   → VeredictoFinal
      │         ├── ValidadorGenericoService<Imagen>→ VeredictoFinal
      │         ├── CalculadorPHash                 → pHash
      │         └── [APROBADO] → avanzar() → DatosObraState
      │
      ├─► [FASE 2] DatosObraState.ejecutar()
      │         ├── Recibe Autor + Obra + Declaraciones
      │         ├── Declaraciones.isCompletas() == true
      │         └── avanzar() → FirmaAutorState
      │
      ├─► [FASE 3] FirmaAutorState.ejecutar()
      │         ├── ExpedienteService.construir()   → Expediente JSON
      │         ├── FirmaAutorService.validar(p12Autor, pass)
      │         ├── FirmaAutorService.firmar(json, p12Autor, pass) → FirmaAutor
      │         └── EventPublisher → EventoFirmaRealizada
      │                 ├── ExpedienteListener  → guarda expediente-firmado.json
      │                 ├── AuditoriaListener   → log
      │                 └── PDFGeneratorListener → dispara Fase 4
      │
      └─► [FASE 4] CertificacionState.ejecutar()
                ├── CertificadoService.generarId()   → CERT-2026-000001
                ├── QRGeneratorAdapter.generar("CERT-000001") → qrBase64
                ├── GeneradorPDFAdapter.generar()    → byte[] PDF sin firmar
                │       ├── certificado visual completo
                │       ├── adjunto: expediente-firmado.json
                │       └── metadata XMP
                ├── FirmadorPDFAdapter.firmar(pdf, root_ca.p12, pass)
                │       ├── Carga root_ca.p12 → PrivateKey + cert chain
                │       ├── PdfSigner.signDetached(...)
                │       └── → CERT-2026-000001.pdf (firmado digitalmente)
                ├── EsteganografiaAdapter.inyectar(imagen, json) → obra-certificada.png
                └── EventPublisher → EventoCertificadoEmitido
                        └── AuditoriaListener
```

---

## Archivos de Salida

| Archivo | Contenido |
|---|---|
| `CERT-2026-000001.pdf` | Certificado visual + `expediente-firmado.json` adjunto + firma digital `root_ca.p12` |
| `obra-certificada.png` | Imagen original con chunk `tEXt` inyectado: `{"id":"CERT-2026-000001","hash":"..."}` |

---

## Plan de Verificación

### Pruebas Unitarias (JUnit 5)
- `EstadoProcesoTest` — Transiciones válidas e inválidas entre los 4 estados
- `ContextoProcesoTest` — Acumulación correcta de datos entre fases
- `ExpedienteServiceTest` — JSON generado coincide con el schema definido
- `FirmaAutorServiceTest` — P12 válido, contraseña incorrecta, certificado expirado
- `FirmadorPDFAdapterTest` — PDF firmado tiene campo de firma válido
- `HashSHA256ServiceTest` — Hash determinista para el mismo input
- `EsteganografiaPNGAdapterTest` — Inyección y lectura del chunk `tEXt`
- `QRGeneratorAdapterTest` — QR contiene exactamente `CERT-NNNNNN`
- `CertificadoServiceTest` — Formato `CERT-YYYY-NNNNNN` correcto

### Verificación Manual
1. Ejecutar el flujo end-to-end con un PSD + PNG de prueba reales
2. Abrir el PDF en Adobe Reader → verificar firma digital del CA en el panel de firmas
3. Usar `pngcheck` o un editor hex para confirmar el chunk `tEXt` inyectado
4. Inspeccionar metadata XMP del PDF con Exiftool
5. Escanear el QR → debe mostrar `CERT-000001` exactamente
