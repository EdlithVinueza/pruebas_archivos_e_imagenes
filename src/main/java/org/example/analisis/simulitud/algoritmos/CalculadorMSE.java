package org.example.analisis.simulitud.algoritmos;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class CalculadorMSE {

    public double calcular(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            throw new IllegalArgumentException("Las dimensiones de las imágenes deben coincidir.");
        }

        double sumaCuadrados = 0;
        int ancho = img1.getWidth();
        int alto = img1.getHeight();

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                Color pixel1 = new Color(img1.getRGB(x, y));
                Color pixel2 = new Color(img2.getRGB(x, y));

                sumaCuadrados += Math.pow(pixel1.getRed() - pixel2.getRed(), 2);
                sumaCuadrados += Math.pow(pixel1.getGreen() - pixel2.getGreen(), 2);
                sumaCuadrados += Math.pow(pixel1.getBlue() - pixel2.getBlue(), 2);
            }
        }

        // Dividimos por el número total de muestras (píxeles * 3 canales R,G,B)
        return sumaCuadrados / (ancho * alto * 3.0);
    }
}
