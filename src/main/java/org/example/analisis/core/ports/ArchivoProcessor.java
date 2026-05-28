package org.example.analisis.core.ports;

import org.example.analisis.core.model.base.ArchivoBase;
import java.io.File;

public interface ArchivoProcessor<T extends ArchivoBase> {
    T procesar(File file);
    boolean soporta(File file);
}
