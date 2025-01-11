package systemlib;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

public class World {
    private final int width;
    private final int high;
    private JFrame window;
    private WorldPanel panel;
    private final String name;
    private Group allShapes;
    private Actor.KeyHandler keyHandler;
    private Ticker gameTicker;


    public World() {
        this("");
    }
    public World(String name) {
        width = Toolkit.getDefaultToolkit().getScreenSize().width;
        high = Toolkit.getDefaultToolkit().getScreenSize().height;
        initAttributes();
        this.name = name;
        panel.setPreferredSize(new Dimension(width,high));
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        initWindow();
    }

    public World(int width, int high, String name){
        this.width = width;
        this.high = high;
        initAttributes();
        this.name = name;
        panel.setPreferredSize(new Dimension(width,high));
        initWindow();
    }


    private void initAttributes(){
        window = new JFrame();
        panel = new WorldPanel(this);
        gameTicker = new Ticker();
        allShapes = new Group(this, true);
        keyHandler = new Actor.KeyHandler();
        this.addKeyListener(keyHandler);
    }

    public void setResizeable(boolean yn){
        window.setResizable(yn);
    }
    public Actor.KeyHandler getKeyHandler(){
    return keyHandler;
    }

    private void initWindow(){
        if (!ImageRegister.isLoaded()){
            ImageRegister.load();
        }
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setTitle(name);



        panel.setBackground(Color.BLACK);
        panel.setDoubleBuffered(true);
        panel.setFocusable(true);
        panel.requestFocus();

        window.add(panel);

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        }

        public void startDrawThread(){
            panel.startDrawThread();
        }
        public void startTicking(){
            gameTicker.startTicker();
        }
    public void addMouseListener(MouseListener listener){
        panel.addMouseListener(listener);
    }
    public void addKeyListener(KeyListener listener){
        panel.addKeyListener(listener);
    }


    public void clear(){
        allShapes.destroy();
    }
     public int getHeight() {
        return panel.getHeight();
     }

    public int getWidth(){
        return panel.getWidth();
    }

    public void setBackgroundColor(Color color){
        panel.setBackground(color);
    }
    public Color getBackgroundColor(){
        return panel.getBackground();
    }
    //public void setCoordinateSystem(double left, double top, double width, double height)

    public void tick(){
    allShapes.act();
    }

    public void draw(Graphics2D g2){
       allShapes.draw(g2);
    }

    public void addShape(Actor shape){
        allShapes.add(shape);
    }

    /*
    public void move(double x, double y)
    public void rotate(double angleInDeg, double x, double y)
    public void scale(double factor, double x, double y)
    public void setCursor(String cursor)
    */

    private class Ticker implements Runnable {

        public int tps = 40;
        private final double drawInterval = 1000000000.0 / tps;
        private Thread tickThread;

        public void startTicker() {
            tickThread = new Thread(this);
            tickThread.start();
            LOGGER.log("Started Game-Ticker!");
        }

        public void stopTicker(){
            tickThread.interrupt();
            LOGGER.log("Stopped Game-Ticker!");
        }

        @Override
        public void run() {
            double delta = 0;
            long lastTime = System.nanoTime();
            long currentTime;

            long timer = 0;

            while (tickThread != null) {

                currentTime = System.nanoTime();
                delta += (currentTime - lastTime) / drawInterval;
                timer += (currentTime - lastTime);
                lastTime = currentTime;

                if (delta >= 1) {
                    tick();
                    delta--;
                }

                if (timer >= 1000000000) {
                    timer = 0;
                }
            }
        }
    }
    private class WorldPanel extends JPanel implements Runnable{

        //Screen settings
        public double fps = 60;
        private final double drawInterval = 1000000000.0/fps;
        private Thread drawThread;

        private World w;

        public WorldPanel(World world){
            w = world;
        }

        public void startDrawThread(){
        drawThread = new Thread(this);
        drawThread.start();
        LOGGER.log("Started Draw-Ticker!");
        }


        public void paintComponent(Graphics g){
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            w.draw(g2);
        }

        @Override
        public void run() {
            double delta = 0;
            long lastTime = System.nanoTime();
            long currentTime;

            long timer = 0;
            int drawCount = 0;

            while(drawThread != null){

                currentTime = System.nanoTime();
                delta += (currentTime-lastTime)/drawInterval;
                timer += (currentTime-lastTime);
                lastTime = currentTime;

                if (delta >= 1){
                    repaint();
                    delta--;
                    drawCount++;
                }

                if (timer>= 1000000000){
                   //System.out.println("FPS: "+ drawCount);
                    drawCount = 0;
                    timer = 0;
                }

            }
        }
    }

}
