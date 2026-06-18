package org.example.analisis.core.model.forense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayloadForense {

    @JsonProperty("metadata_version")
    private String metadataVersion = "1.1";

    @JsonProperty("autor")
    private Autor autor;

    @JsonProperty("obra")
    private Obra obra;

    @JsonProperty("analisis_forense_digital")
    private AnalisisForenseDigital analisisForenseDigital;

    @JsonProperty("datos_del_certificado")
    private DatosCertificado datosDelCertificado;

    @JsonProperty("firma_digital")
    private FirmaDigital firmaDigital;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Autor {
        @JsonProperty("nombre")
        private String nombre;

        @JsonProperty("seudonimo")
        private String seudonimo;

        @JsonProperty("id_institucional")
        private String idInstitucional;
        
        public Autor(String nombre, String idInstitucional) {
            this.nombre = nombre;
            this.seudonimo = null;
            this.idInstitucional = idInstitucional;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Obra {
        @JsonProperty("titulo")
        private String titulo;

        @JsonProperty("fecha_declarada_creacion")
        private String fechaDeclaradaCreacion;

        @JsonProperty("software_original")
        private String softwareOriginal;

        @JsonProperty("hardware_adicional")
        private String hardwareAdicional;

        @JsonProperty("detalles_tecnicos")
        private String detallesTecnicos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalisisForenseDigital {
        @JsonProperty("sha256_criptografico")
        private String sha256Criptografico;

        @JsonProperty("phash_perceptual")
        private String phashPerceptual;

        @JsonProperty("dimensiones")
        private String dimensiones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatosCertificado {
        @JsonProperty("id_certificado")
        private String idCertificado;

        @JsonProperty("entidad_emisora")
        private String entidadEmisora;

        @JsonProperty("autoridad_delegatoria")
        private String autoridadDelegatoria;

        @JsonProperty("fecha_emision")
        private String fechaEmision;

        @JsonProperty("estado_inicial")
        private String estadoInicial;

        @JsonProperty("algoritmo_llave")
        private String algoritmoLlave;

        @JsonProperty("hash_certificado_raiz")
        private String hashCertificadoRaiz;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FirmaDigital {
        @JsonProperty("algoritmo")
        private String algoritmo;

        @JsonProperty("valor_firma")
        private String valorFirma;
    }
}
