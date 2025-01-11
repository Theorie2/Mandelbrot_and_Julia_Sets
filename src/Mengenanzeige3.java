import systemlib.Point;
import systemlib.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
public class Mengenanzeige3 {
    private World w;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private ZahlenfolgenRechner zr;
    private double k;
    private boolean julia;
    private final double originalMinX;
    private final double originalMaxX;
    private final double originalMinY;
    private final double originalMaxY;
    private BufferedImage buffer;
    private Graphics2D graphics;

    public Mengenanzeige3(double k, Complex c, boolean julia) {
        initWorld();
        zr = new ZahlenfolgenRechner(k, c);
        minX = 1.75;
        maxX = -1.75;
        minY = 1.0;
        maxY = -1.0;
        this.originalMinX = minX;
        this.originalMaxX = maxX;
        this.originalMinY = minY;
        this.originalMaxY = maxY;
        this.k = k;
        this.julia = julia;
        display();
    }

    public Mengenanzeige3(double k, Complex c, boolean julia, double minX, double maxX, double minY, double maxY) {
        initWorld();
        zr = new ZahlenfolgenRechner(k, c);
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.originalMinX = minX;
        this.originalMaxX = maxX;
        this.originalMinY = minY;
        this.originalMaxY = maxY;
        this.julia = julia;
        display();
    }
    private void initWorld(){
        w = new World(1400, 800, "Mandelbrotmenge");
        w.setResizeable(false);
        w.startDrawThread();
        w.startTicking();
        buffer = new BufferedImage(w.getWidth(), w.getHeight(), BufferedImage.TYPE_INT_RGB);
        graphics = buffer.createGraphics();

        w.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    zoom(e.getX(), e.getY(), 5);
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    zoom(e.getX(), e.getY(), 0.2);
                } else if (e.getButton() == MouseEvent.BUTTON2) {
                    resetView();
                }
            }
        });
    }
    private void resetView() {
        minX = originalMinX;
        maxX = originalMaxX;
        minY = originalMinY;
        maxY = originalMaxY;
        display();
    }
    public void zoom(int centerX, int centerY, double scale) {
        // Convert screen coordinates to mathematical coordinates
        double mathX = ((centerX * (minX - maxX)) / w.getWidth()) + maxX;
        double mathY = ((centerY * (minY - maxY)) / w.getHeight()) + maxY;

        // Calculate current ranges
        double rangeX = maxX - minX;
        double rangeY = maxY - minY;

        // Calculate new ranges after zoom
        double newRangeX = rangeX / scale;
        double newRangeY = rangeY / scale;

        // Calculate new bounds keeping the math point centered
        minX = mathX - (newRangeX / 2);
        maxX = mathX + (newRangeX / 2);
        minY = mathY - (newRangeY / 2);
        maxY = mathY + (newRangeY / 2);

        display();
    }

    public void zoom(double scale) {
        minX /= scale;
        maxX /= scale;
        minY /= scale;
        maxY /= scale;
        display();
    }

    private void display() {
        if (k % 1 == 0) {
            if (julia) {
                displayJuliaMenge();
            } else {
                displayMandelbrotmenge();
            }
        } else {
            if (julia) {
                displayJuliaMengeF();
            } else {
                displayMandelbrotmengeF();
            }
        }
    }

    public void displayJuliaMenge() {
        int width = w.getWidth();
        int height = w.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double re = ((x * (minX - maxX)) / width) + maxX;
                double im = ((y * (minY - maxY)) / height) + maxY;
                Complex z = new Complex(re, im);

                int iteration = zr.zahlInJuliaMenge(z);
                Color color;
                if (iteration >= ZahlenfolgenRechner.ANZAHL_ITERATIONEN) {
                    color = Color.WHITE;
                } else {
                    color = colorCalculator(iteration);
                }
                pixels[y * width + x] = color.getRGB();
            }
        }

        buffer.setRGB(0, 0, width, height, pixels, 0, width);

        SwingUtilities.invokeLater(() -> {
            w.clear();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color pixelColor = new Color(buffer.getRGB(x, y));
                    systemlib.Point p = new Point(x, y, w);
                    p.setColor(pixelColor);
                }
            }
        });
    }

    public void displayMandelbrotmenge() {
        int width = w.getWidth();
        int height = w.getHeight();
        int[] pixels = new int[width * height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double re = ((x * (minX - (maxX))) / width) + (maxX);
                double im = ((y * (minY - (maxY))) / height) + (maxY);
                Complex z = new Complex(re, im);

                int iteration = zr.zahlInMandelbrotmenge(z);
                Color color;
                if (iteration >= ZahlenfolgenRechner.ANZAHL_ITERATIONEN) {
                    color = Color.WHITE;
                } else {
                    color = colorCalculator(iteration);
                }
                pixels[y * width + x] = color.getRGB();

            }
        }
        buffer.setRGB(0, 0, width, height, pixels, 0, width);
        SwingUtilities.invokeLater(() -> {
            w.clear();
            // Draw the entire buffer at once
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color pixelColor = new Color(buffer.getRGB(x, y));
                    systemlib.Point p = new Point(x, y, w);
                    p.setColor(pixelColor);
                }
            }
        });
    }

    public void displayJuliaMengeF() {
        int width = w.getWidth();
        int height = w.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double re = ((x * (minX - maxX)) / width) + maxX;
                double im = ((y * (minY - maxY)) / height) + maxY;
                Complex z = new Complex(re, im);

                int iteration = zr.zahlInJuliaMengeF(z);
                Color color;
                if (iteration >= ZahlenfolgenRechner.ANZAHL_ITERATIONEN) {
                    color = Color.WHITE;
                } else {
                    color = colorCalculator(iteration);
                }
                pixels[y * width + x] = color.getRGB();
            }
        }

        buffer.setRGB(0, 0, width, height, pixels, 0, width);

        SwingUtilities.invokeLater(() -> {
            w.clear();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color pixelColor = new Color(buffer.getRGB(x, y));
                    systemlib.Point p = new Point(x, y, w);
                    p.setColor(pixelColor);
                }
            }
        });
    }

    public void displayMandelbrotmengeF() {
        int width = w.getWidth();
        int height = w.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double re = ((x * (minX - maxX)) / width) + maxX;
                double im = ((y * (minY - maxY)) / height) + maxY;
                Complex z = new Complex(re, im);

                int iteration = zr.zahlInMandelbrotmengeF(z);
                Color color;
                if (iteration >= ZahlenfolgenRechner.ANZAHL_ITERATIONEN) {
                    color = Color.WHITE;
                } else {
                    color = colorCalculator(iteration);
                }
                pixels[y * width + x] = color.getRGB();
            }
        }

        buffer.setRGB(0, 0, width, height, pixels, 0, width);

        SwingUtilities.invokeLater(() -> {
            w.clear();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color pixelColor = new Color(buffer.getRGB(x, y));
                    systemlib.Point p = new Point(x, y, w);
                    p.setColor(pixelColor);
                }
            }
        });
    }

    private Color colorCalculator(int iterations) {
        /*int r = (iterations * 23) % 256;  // Rot-Wert
                    int g = (iterations * 17) % 256;  // Grün-Wert
                    int b = (iterations * 37) % 256;  // Blau-Wert
                    /*color = new Color(r, g, b);
        int r = (int) (128 + 128 * Math.sin(iterations * 0.1));  // Rot-Wert
        int g = (int) (128 + 128 * Math.sin(iterations * 0.2));  // Grün-Wert
        int b = (int) (128 + 128 * Math.sin(iterations * 0.3));  // Blau-Wert
        return new Color(r, g, b);*/

        double hue = iterations * 0.01;
        float saturation = 0.8f;
        float brightness = (iterations < ZahlenfolgenRechner.ANZAHL_ITERATIONEN) ? 1.0f : 0.0f;
        return Color.getHSBColor((float)hue, saturation, brightness);

        /* int blue = (int)(255 * Math.pow(Math.sin(iterations * 0.05), 2));
         int green = (int)(128 * Math.pow(Math.sin(iterations * 0.03), 2));
        return new Color(0, green, blue);*/

        /*double golden_ratio = 0.618033988749895;
         double hue = (iterations * golden_ratio) % 1.0;
         return Color.getHSBColor((float)hue, 0.85f, 0.95f);*/

         /*int r = (int)(255 * Math.pow(Math.sin(iterations * 0.02), 4));
         int g = (int)(255 * Math.pow(Math.sin(iterations * 0.015), 4));
         int b = (int)(255 * Math.pow(Math.sin(iterations * 0.01), 4));
         return new Color(r, g, b);*/
         /*int r = (int)(192 + 63 * Math.sin(iterations * 0.05));
         int g = (int)(128 + 63 * Math.sin(iterations * 0.06));
         int b = (int)(64 + 63 * Math.sin(iterations * 0.07));
         return new Color(r, g, b);*/
         /*double t = iterations * 0.1;
         int r = (int)(255 * Math.abs(Math.cos(t)));
         int g = (int)(255 * Math.abs(Math.cos(t + 2.0)));
         int b = (int)(255 * Math.abs(Math.cos(t + 4.0)));
         return new Color(r, g, b);*/
         /*double angle = iterations * 0.07;
         int r = (int)(192 + 63 * Math.cos(angle));
         int g = (int)(192 + 63 * Math.sin(angle * 1.5));
         int b = (int)(192 + 63 * Math.cos(angle * 2));
         return new Color(r, g, b);*/
        /*double t = (double)iterations / ZahlenfolgenRechner.ANZAHL_ITERATIONEN;
        int r = (int)(255 * Math.pow(t, 0.5));
        int g = (int)(255 * Math.pow(t, 3.0));
        int b = (int)(64 * Math.pow(t, 4.0));
        return new Color(r, g, b);*/
    }
}
