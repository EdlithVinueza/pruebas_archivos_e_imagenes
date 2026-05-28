package org.example.borradores.archivosimagen.estructura.modelo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstructuraImagen {
    private String formatoReal;      // PNG o JPEG
    private String firmaHex;         // Los primeros bytes en texto
    private int dpiX;
    private int dpiY;
    private boolean tieneResolucionFisica;

    @Override
    public String toString() {
        return String.format(
                "\n[ANÁLISIS ESTRUCTURAL BINARIO]\n" +
                        "• Firma (Magic):    %s\n" +
                        "• Formato Real:     %s\n" +
                        "• Densidad Física:  %d x %d DPI",
                firmaHex, formatoReal, dpiX, dpiY
        );
    }
}
