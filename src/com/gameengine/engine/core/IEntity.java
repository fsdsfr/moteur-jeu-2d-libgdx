package com.gameengine.engine.core;

import com.badlogic.gdx.math.Vector2;

public interface IEntity {
    
    void update(float deltaTime);
    
    void render();
    
    void dispose();
    
    Vector2 getPosition();
    
    void setPosition(Vector2 position);
    
    Vector2 getSize();
    
    boolean isActive();
    
    void setActive(boolean active);
}
