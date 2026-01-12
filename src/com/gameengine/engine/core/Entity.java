package com.gameengine.engine.core;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity implements IEntity {
    
    protected Vector2 position;
    protected Vector2 size;
    protected boolean active;
    protected SpriteBatch batch;
    
    public Entity(float x, float y, float width, float height, SpriteBatch batch) {
        this.position = new Vector2(x, y);
        this.size = new Vector2(width, height);
        this.active = true;
        this.batch = batch;
    }
    
    @Override
    public void update(float deltaTime) {
        
    }
    
    @Override
    public void render() {
        
    }
    
    @Override
    public void dispose() {
        
    }
    
    @Override
    public Vector2 getPosition() {
        return position.cpy();
    }
    
    @Override
    public void setPosition(Vector2 position) {
        this.position = position.cpy();
    }
    
    @Override
    public Vector2 getSize() {
        return size.cpy();
    }
    
    @Override
    public boolean isActive() {
        return active;
    }
    
    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, size.x, size.y);
    }
    
    public boolean collidesWith(Entity other) {
        return this.getBounds().overlaps(other.getBounds());
    }
}
