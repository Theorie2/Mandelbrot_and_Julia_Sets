package systemlib;

import java.awt.*;

public class DialogWindow extends Sprite{
    private World w;
    public DialogWindow(String text,World w){
        super("/dialog_window_layout.png", 800, 850,w);
        scale(2.5);
        setColor(Color.BLUE);
        this.w = w;
        makeText(text);
    }

    public DialogWindow(String text, Color color, World w){
        super("/dialog_window_layout.png", 800, 850,w);
        scale(2.5);
        setColor(color);
        this.w = w;
        makeText(text);
    }

    private void makeText(String text){
        //Hier fehlt: Was wenn mehrzeilig? --> Längste Zeile wird genommen! aber muss noch geändert werden!
        Text realText =  new Text(w,x,y, text,(int)((40.0/(text.length()/2.0))));
        realText.setColor(Color.black);
        realText.moveTo(x-(int)(realText.getScale())*2,860);
    }
    public void setColor(Color color){
        for (int pixelX = 0;  pixelX < getImage().getWidth(); pixelX++){
            for (int pixelY = 0;  pixelY < getImage().getHeight(); pixelY++){
                if (getImage().getRGB(pixelX,pixelY) != 0){
            getImage().setRGB(pixelX, pixelY, color.getRGB());
                }
            }
        }
    }

}
