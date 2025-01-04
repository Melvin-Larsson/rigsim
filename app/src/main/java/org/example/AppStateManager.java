package org.example;

import java.io.File;
import java.io.IOException;

public class AppStateManager {
    public static final String APP_NAME = "psim";

    public static File getStateFile(String name) {
        String os = System.getProperty("os.name").toLowerCase();
        System.out.println("Using OS + " + os);
        File parent;
        if(os.contains("windows")){
            String appDataPath = System.getenv("APPDATA");
            parent = new File(appDataPath);
        }else if(os.contains("linux")){
            String home = System.getenv("HOME");
            parent = new File(home, ".config");
        }else if(os.contains("mac")){
            String home = System.getenv("HOME");
            parent = new File(home, "Library/Application Support");
        }else{
            throw new RuntimeException("Unknown operating system " + os);
        }

        File applicationFolder = ensureApplicationFolder(parent);
        return new File(applicationFolder, name);
    }

    private static File ensureApplicationFolder(File parent){
        File appFolder = new File(parent, APP_NAME);
        if(appFolder.exists() && appFolder.isDirectory()){
            return appFolder;
        }

        if(!appFolder.mkdirs()){
            throw new RuntimeException("Unable to create app folder (" + appFolder.getAbsolutePath() + ")");
        }

        return appFolder;
    }
}
