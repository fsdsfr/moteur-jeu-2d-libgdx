package com.gameengine.entity.player;

public class PlayerConfig {
    
    private float width;
    private float height;
    private float maxMovementSpeed;
    private float movementAcceleration;
    private float gravityStrength;
    private float maxFallSpeed;
    private float jumpForce;
    private float friction;
    
    public PlayerConfig() {
        
        this.width = 32;
        this.height = 48;
        this.maxMovementSpeed = 280;
        this.movementAcceleration = 20;
        this.gravityStrength = 1500;
        this.maxFallSpeed = 500;
        this.jumpForce = 400;
        this.friction = 0.8f;
    }
    
    public float getWidth() {
        return width;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    public float getMaxMovementSpeed() {
        return maxMovementSpeed;
    }
    
    public void setMaxMovementSpeed(float speed) {
        this.maxMovementSpeed = speed;
    }
    
    public float getMovementAcceleration() {
        return movementAcceleration;
    }
    
    public void setMovementAcceleration(float acceleration) {
        this.movementAcceleration = acceleration;
    }
    
    public float getGravityStrength() {
        return gravityStrength;
    }
    
    public void setGravityStrength(float gravity) {
        this.gravityStrength = gravity;
    }
    
    public float getMaxFallSpeed() {
        return maxFallSpeed;
    }
    
    public void setMaxFallSpeed(float speed) {
        this.maxFallSpeed = speed;
    }
    
    public float getJumpForce() {
        return jumpForce;
    }
    
    public void setJumpForce(float force) {
        this.jumpForce = force;
    }
    
    public float getFriction() {
        return friction;
    }
    
    public void setFriction(float friction) {
        this.friction = friction;
    }
}
