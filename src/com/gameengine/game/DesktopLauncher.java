package com.gameengine.game;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class DesktopLauncher {
    
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        config.setTitle("LibGDX 2D Platformer Game Engine");
        config.setWindowedMode(1200, 720);
        config.setForegroundFPS(60);
        config.setIdleFPS(60);
        
        new Lwjgl3Application(new PlatformerGame(), config);
    }
}
