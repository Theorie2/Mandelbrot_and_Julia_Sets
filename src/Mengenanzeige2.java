import systemlib.Point;
import systemlib.World;

import java.awt.*;
import java.awt.event.*;
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
                float hue = (float) (i * 0.01f % 1.0f);
                Color color = Color.getHSBColor(hue, 0.8f, 1.0f);
                colorCache[i][0] = color.getRed();
                colorCache[i][1] = color.getGreen();
                colorCache[i][2] = color.getBlue();
            }
        }

        // Initialize point grid
        initializePoints();
        executorService = Executors.newFixedThreadPool(
                Math.max(1, Runtime.getRuntime().availableProcessors() / 2)
        );

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
        final int numChunks = (height + CHUNK_SIZE - 1) / CHUNK_SIZE;
        final CountDownLatch latch = new CountDownLatch(numChunks);
        final AtomicInteger activeChunks = new AtomicInteger(0);
        final int maxProcessors = Runtime.getRuntime().availableProcessors();

        Runnable renderTask = () -> {
            for (int startY = 0; startY < height; startY += CHUNK_SIZE) {
                final int currentStartY = startY;

                executorService.execute(() -> {
                    try {
                        int endY = Math.min(currentStartY + CHUNK_SIZE, height);

                        if (k % 1 == 0) {
                            renderChunk(currentStartY, endY, width, height);
                        } else {
                            renderChunkF(currentStartY, endY, width, height);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

//
//        if(k%1==0){
//            for (int startY = 0; startY < height; startY += CHUNK_SIZE) {
//                final int currentStartY = startY;
//                while (activeChunks.get() >= Runtime.getRuntime().availableProcessors()) {
//                    Thread.yield();
//                }
//
//                executorService.execute(() -> {
//                    activeChunks.incrementAndGet();
//                    try {
//                        int endY = Math.min(currentStartY + CHUNK_SIZE, height);
//                        renderChunk(currentStartY, endY, width,height);
//                    } finally {
//                        activeChunks.decrementAndGet();
//                        latch.countDown();
//                    }
//                });
//            }} else{
//            for (int startY = 0; startY < height; startY += CHUNK_SIZE) {
//                final int currentStartY = startY;
//                while (activeChunks.get() >= Runtime.getRuntime().availableProcessors()) {
//                    Thread.yield();
//                }
//
//                executorService.execute(() -> {
//                    activeChunks.incrementAndGet();
//                    try {
//                        int endY = Math.min(currentStartY + CHUNK_SIZE, height);
//                        renderChunkF(currentStartY, endY, width,height);
//                    } finally {
//                        activeChunks.decrementAndGet();
//                        latch.countDown();
//                    }
//                });
//            }
//        }
//
        new Thread(() -> {
            try {
                latch.await();
                isRendering = false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private synchronized void renderChunk(int startY, int endY, int width, int height) {
        double xScale = (maxX - minX) / (width - 1);
        double yScale = (maxY - minY) / (height - 1);
        Complex z = new Complex(0, 0);

        for (int y = startY; y < endY; y++) {
            double imag = minY + (y * yScale);

            for (int x = 0; x < width; x++) {
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

    private synchronized void renderChunkF(int startY, int endY, int width, int height) {
        double xScale = (maxX - minX) / (width - 1);
        double yScale = (maxY - minY) / (height - 1);
        Complex z = new Complex(0, 0);

        for (int y = startY; y < endY; y++) {
            double imag = minY + (y * yScale);

            for (int x = 0; x < width; x++) {
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
