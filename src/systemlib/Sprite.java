package systemlib;

import java.awt.*;
import java.awt.image.BufferedImage;


public class Sprite extends Shape {
    private World world;
    private BufferedImage image;
    private Graphics2D g2;
    private String imagePath;
    public Sprite(BufferedImage image, int x, int y, World world){
        super(world);
        this.image = image;
        initSprite();
        this.x = x;
        this.y = y;
        this.world = world;
    }

    public Sprite(String imagePath, int x, int y, World world){
        super(world);
        this.image = ImageRegister.getImage(imagePath);

        initSprite();
        this.x = x;
        this.y = y;
        this.world = world;
    }

    private void initSprite(){
        width = image.getWidth();
        height = image.getHeight();
        resetCenter();
    }

    private void tick(){

        if (isVisible()&&!isDestroyed()&&!isOutsideView()){
            resetCenter();
            g2.rotate(getAngleInRadians(),getCenterX(),getCenterY());

            g2.drawImage(image,x-(mirroredX ? -width : width),y-(mirroredY ? -height : height),
                    (int)((mirroredX ? -width : width)*getScale()),(int)((mirroredY ? -height : height) *getScale()),
                    null);

            g2.rotate(-getAngleInRadians(),getCenterX(),getCenterY());
        }
        if (!isDestroyed()){
        act();
        }
    }


    @Override
    public void draw(Graphics2D g2) {
        if (isVisible()){
        this.g2 = g2;
        }
        tick();
    }

    public String toString(){
        return getClass().getName();
    }

    @Override
    public void destroy() {
        super.destroy();
        image = null;
    }

    @Override
    public void resetCenter() {
        defineCenter(x+ (int)((image.getWidth()/2.0)*getScale()),y+(int)((image.getHeight()/2)*getScale()));
    }

    @Override
    public boolean isDestroyed() {
        return image == null;
    }

    public void setImage(BufferedImage image){
        this.image = image;
        height = this.image.getHeight();
        width = this.image.getWidth();
    }

    //Not working yet with mirrorY() but with mirrorX()
    public boolean collidesWith(Sprite object) {

        if (new Rectangle(x,y,(int)(width*getScale()),(int)(height*getScale()))
                .intersects(new Rectangle(object.x,object.y,(int)(object.width * object.getScale()),
                        (int)(object.height * object.getScale())))){

            //making the first mask to compare that one with the other one
            long[][][] maskThis = new long[(int)(width * getScale())][(int)(height*getScale())][3];
            if (!mirroredX) {
                if (!mirroredY) {
                    for (int widthThis = 0; widthThis < (int)(this.width * getScale()); widthThis++) {
                        for (int heightThis = 0; heightThis < (int)(this.height * getScale()); heightThis++) {

                            maskThis[widthThis][heightThis][0] =
                                    image.getRGB((int) (widthThis / this.getScale()), (int) (heightThis / this.getScale())) != 0 ? 1 : 0;
                            maskThis[widthThis][heightThis][1] = widthThis + 1 + x;
                            maskThis[widthThis][heightThis][2] = heightThis + 1 + y;
                        }
                    }
                } else {
                    for (int widthThis = 0; widthThis < (int)(this.width * getScale()); widthThis++) {
                        for (int heightThis = 0; heightThis < (int)(this.height * getScale()); heightThis++) {

                            maskThis[widthThis][heightThis][0] =
                                    image.getRGB((int) (widthThis / this.getScale()), (int) (heightThis / this.getScale())) != 0 ? 1 : 0;
                            maskThis[widthThis][heightThis][1] = widthThis + 1 + x;
                            maskThis[widthThis][heightThis][2] = (int)(this.height*getScale()) - heightThis + 1 + this.y;
                        }
                    }
                }

            } else{
                if (!mirroredY){
                    for (int widthThis = 0; widthThis < (int)(this.width * this.getScale()); widthThis++){
                        for (int heightThis = 0; heightThis < (int)(this.height * this.getScale()); heightThis++){

                            maskThis[widthThis][heightThis][0] =
                                    image.getRGB((int)(widthThis/this.getScale()),(int)(heightThis/this.getScale())) != 0 ? 1 : 0;
                            maskThis[widthThis][heightThis][1] = (int)(this.width*getScale()) - widthThis + 1 + this.x;
                            maskThis[widthThis][heightThis][2] = heightThis + 1 + this.y;
                        }
                    }
                } else {
                    for (int widthThis = 0; widthThis < (int)(this.width * getScale()); widthThis++) {
                        for (int heightThis = 0; heightThis < (int)(this.height * getScale()); heightThis++) {

                            maskThis[widthThis][heightThis][0] =
                                    image.getRGB((int) (widthThis / this.getScale()), (int) (heightThis / this.getScale())) != 0 ? 1 : 0;
                            maskThis[widthThis][heightThis][1] = (int)(this.width*getScale()) - widthThis + 1 + this.x;
                            maskThis[widthThis][heightThis][2] = (int)(this.height*getScale()) - heightThis + 1 + this.y;
                        }
                    }
                }
            }

            //making the second mask to compare that one with the other one
            long[][][] maskObject = new long[(int)(object.width* object.getScale())][(int)(object.height* object.getScale())][3];
            if (!object.mirroredX){
                if (!object.mirroredY) {
                    for (int widthThis = 0; widthThis < (int)(object.width * object.getScale()); widthThis++) {
                        for (int heightThis = 0; heightThis < (int)(object.height * object.getScale()); heightThis++) {

                            maskObject[widthThis][heightThis][0] =
                                    object.getImage().getRGB((int) (widthThis / object.getScale()), (int) (heightThis / object.getScale())) != 0 ? 1 : 0;
                            maskObject[widthThis][heightThis][1] = widthThis + 1 + object.x;
                            maskObject[widthThis][heightThis][2] = heightThis + 1 + object.y;
                        }
                    }
                } else{
                    for (int widthThis = 0; widthThis < (int)(object.width * object.getScale()); widthThis++) {
                        for (int heightThis = 0; heightThis < (int)(object.height * object.getScale()); heightThis++) {

                            maskObject[widthThis][heightThis][0] =
                                    object.getImage().getRGB((int) (widthThis / object.getScale()), (int) (heightThis / object.getScale())) != 0 ? 1 : 0;
                            maskObject[widthThis][heightThis][1] = widthThis + 1 + object.x;
                            maskObject[widthThis][heightThis][2] = (int)(object.height* object.getScale()) - heightThis+1+ object.y;
                        }
                    }
                }
            } else{
                if (!object.mirroredY){

                for (int widthThis = 0; widthThis < (int)(object.width * object.getScale()); widthThis++){
                    for (int heightThis = 0; heightThis < (int)(object.height * object.getScale()); heightThis++){
                        maskObject[widthThis][heightThis][0] =
                                object.getImage().getRGB((int)(widthThis/object.getScale()),(int)(heightThis/object.getScale()))!= 0 ? 1 : 0;
                        maskObject[widthThis][heightThis][1] = (int)(object.width* object.getScale()) - widthThis+1+ object.x;
                        maskObject[widthThis][heightThis][2] = heightThis+1 + object.y;
                    }
                }
                } else {
                    for (int widthThis = 0; widthThis < (int)(object.width * object.getScale()); widthThis++){
                        for (int heightThis = 0; heightThis < (int)(object.height * object.getScale()); heightThis++){
                            maskObject[widthThis][heightThis][0] =
                                    object.getImage().getRGB((int)(widthThis/object.getScale()),(int)(heightThis/object.getScale()))!= 0 ? 1 : 0;
                            maskObject[widthThis][heightThis][1] = (int)(object.width* object.getScale()) - widthThis+1+ object.x;
                            maskObject[widthThis][heightThis][2] = (int)(object.height* object.getScale()) - heightThis+1+ object.y;;
                        }
                    }
                }
            }



            //the final answer
            for (int thatWidth = 0; thatWidth < (int)(this.width * getScale()); thatWidth++) {
                for (int thatHeight = 0; thatHeight < (int)(this.height * getScale()); thatHeight++) {

                    if (maskThis[thatWidth][thatHeight][0] != 0) {
                        for (int objectThatWidth = 0; objectThatWidth < (int)(object.width * object.getScale()); objectThatWidth++){
                            for (int objectThatHeight = 0; objectThatHeight < (int)(object.height * object.getScale()); objectThatHeight++) {
                                if (maskObject[objectThatWidth][objectThatHeight][1] == maskThis[thatWidth][thatHeight][1]
                                        && maskObject[objectThatWidth][objectThatHeight][2] == maskThis[thatWidth][thatHeight][2]
                                        && maskObject[objectThatWidth][objectThatHeight][0] != 0
                                ){
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        return false;
    }

    public BufferedImage getImage(){
        return this.image;
    }
}