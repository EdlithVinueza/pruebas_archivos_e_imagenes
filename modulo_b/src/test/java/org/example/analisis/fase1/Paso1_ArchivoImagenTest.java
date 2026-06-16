package org.example.analisis.fase1;

import org.example.analisis.core.model.base.ArchivoBase;
import org.example.analisis.core.model.imagen.ArchivoImagen;
import org.example.analisis.core.model.validacion.ResultadoValidacion;
import org.example.analisis.core.model.validacion.VeredictoFinal;
import org.example.analisis.core.ports.out.ArchivoProcessorPort;
import org.example.analisis.core.service.ArchivoProcessorFactory;
import org.example.analisis.core.service.ValidadorGenericoService;
import org.example.analisis.infrastructure.adapters.processors.ArchivoImagenProcessor;
import org.example.analisis.core.rules.imagen.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class Paso1_ArchivoImagenTest {

    private static final String BASE_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/";
    private ArchivoProcessorFactory factory;
    private ValidadorGenericoService<ArchivoImagen> validadorImagen;

    @BeforeEach
    public void setUp() {
        // Inicializar fábrica con el procesador de imágenes
        factory = new ArchivoProcessorFactory(Collections.singletonList(new ArchivoImagenProcessor()));

        // Inicializar validador con reglas de imagen
        validadorImagen = new ValidadorGenericoService<>();
        validadorImagen.registrarRegla(new ReglaFirmaEstructural());
        validadorImagen.registrarRegla(new ReglaCoherenciaDpi());
        validadorImagen.registrarRegla(new ReglaAnalisisOrigen());
    }

    @Test
    public void testProcesamientoYValidacionReglasImagen() {
        File filePNG = new File(BASE_PATH + "imagenes/girasol-original.png");
        Assumptions.assumeTrue(filePNG.exists(), "La imagen PNG no existe: " + filePNG.getAbsolutePath());

        ArchivoProcessorPort<ArchivoImagen> processor = (ArchivoProcessorPort<ArchivoImagen>) factory.getProcessor(filePNG);
        ArchivoImagen img = processor.procesar(filePNG);
        assertNotNull(img, "El objeto procesado de la imagen no debe ser nulo");

        // Validar individualmente
        VeredictoFinal veredicto = validadorImagen.validar(img);
        assertNotNull(veredicto, "El veredicto final no debe ser nulo");
        System.out.println("Veredicto Imagen: " + veredicto.isEsRechazado() + " | Razón: " + veredicto.getRazonRechazo());

        // Testear cada regla de manera aislada
        ReglaFirmaEstructural reglaFirma = new ReglaFirmaEstructural();
        ResultadoValidacion resFirma = reglaFirma.validar(img);
        assertNotNull(resFirma, "Resultado de regla de firma no debe ser nulo");
        assertTrue(resFirma.isEsValido(), "La firma estructural de la imagen original debería ser válida");

        ReglaCoherenciaDpi reglaDpi = new ReglaCoherenciaDpi();
        ResultadoValidacion resDpi = reglaDpi.validar(img);
        assertNotNull(resDpi, "Resultado de regla DPI no debe ser nulo");
    }
}
