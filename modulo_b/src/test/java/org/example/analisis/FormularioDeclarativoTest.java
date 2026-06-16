package org.example.analisis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.analisis.core.model.forense.PayloadForense;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormularioDeclarativoTest {

    @Test
    public void debeEmpaquetarDatosDelFormularioEnJsonCorrectamente() throws Exception {
        System.out.println("=== TEST: Formulario Web Declarativo ===");

        // Simulación de los datos ingresados por el artista en el formulario frontend
        String inputTitulo = "Reflejos de la Memoria";
        String inputFecha = "2026-05-10T15:30:00Z";
        String inputSoftware = "FireAlpaca v2.11";
        String inputHardware = "Huion Inspiroy H610PRO v2";
        String inputDetalles = "Uso de capas de opacidad múltiple y pinceles simétricos";

        // Ensamblado del Payload v1.1
        // TODO: En producción, los datos del Autor (nombre, id) deben ser extraídos del certificado .p12
        // del artista durante la firma, en lugar de ser mockeados aquí.
        PayloadForense.Autor autor = new PayloadForense.Autor("Edlith Vinueza", "UCE-77765");
        
        PayloadForense.Obra obra = new PayloadForense.Obra(inputTitulo, inputFecha, inputSoftware, inputHardware, inputDetalles);
        PayloadForense.AnalisisForenseDigital analisis = new PayloadForense.AnalisisForenseDigital("e3b0c442...", "a7c3c3e1...", "4000x3000 px");
        PayloadForense.DatosCertificado cert = new PayloadForense.DatosCertificado("VA-001", "VerisArt", "ALCOTEL", "2026", "VALIDO", "RSA", "hash");
        
        // TODO: En producción, el valor de la firma y el algoritmo deben obtenerse matemáticamente 
        // firmando el hash SHA-256 usando la clave privada contenida en el .p12.
        PayloadForense.FirmaDigital firma = new PayloadForense.FirmaDigital("SHA256", "firma_encriptada_simulada");

        PayloadForense payload = new PayloadForense("1.1", autor, obra, analisis, cert, firma);

        // Serializar a JSON
        ObjectMapper mapper = new ObjectMapper();
        String jsonResultante = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload);

        System.out.println("JSON Generado:\n" + jsonResultante);

        // Verificaciones
        assertTrue(jsonResultante.contains("\"metadata_version\" : \"1.1\""));
        assertTrue(jsonResultante.contains("\"" + inputTitulo + "\""));
        assertTrue(jsonResultante.contains("\"" + inputSoftware + "\""));
        assertTrue(jsonResultante.contains("\"" + inputHardware + "\""));
        assertTrue(jsonResultante.contains("\"" + inputDetalles + "\""));
        
        System.out.println("Éxito: Todos los datos del formulario fueron mapeados al esquema JSON v1.1 correctamente.");
    }
}
