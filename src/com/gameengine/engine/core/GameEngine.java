package com.gameengine.engine.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class GameEngine {
    private static GameEngine instance;
    
    private AbstractScreen currentScreen;
    private float targetFPS = 60f;
    private boolean isRunning = true;
    
    private GameEngine() {}
    
    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }
    
    public void initialize(AbstractScreen initialScreen) {
        this.currentScreen = initialScreen;
        if (this.currentScreen != null) {
            this.currentScreen.create();
        }
    }
    
    public void setScreen(AbstractScreen newScreen) {
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        currentScreen = newScreen;
        if (currentScreen != null) {
            currentScreen.create();
            
            currentScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }
    
    public void update(float delta) {
        if (currentScreen != null) {
            currentScreen.update(delta);
        }
    }
    
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        
        if (currentScreen != null) {
            currentScreen.render();
        }
    }
    
    public void resize(int width, int height) {
        if (currentScreen != null) {
            currentScreen.resize(width, height);
        }
    }
    
    public void stop() {
        isRunning = false;
        if (currentScreen != null) {
            currentScreen.dispose();
        }
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public void setTargetFPS(float fps) {
        this.targetFPS = fps;
    }
    
    public float getTargetFPS() {
        return targetFPS;
    }
}
