package org.example.analisis.simulitud.algoritmos;

public class  CalculadorPSNR {

    public double calcular(double mse) {
        if (mse == 0) {
            return 100.0; // Valor máximo representativo de imágenes idénticas
        }
        // Fórmula estándar: 10 * log10 ( MAX_I^2 / MSE )
        return 10 * Math.log10((255 * 255) / mse);
    }
}