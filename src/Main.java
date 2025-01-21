import systemlib.LOGGER;
import systemlib.Point;
import systemlib.Sprite;
import systemlib.World;

import javax.swing.*;
import java.awt.*;


public class Main {


    public static void main(String... args) {
        LOGGER.load("C:\\Users\\Toran\\IdeaProjects\\MandelbrotmengenGenerator\\res\\logs\\log.txt");
        Mengenanzeige3 ma = new Mengenanzeige3(2.5,new Complex(-0.8, 0.156),false);
        //Mengenanzeige ma2 = new Mengenanzeige(2,new Complex(-0.8, 0.156),false,-1.75,1.75,1.0-1,-1.0-1);
    }
}