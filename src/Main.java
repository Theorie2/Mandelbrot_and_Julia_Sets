import systemlib.LOGGER;
import systemlib.Point;
import systemlib.Sprite;
import systemlib.World;

import javax.swing.*;
import java.awt.*;


public class Main {


    public static void main(String... args) {
        LOGGER.load("C:\\Users\\Toran\\IdeaProjects\\MandelbrotmengenGenerator\\res\\logs\\log.txt");
        Mengenanzeige2 ma = new Mengenanzeige2(2,new Complex(-0.8, 0.156),false);


        //ma.close();
        //Mengenanzeige ma2 = new Mengenanzeige(2,new Complex(-0.8, 0.156),true,-1.75,1.75,1.0-1,-1.0-1);
    }
}