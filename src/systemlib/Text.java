package systemlib;

import java.awt.*;

public class Text extends Shape{
    private String text;
    private Color color;
    private Graphics2D g2;
    private Font font;
    public Text(World world, int x, int y, String text) {
        super(world);
        this.text = text;
        this.x = x;
        this.y = y;
        this.world = world;
        color = Color.BLUE;
        font = new Font("Arial",Font.PLAIN,10);

    }

    public Text(World world, int x, int y, String text, int fontsize) {
        super(world);
        this.text = text;
        this.x = x;
        this.y = y;
        this.world = world;
        color = Color.BLUE;
        font = new Font("Arial",Font.PLAIN,fontsize);
    }

    public Text(World world, int x, int y, String text, int fontsize, String fount) {
        super(world);
        this.text = text;
        this.x = x;
        this.y = y;
        this.world = world;
        color = Color.BLUE;
        font = new Font(fount, Font.PLAIN, fontsize);
    }
    public Text(World world, int x, int y, String text, Font font) {
        super(world);
        this.text = text;
        this.x = x;
        this.y = y;
        this.world = world;
        color = Color.BLUE;
        this.font = font;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (isVisible()){
        super.draw(g2);
        this.g2 = g2;
        g2.setColor(color);
        g2.setFont(font);
        if (!text.contains("\n")){
        g2.drawString(text, x,y);
        } else {
            String[] textPart = text.split("\n");
            for (int i = 0; i < textPart.length; i++){
                g2.drawString(textPart[i],x,y+(int)((i==0?-5: i == 1 ? 0 : i == 2 ? +5 :+10)*getScale()/2.0));
                System.out.println(getScale());
            }
        }

        }
    }
    @Override
    public void scale(double factor){
        Font oldFont = font;
       font = new Font(oldFont.getFontName(),oldFont.getStyle(),(int)(oldFont.getSize()*factor));
    }

    @Override
    public void setScale(double newFontSize) {
        Font oldFont = font;
        font = new Font(oldFont.getFontName(),oldFont.getStyle(),(int)(newFontSize));
    }

    @Override
    public double getScale() {
        return font.getSize();
    }

    public void setColor(Color color){
        this.color = color;
    }

    public String getText(){
        return this.text;
    }
}
