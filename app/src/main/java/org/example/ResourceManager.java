package org.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public class ResourceManager {
    private static ResourceManager resourceManager;

    private ResourceManager(){}

    public static ResourceManager getInstance(){
        if(resourceManager == null){
            resourceManager = new ResourceManager();
        }

        return resourceManager;
    }

    public ImageIcon loadImage(String name){
        try(InputStream inputStream = ResourceManager.class.getClassLoader().getResourceAsStream(name)){
            if(inputStream == null){
                throw new IllegalArgumentException("Image not found: " + name);
            }
            return new ImageIcon(ImageIO.read(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
