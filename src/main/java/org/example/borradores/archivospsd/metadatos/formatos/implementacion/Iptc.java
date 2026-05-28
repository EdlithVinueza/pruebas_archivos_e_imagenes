package org.example.borradores.archivospsd.metadatos.formatos.implementacion;

import com.drew.metadata.Metadata;
import com.drew.metadata.iptc.IptcDirectory;
import org.example.borradores.archivospsd.metadatos.formatos.interfaz.IMetadataExtractor;
import org.example.borradores.archivospsd.metadatos.modelo.MetadatosPSD.MetadatosPSDBuilder;

public class Iptc implements IMetadataExtractor {

    @Override
    public void extract(Metadata metadata, MetadatosPSDBuilder builder) {
        IptcDirectory directory = metadata.getFirstDirectoryOfType(IptcDirectory.class);
        if (directory == null) {
            return;
        }

        String autor = primerValorNoVacio(
                directory.getString(IptcDirectory.TAG_BY_LINE),
                directory.getString(IptcDirectory.TAG_BY_LINE_TITLE)
        );
        if (autor != null && !autor.isBlank()) {
            builder.autor(autor);
        }

        String software = directory.getString(IptcDirectory.TAG_ORIGINATING_PROGRAM);
        if (software != null && !software.isBlank()) {
            builder.software(software);
        }

        String fecha = combinarFechaHora(
                primerValorNoVacio(directory.getString(IptcDirectory.TAG_DIGITAL_DATE_CREATED),
                        directory.getString(IptcDirectory.TAG_DATE_CREATED)),
                primerValorNoVacio(directory.getString(IptcDirectory.TAG_DIGITAL_TIME_CREATED),
                        directory.getString(IptcDirectory.TAG_TIME_CREATED))
        );
        if (fecha != null && !fecha.isBlank()) {
            builder.fechaCreacion(fecha);
        }
    }

    private static String combinarFechaHora(String fecha, String hora) {
        if (fecha == null || fecha.isBlank()) {
            return hora;
        }
        if (hora == null || hora.isBlank()) {
            return fecha;
        }
        return fecha + " " + hora;
    }

    private static String primerValorNoVacio(String primero, String segundo) {
        if (primero != null && !primero.isBlank()) {
            return primero;
        }
        return segundo;
    }
}