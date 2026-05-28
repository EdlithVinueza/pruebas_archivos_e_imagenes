package org.example.borradores.simulitud.service;

import org.example.borradores.simulitud.algoritmos.CalculadorMSE;
import org.example.borradores.simulitud.algoritmos.CalculadorPHash;
import org.example.borradores.simulitud.algoritmos.CalculadorPSNR;

import java.awt.image.BufferedImage;

public class ServicioComparador{

    private final CalculadorMSE calculadorMSE = new CalculadorMSE();
    private final CalculadorPSNR calculadorPSNR = new CalculadorPSNR();
    private final CalculadorPHash calculadorPHash = new CalculadorPHash();

    public void imprimirAnalisisCompleto(BufferedImage img1, BufferedImage img2) {
        // Ejecutar MSE y PSNR
        double mse = calculadorMSE.calcular(img1, img2);
        double psnr = calculadorPSNR.calcular(mse);

        // Ejecutar p-Hash
        String h1 = calculadorPHash.generarHash(img1);
        String h2 = calculadorPHash.generarHash(img2);
        double similitud = calculadorPHash.compararSimilitud(h1, h2);

        System.out.println("====== REPORTE DE CALIDAD DE IMAGEN ======");
        System.out.printf("Error Cuadrático Medio (MSE): %.6f%n", mse);
        System.out.printf("Relación Señal-Ruido (PSNR):  %.2f dB%n", psnr);
        System.out.printf("Similitud Visual (p-Hash):    %.2f%%%n", similitud);
        System.out.println("==========================================");

        if (mse == 0) {
            System.out.println("Resultado: Las imágenes son idénticas bit a bit.");
        } else if (psnr > 40) {
            System.out.println("Resultado: Calidad profesional. Diferencia invisible.");
        }
    }
}