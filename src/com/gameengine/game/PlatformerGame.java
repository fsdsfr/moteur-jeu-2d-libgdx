package com.gameengine.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.gameengine.engine.core.GameEngine;

public class PlatformerGame extends ApplicationAdapter {
    
    private GameEngine gameEngine;
    private static final float TARGET_FPS = 60f;
    private static final float SCREEN_WIDTH = 1200;
    private static final float SCREEN_HEIGHT = 720;
    
    @Override
    public void create() {
        
        Gdx.graphics.setTitle("LibGDX 2D Platformer Game Engine");
        
        gameEngine = GameEngine.getInstance();
        gameEngine.setTargetFPS(TARGET_FPS);
        
        MainMenuScreen mainMenu = new MainMenuScreen(SCREEN_WIDTH, SCREEN_HEIGHT);
        gameEngine.initialize(mainMenu);
    }
    
    @Override
    public void render() {
        
        float deltaTime = Gdx.graphics.getDeltaTime();
        
        gameEngine.update(deltaTime);
        gameEngine.render();
    }
    
    @Override
    public void resize(int width, int height) {
        gameEngine.resize(width, height);
    }
    
    @Override
    public void pause() {
        
    }
    
    @Override
    public void resume() {
        
    }
    
    @Override
    public void dispose() {
        gameEngine.stop();
    }
}
