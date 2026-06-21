package analisis.core.model.psd;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import analisis.core.model.base.ArchivoBase;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ArchivoPSD extends ArchivoBase {
    private MetadatosPSD metadatos;
    private List<EstructuraCapaPSD> capas; // Capas físicas extraídas

    @Override
    public MetadatosPSD getMetadatos() { return metadatos; }
}
