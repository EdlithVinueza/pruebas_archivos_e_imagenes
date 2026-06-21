package analisis.core.rules;

import analisis.core.model.validacion.ResultadoValidacion;

public interface IReglaValidacion<T> {
    ResultadoValidacion validar(T objeto);
    boolean esCritica();
}
