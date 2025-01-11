package systemlib;

import java.awt.*;

public abstract class Shape extends Actor {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    private double scale;
    private double angle;
    private double direction;
    private boolean visible;
    private int centerX;
    private int centerY;
    private final boolean isParentGroup;
    World world;
    public Shape(World world){
        super(world);
        isParentGroup = false;
        this.world = world;
        world.addShape(this);
        initShape();
    }

    public Shape(World world, boolean isParentGroup){
        super(world);
        this.isParentGroup = isParentGroup;
        if (!isParentGroup) {
            world.addShape(this);
        }
        this.world = world;
        initShape();
    }

    private void initShape(){
        scale = 1.0;
        angle = 0;
        visible = true;
        centerX = x+(width/2);
        centerY = y+ (height /2);
    }

    public boolean collidesWith(Shape object){
        if (new Rectangle(width,height).intersects(new Rectangle(object.width,object.height))){
            return true;
        }
        return false;
    }


    public boolean containsPoint(double nX, double nY) {
        return this.x <= nX && this.x + width >= nX && this.y <= nY && this.y + height >= nY;
    }



    public Shape copy(){
        return this;
    }


    public void forward(double distance){
        move((int)(Math.cos(angle)*distance),(int)(Math.sin(angle)*distance));
    }

    public int getCenterX(){
        resetCenter();
     return centerX;
    }
    public int getCenterY(){
        resetCenter();
        return centerY;
    }

    public Group getParentGroup() {
        return group;
    }
    public double getScale() {
        return scale;
    }
    public boolean isOutsideView() {
        return x+width*getScale() <= 0 || x >= world.getWidth()+width || y + height*getScale() <= 0 || y >= world.getHeight()+height;
    }
    public boolean isVisible() {
        return visible;
    }
    public void mirrorX() {
        if (!mirroredX){
            mirroredX = true;
        } else {
            mirroredX = false;
        }
    }
    public void mirrorY() {
        if (!mirroredY){
        mirroredY = true;
        } else {
        mirroredY = false;
        }
        y += height;
    }

    //Bewegt das Objekt von dem jetzigen Standpunkt aus
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    //Bewegt das Objekt direkt zu dieser Position
    public void moveTo(int x, int y){
    this.x = x;
    this.y = y;
    }


    public void rotate(double angleInDeg) {
    angle = angle+Math.toRadians(angleInDeg);
    }
    public void setAngle(double angleDeg) {
        angle = Math.toRadians(angleDeg);
    }
    public double getAngleInDegree(){
        return Math.toDegrees(angle);
    }
    public double getAngleInRadians(){
        return (angle);
    }

    public void sendToBack() {

    }

    public void scale(double factor) {
        scale *= factor;
        x-=(int)(width/2.0*scale);
        y-=(int)(height/2.0*scale);
    }
    public void setScale(double newScale){
        scale = newScale;
    }
    public void setVisible(boolean visible){
        this.visible = visible;
    }

    public void draw(Graphics2D g2){

    }
    //public boolean collidesWithAnyShape()
    public void defineCenter(int newX, int newY){
      centerX = newX;
      centerY = newY;
    }
    public void resetCenter(){
        centerX = x+(int)(width*getScale()/2.0);
        centerY = y+ (int)(height*getScale() /2.0);
    }

    @Override
    public void destroy() {
        if (!isParentGroup){
        super.destroy();

        visible = false;
        group.remove(this);
        }
    }
    // public void rotate(double angleInDeg, double centerX, double centerY)
    //public Shape[] getCollidingShapes(Group group)
    // public void scale(double factor, double centerX, double centerY)
}
