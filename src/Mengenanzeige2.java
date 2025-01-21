import systemlib.Point;
import systemlib.World;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Mengenanzeige2 {
    private static final int CHUNK_SIZE = 1; // Larger chunks to reduce overhead
    private final World w;
    private double minX, maxX, minY, maxY;
    private final ZahlenfolgenRechner zr;
    private final boolean julia;
    private final double originalMinX, originalMaxX, originalMinY, originalMaxY;
    private final ExecutorService executorService;
    private final int[][] colorCache;
    private volatile boolean isRendering;
    private volatile boolean isRendering2;
    private Point[][] points;
    private double k;

    public Mengenanzeige2(double k, Complex c, boolean julia) {
        this(k, c, julia, -1.75, 1.75, -1.0, 1.0);
    }

    public Mengenanzeige2(double k, Complex c, boolean julia, double minX, double maxX, double minY, double maxY) {
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

        w = new World(1400, 800, "Fractals");
        w.setResizeable(false);
        w.setBackgroundColor(Color.BLACK);

        // Pre-calculate colors
        colorCache = new int[ZahlenfolgenRechner.ANZAHL_ITERATIONEN + 1][3]; // RGB components
        for (int i = 0; i < colorCache.length; i++) {
            if (i >= ZahlenfolgenRechner.ANZAHL_ITERATIONEN) {
                colorCache[i] = new int[]{255, 255, 255}; // White
            } else {
                Color color = colorCalculation(i,1);
                colorCache[i][0] = color.getRed();
                colorCache[i][1] = color.getGreen();
                colorCache[i][2] = color.getBlue();
            }
        }

        // Initialize point grid
        initializePoints();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


        zr = new ZahlenfolgenRechner(k, c);

        setupMouseListeners();

        w.startDrawThread();
        w.startTicking();
        display();
    }

    private void initializePoints() {
        int width = w.getWidth();
        int height = w.getHeight();
        points = new Point[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                points[y][x] = new Point(x, y, w);
            }
        }

    }

    private void setupMouseListeners() {
        w.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isRendering) {
                    switch (e.getButton()) {
                        case MouseEvent.BUTTON1:
                            zoom(e.getX(), e.getY(), 5);
                            break;
                        case MouseEvent.BUTTON3:
                            zoom(e.getX(), e.getY(), 0.2);
                            break;
                        case MouseEvent.BUTTON2:
                            resetView();
                            break;
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

        w.clear();

        final int width = w.getWidth();
        final int height = w.getHeight();

        // Calculate optimal chunk size based on screen height and available processors
        final int optimalChunkSize = Math.max(1, height / (Runtime.getRuntime().availableProcessors() * 2));
        final int numChunks = (height + optimalChunkSize - 1) / optimalChunkSize;

        final CountDownLatch latch = new CountDownLatch(numChunks);
        final AtomicInteger activeChunks = new AtomicInteger(0);

        // Create tasks for each chunk
        for (int startY = 0; startY < height; startY += optimalChunkSize) {
            final int currentStartY = startY;
            final int endY = Math.min(currentStartY + optimalChunkSize, height);

            // Submit task to executor service
            executorService.execute(() -> {
                try {
                    // Record active chunks for monitoring
                    activeChunks.incrementAndGet();

                    // Choose rendering method based on k value
                    if (k % 1 == 0) {
                        renderChunk(currentStartY, endY, width, height);
                    } else {
                        renderChunkF(currentStartY, endY, width, height);
                    }
                } catch (Exception e) {
                    // Log any rendering errors
                    System.err.println("Error rendering chunk: " + e.getMessage());
                } finally {
                    activeChunks.decrementAndGet();
                    latch.countDown();
                }
            });
        }

        // Create monitoring thread to track completion
        Thread monitorThread = new Thread(() -> {
            try {
                // Wait for all chunks to complete
                latch.await();

                // Reset rendering flag
                isRendering = false;

                // Optional: trigger any post-rendering operations here
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                isRendering = false;
            }
        });

        // Set monitor thread as daemon to not prevent JVM shutdown
        monitorThread.setDaemon(true);
        monitorThread.start();
    }


    private void renderChunk(int startY, int endY, int width, int height) {
        double xScale = (maxX - minX) / (width - 1);
        double yScale = (maxY - minY) / (height - 1);
        Complex z = new Complex(0, 0);

        for (int y = startY; y < endY; y++) {
            double imag = minY + (y * yScale);


                for (int x = 0; x < width; x++) {
            synchronized(points) {
                    double real = minX + (x * xScale);
                    z.setReal(real);
                    z.setImag(imag);

                    int iterations = julia ?
                            zr.zahlInJuliaMenge(z) :
                            zr.zahlInMandelbrotmenge(z);

                    int[] rgb = colorCache[iterations];
                    points[y][x].setColor(new Color(rgb[0], rgb[1], rgb[2]));
                }
            }
        }
    }

    private void renderChunkF(int startY, int endY, int width, int height) {
        double xScale = (maxX - minX) / (width - 1);
        double yScale = (maxY - minY) / (height - 1);
        Complex z = new Complex(0, 0);

        for (int y = startY; y < endY; y++) {
            double imag = minY + (y * yScale);

            // Synchronize on each row to prevent line artifacts
                for (int x = 0; x < width; x++) {
            synchronized(points) {
                    double real = minX + (x * xScale);
                    z.setReal(real);
                    z.setImag(imag);

                    int iterations = julia ?
                            zr.zahlInJuliaMengeF(z) :
                            zr.zahlInMandelbrotmengeF(z);

                    int[] rgb = colorCache[iterations];
                    points[y][x].setColor(new Color(rgb[0], rgb[1], rgb[2]));
                }
            }
        }
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
    public Color colorCalculation(int i, int type){
        switch (type){
            case 1:
                float hue = (float) (i * 0.01f % 1.0f);
                return Color.getHSBColor(hue, 0.8f, 1.0f);
            case 2: // Fire colors
                float red = Math.min(1.0f, (i % 100) / 50.0f);
                float green = Math.max(0.0f, Math.min(1.0f, (i % 100 - 25) / 50.0f));
                float blue = Math.max(0.0f, Math.min(1.0f, (i % 100 - 50) / 50.0f));
                return new Color(red, green, blue);

            case 3: // Blue to white gradient
                float intensity = Math.min(1.0f, (i % 100) / 100.0f);
                return new Color(intensity, intensity, 1.0f);

            case 4: // Psychedelic
                float h = (float) (Math.sin(i * 0.1) * 0.5 + 0.5);
                float s = (float) (Math.cos(i * 0.2) * 0.5 + 0.5);
                float b = (float) (Math.sin(i * 0.3) * 0.5 + 0.5);
                return Color.getHSBColor(h, s, b);

            case 5: // Grayscale
                float gray = (i % 256) / 255.0f;
                return new Color(gray, gray, gray);

            case 6: // Ocean depths
                float depth = Math.min(1.0f, (i % 100) / 100.0f);
                return new Color(0.0f, depth * 0.5f, depth);

            case 7: // Golden ratio coloring
                float goldenRatio = 0.618033988749895f;
                float hue2 = (i * goldenRatio) % 1;
                return Color.getHSBColor(hue2, 0.85f, 0.9f);

            case 8: // Smooth normalized iteration count
                double zn = Math.sqrt(i * 1.0 / 100);
                float hue3 = (float) (zn % 1.0);
                return Color.getHSBColor(hue3, 0.9f, 0.9f);
            default :
                return Color.BLACK;
        }

    }
    /*public void generateImage(String imagePath, int pixelX, int pixelY){
        BufferedImage image = new BufferedImage(pixelX,pixelY,1);
        if (isRendering2) return;
        isRendering2 = true;

        final int width = pixelX;
        final int height = pixelY;
        final int numChunks = (height + CHUNK_SIZE - 1) / CHUNK_SIZE;
        final CountDownLatch latch = new CountDownLatch(numChunks);
        Runnable renderTask = () -> {
            for (int startY = 0; startY < height; startY += CHUNK_SIZE) {
                final int currentStartY = startY;

                int finalStartY = startY;
                int finalStartY1 = startY;
                executorService.execute(() -> {
                    try {
                        int endY = Math.min(currentStartY + CHUNK_SIZE, height);

                        if (k % 1 == 0) {
                            double xScale = (maxX - minX) / (width - 1);
                            double yScale = (maxY - minY) / (height - 1);
                            Complex z = new Complex(0, 0);

                            for (int y = finalStartY; y < endY; y++) {
                                double imag = minY + (y * yScale);

                                for (int x = 0; x < width; x++) {
                                    double real = minX + (x * xScale);
                                    z.setReal(real);
                                    z.setImag(imag);

                                    int iterations = julia ?
                                            zr.zahlInJuliaMenge(z) :
                                            zr.zahlInMandelbrotmenge(z);

                                    int[] rgb = colorCache[iterations];
                                    image.setRGB(x,y,new Color(rgb[0],rgb[1],rgb[2]).getRGB());
                                }
                            }
                        } else {
                            double xScale = (maxX - minX) / (width - 1);
                            double yScale = (maxY - minY) / (height - 1);
                            Complex z = new Complex(0, 0);

                            for (int y = finalStartY1; y < endY; y++) {
                                double imag = minY + (y * yScale);

                                for (int x = 0; x < width; x++) {
                                    double real = minX + (x * xScale);
                                    z.setReal(real);
                                    z.setImag(imag);

                                    int iterations = julia ?
                                            zr.zahlInJuliaMengeF(z) :
                                            zr.zahlInMandelbrotmengeF(z);
                                    int[] rgb = colorCache[iterations];
                                    image.setRGB(x,y,new Color(rgb[0],rgb[1],rgb[2]).getRGB());
                                }
                            }
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
        };
        renderTask.run();
        new Thread(() -> {
            try {
                latch.await();
                isRendering2 = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        try {
            latch.await();
            try {
                boolean gespeichert = ImageIO.write(image, "png", new File(imagePath));

                if (gespeichert) {
                    System.out.println("Das Bild wurde erfolgreich gespeichert unter: " + imagePath);
                } else {
                    System.out.println("Das Bild konnte nicht gespeichert werden.");
                }
            } catch (IOException e) {
                System.err.println("Fehler beim Speichern des Bildes: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }*/
    }



