package systemlib;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class ImageRegister {
    private static boolean loaded = false;
    public static String[] imagePaths = new String[]{
            "/test.png"
    };
    public static BufferedImage[] images = new BufferedImage[imagePaths.length];

    public static void load(){
        for(int i = 0; i < ImageRegister.imagePaths.length;i++){
            try{
                images[i] = ImageIO.read(ImageRegister.class.getResourceAsStream(imagePaths[i]));
                LOGGER.log("Loaded textures correctly");
            } catch (IOException e){
                e.printStackTrace();
                LOGGER.log("Textures loaded not correctly");
            }
        }
        loaded = true;
    }

    public static BufferedImage getImage(String imagePath){
        for(int i = 0; i < ImageRegister.imagePaths.length;i++){
            if (Objects.equals(ImageRegister.imagePaths[i], imagePath)){
            return images[i];
            }
        }
        return images[0];
    }

    public static boolean isLoaded() {
        return loaded;
    }

}
