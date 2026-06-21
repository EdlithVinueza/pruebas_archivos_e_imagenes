package analisis.core.ports.out;

import analisis.core.model.base.ArchivoBase;
import java.io.File;

public interface ArchivoProcessorPort<T extends ArchivoBase> {
    T procesar(File file);
    boolean soporta(File file);
}
