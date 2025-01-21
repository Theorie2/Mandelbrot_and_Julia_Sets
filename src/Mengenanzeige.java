import systemlib.Point;
import systemlib.Sprite;
import systemlib.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

public class Mengenanzeige {
    private static final int CHUNK_HEIGHT = 4; // Small chunks for better work distribution
    private final World w;
    private double minX, maxX, minY, maxY;
    private final ZahlenfolgenRechner zr;
    private final boolean julia;
    private final double originalMinX, originalMaxX, originalMinY, originalMaxY;
    private final BufferedImage buffer;
    private final int[] directPixels; // Direct access to image pixels
    private final ExecutorService executorService;
    private final Color[] colorPalette;
    private volatile boolean isRendering;
    private final double k;
    public Mengenanzeige(double k, Complex c, boolean julia) {
        this(k, c, julia, -1.75, 1.75, -1.0, 1.0);
    }

    public Mengenanzeige(double k, Complex c, boolean julia, double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.originalMinX = minX;
        this.originalMaxX = maxX;
        this.originalMinY = minY;
        this.originalMaxY = maxY;
        this.julia = julia;
        this.k = k;

        w = new World(1400, 800, "Fraktale");
        w.setResizeable(false);
        w.startDrawThread();
        w.startTicking();

        buffer = new BufferedImage(w.getWidth(), w.getHeight(), BufferedImage.TYPE_INT_RGB);
        // Get direct access to the image pixels
        directPixels = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();

        // Use number of physical cores (not logical processors) for better performance
        int cores = Runtime.getRuntime().availableProcessors() / 2;
        cores = Math.max(1, cores); // Ensure at least one core
        executorService = Executors.newFixedThreadPool(cores);

        zr = new ZahlenfolgenRechner(k, c);

        // Pre-compute color palette
        colorPalette = new Color[ZahlenfolgenRechner.ANZAHL_ITERATIONEN + 1];
        initializeColorPalette();

        setupMouseListeners();
        display();
    }

    private void initializeColorPalette() {
        for (int i = 0; i < colorPalette.length; i++) {
            if (i >= ZahlenfolgenRechner.ANZAHL_ITERATIONEN) {
                colorPalette[i] = Color.WHITE;
            } else {
                float hue = (float)(i * 0.01f % 1.0f);
                colorPalette[i] = Color.getHSBColor(hue, 0.8f, 1.0f);
            }
        }
    }

    private void setupMouseListeners() {
        w.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isRendering) {
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1: zoom(e.getX(), e.getY(), 5); break;
                        case MouseEvent.BUTTON3: zoom(e.getX(), e.getY(), 0.2); break;
                        case MouseEvent.BUTTON2: resetView(); break;
                    }
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

    private void display() {
        if (isRendering) return;
        isRendering = true;

        final int width = w.getWidth();
        final int height = w.getHeight();
        final int totalChunks = (height + CHUNK_HEIGHT - 1) / CHUNK_HEIGHT;
        final CountDownLatch latch = new CountDownLatch(totalChunks);

        final double xScale = (maxX - minX) / (width - 1);
        final double yScale = (maxY - minY) / (height - 1);

        // Process in small chunks for better load balancing
        if(k%1==0) {//Einfachere Potenzfunktion wird hier benutzt um Resourcen zu sparen
            for (int startY = 0; startY < height; startY += CHUNK_HEIGHT) {
                final int currentStartY = startY;
                executorService.execute(() -> {
                    try {
                        int endY = Math.min(currentStartY + CHUNK_HEIGHT, height);
                        renderChunk(currentStartY, endY, width, xScale, yScale);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        } else {//Für keine ganzen Zahlen nötig aber unperformant
            for (int startY = 0; startY < height; startY += CHUNK_HEIGHT) {
                final int currentStartY = startY;
                executorService.execute(() -> {
                    try {
                        int endY = Math.min(currentStartY + CHUNK_HEIGHT, height);
                        renderChunkF(currentStartY, endY, width, xScale, yScale);
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        // Wait for all chunks to complete and update display
        new Thread(() -> {
            try {
                latch.await();
                updateDisplay();
                isRendering = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void renderChunk(int startY, int endY, int width, double xScale, double yScale) {
        // Pre-calculate complex numbers for the row
        Complex z = new Complex(0, 0);

        for (int y = startY; y < endY; y++) {
            double imag = minY + (y * yScale);
            int rowOffset = y * width;

            for (int x = 0; x < width; x++) {
                double real = minX + (x * xScale);
                z.setReal(real);
                z.setImag(imag);

                int iterations = julia ? zr.zahlInJuliaMenge(z) : zr.zahlInMandelbrotmenge(z);
                directPixels[rowOffset + x] = colorPalette[iterations].getRGB();
            }
        }
    }
    private void renderChunkF(int startY, int endY, int width, double xScale, double yScale) {
        // Pre-calculate complex numbers for the row
        Complex z = new Complex(0, 0);

        for (int y = startY; y < endY; y++) {
            double imag = minY + (y * yScale);
            int rowOffset = y * width;

            for (int x = 0; x < width; x++) {
                double real = minX + (x * xScale);
                z.setReal(real);
                z.setImag(imag);

                int iterations = julia ? zr.zahlInJuliaMengeF(z) : zr.zahlInMandelbrotmengeF(z);
                directPixels[rowOffset + x] = colorPalette[iterations].getRGB();
            }
        }
    }

    private void updateDisplay() {
        SwingUtilities.invokeLater(() -> {
            new Sprite(buffer, w.getWidth(), w.getHeight(), w);
        });
    }

    public void zoom(int centerX, int centerY, double scale) {
        if (isRendering) return;

        double mathX = minX + (centerX * (maxX - minX) / w.getWidth());
        double mathY = minY + (centerY * (maxY - minY) / w.getHeight());

        double rangeX = (maxX - minX) / scale;
        double rangeY = (maxY - minY) / scale;

        minX = mathX - (rangeX / 2);
        maxX = mathX + (rangeX / 2);
        minY = mathY - (rangeY / 2);
        maxY = mathY + (rangeY / 2);

        display();
    }

    public void close() {
        executorService.shutdown();
    }
}