package com.gameengine.engine.core;


import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class AbstractScreen {
    protected SpriteBatch batch;
    protected OrthographicCamera camera;
    protected float screenWidth;
    protected float screenHeight;
    
    public AbstractScreen(float screenWidth, float screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.batch = new SpriteBatch();
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, screenWidth, screenHeight);
    }
    
    public abstract void create();
    
    public abstract void update(float delta);
    
    public abstract void render();
    
    public abstract void resize(int width, int height);
    
    public void pause() {}
    
    public void resume() {}
    
    public abstract void dispose();
}
