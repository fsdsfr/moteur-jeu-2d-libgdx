package com.gameengine.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.gameengine.engine.core.Entity;

public abstract class MovableEntity extends Entity {
    
    protected Vector2 velocity;
    protected float gravityStrength;
    protected boolean affectedByGravity;
    
    public MovableEntity(float x, float y, float width, float height, SpriteBatch batch) {
        super(x, y, width, height, batch);
        this.velocity = new Vector2(0, 0);
        this.gravityStrength = 1500;
        this.affectedByGravity = true;
    }
    
    protected void applyGravity(float deltaTime) {
        if (affectedByGravity) {
            velocity.y -= gravityStrength * deltaTime;
        }
    }
    
    protected void move(float deltaTime) {
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
    }
    
    public Vector2 getVelocity() {
        return velocity.cpy();
    }
    
    public void setVelocity(Vector2 vel) {
        this.velocity = vel.cpy();
    }
    
    public float getGravityStrength() {
        return gravityStrength;
    }
    
    public void setGravityStrength(float gravity) {
        this.gravityStrength = gravity;
    }
    
    public boolean isAffectedByGravity() {
        return affectedByGravity;
    }
    
    public void setAffectedByGravity(boolean affected) {
        this.affectedByGravity = affected;
    }
}
