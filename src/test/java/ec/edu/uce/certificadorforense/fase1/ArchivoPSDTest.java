package ec.edu.uce.certificadorforense.fase1;

import ec.edu.uce.certificadorforense.core.model.psd.ArchivoPSD;
import ec.edu.uce.certificadorforense.core.model.validacion.ResultadoValidacion;
import ec.edu.uce.certificadorforense.core.model.validacion.VeredictoFinal;
import ec.edu.uce.certificadorforense.core.ports.out.ArchivoProcessorPort;
import ec.edu.uce.certificadorforense.core.rules.psd.ReglaComplejidadDiseno;
import ec.edu.uce.certificadorforense.core.rules.psd.ReglaFormatoPsd;
import ec.edu.uce.certificadorforense.core.rules.psd.ReglaImagenPegada;
import ec.edu.uce.certificadorforense.core.rules.psd.ReglaResolucionProfesional;
import ec.edu.uce.certificadorforense.core.service.ArchivoProcessorFactory;
import ec.edu.uce.certificadorforense.core.service.ValidadorGenericoService;
import ec.edu.uce.certificadorforense.infrastructure.adapters.processors.ArchivoPSDProcessor;

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
