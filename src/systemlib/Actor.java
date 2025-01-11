package systemlib;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public abstract class Actor{
    private World world;
    private boolean destroyed;
    protected boolean mirroredX;
    protected boolean mirroredY;
    private boolean isActing;

    protected Group group;

    public Actor(World world){
        this.world = world;
        isActing = true;
        destroyed = false;
        mirroredX = false;
        mirroredY = false;
        if (!(this instanceof Shape)){
        world.addShape(this);
        }
    }

    public World getWorld(){
        return world;
    }


    public void changeWorld(World world){
        this.world = world;
    }



    public void destroy(){
        destroyed = true;
    }

    public boolean isDestroyed(){
        return destroyed;
    }


    public void bringToFront(){

    }

    public static class KeyHandler implements KeyListener {
        private int[] keysPressed;
        public KeyHandler(){
            keysPressed = new int[10];
        }
        public KeyHandler(int maxKeysAtTheSameTime){
            keysPressed = new int[maxKeysAtTheSameTime];
        }
        public boolean isKeyPressed(int keyCode){
            for (int e: keysPressed){
                if (e == keyCode) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            for (int i = 0; i < keysPressed.length; i++) {
                if (keysPressed[i] == 0 && !isKeySaved(e)) {
                    keysPressed[i] = e.getKeyCode();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();
            for (int i = 0; i < keysPressed.length; i++){
                if (code == keysPressed[i]){
                    keysPressed[i] = 0;
                    break;
                }
            }
        }
        private boolean isKeySaved(KeyEvent keyEvent){
            for (int e: keysPressed){
                if (keyEvent.getKeyCode() == e){
                    return true;
                }
            }
            return false;
        }
    }
    public void act(){

    }

    public void draw(Graphics2D g2){
        if (this.isActing()) {
            act();
        }
    }
    public void move(int dx,int dy){

    }

    public boolean isActing(){
        return this.isActing;
    }

    public void setActing(boolean shouldAct){
        this.isActing = shouldAct;
    }
}
