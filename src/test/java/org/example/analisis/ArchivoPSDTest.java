package org.example.analisis;

import org.example.analisis.core.model.base.ArchivoBase;
import org.example.analisis.core.model.psd.ArchivoPSD;
import org.example.analisis.core.model.validacion.ResultadoValidacion;
import org.example.analisis.core.model.validacion.VeredictoFinal;
import org.example.analisis.core.ports.out.ArchivoProcessorPort;
import org.example.analisis.core.service.ArchivoProcessorFactory;
import org.example.analisis.core.service.ValidadorGenericoService;
import org.example.analisis.infrastructure.adapters.processors.ArchivoPSDProcessor;
import org.example.analisis.core.rules.psd.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ArchivoPSDTest {

    private static final String BASE_PATH = "C:/Users/Edlith Vinueza/Documents/UCE 26-26/Tesis/Archivos de Prueba/";
    private ArchivoProcessorFactory factory;
    private ValidadorGenericoService<ArchivoPSD> validadorPSD;

    @BeforeEach
    public void setUp() {
        // Inicializar fábrica con el procesador PSD
        factory = new ArchivoProcessorFactory(Collections.singletonList(new ArchivoPSDProcessor()));

        // Inicializar validador con reglas PSD
        validadorPSD = new ValidadorGenericoService<>();
        validadorPSD.registrarRegla(new ReglaFormatoPsd());
        validadorPSD.registrarRegla(new ReglaResolucionProfesional());
        validadorPSD.registrarRegla(new ReglaImagenPegada());
        validadorPSD.registrarRegla(new ReglaComplejidadDiseno());
    }

    @Test
    public void testProcesamientoYValidacionReglasPSD() {
        File filePSD = new File(BASE_PATH + "psd/girasol-original-una-capa.psd");
        Assumptions.assumeTrue(filePSD.exists(), "El archivo de prueba PSD no existe: " + filePSD.getAbsolutePath());

        ArchivoProcessorPort<ArchivoPSD> processor = (ArchivoProcessorPort<ArchivoPSD>) factory.getProcessor(filePSD);
        ArchivoPSD psd = processor.procesar(filePSD);
        assertNotNull(psd, "El objeto procesado de PSD no debe ser nulo");

        // Validar completo
        VeredictoFinal veredicto = validadorPSD.validar(psd);
        assertNotNull(veredicto, "El veredicto final de PSD no debe ser nulo");
        System.out.println("Veredicto PSD: " + veredicto.isEsRechazado() + " | Razón: " + veredicto.getRazonRechazo());

        // Testear reglas de PSD de forma individual
        ReglaFormatoPsd reglaFormato = new ReglaFormatoPsd();
        ResultadoValidacion resFormato = reglaFormato.validar(psd);
        assertTrue(resFormato.isEsValido(), "Debería ser un formato PSD válido");

        ReglaComplejidadDiseno reglaComplejidad = new ReglaComplejidadDiseno();
        ResultadoValidacion resComplejidad = reglaComplejidad.validar(psd);
        assertNotNull(resComplejidad);
    }
}
