package com.gameengine.entity.enemy;

import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.gameengine.engine.world.World;
import com.gameengine.entity.MovableEntity;

public class Enemy extends MovableEntity {
    
    private Texture texture;
    private Animation<TextureRegion> runAnim;
    private float stateTimer;
    private boolean isRight;
    private World world;
    
    private float moveSpeed = 100f;
    
    private static final float ENEMY_WIDTH = 64f;
    private static final float ENEMY_HEIGHT = 42f; 
    
    public Enemy(float x, float y, World world, SpriteBatch batch) {
        super(x, y, ENEMY_WIDTH, ENEMY_HEIGHT, batch); 
        this.world = world;
        this.velocity = new Vector2(moveSpeed, 0); 
        this.isRight = true;
        this.stateTimer = 0;
        
        initAnimations();
    }

    private void initAnimations() {
        try {
            
            texture = new Texture("gfx/enemies/GoblinScout/walk.png"); 
            
            TextureRegion[][] tmp = TextureRegion.split(texture, 600, 500);
            
            Array<TextureRegion> frames = new Array<>();
            for (int i = 0; i < tmp[0].length; i++) {
                frames.add(tmp[0][i]);
            }
            runAnim = new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);
        } catch (Exception e) {
            System.err.println("Could not load enemy texture: " + e.getMessage());
        }
    }

    @Override
    public void update(float deltaTime) {
        if (!active) return;
        
        stateTimer += deltaTime;
        
        applyGravity(deltaTime);
        
        float dir = (velocity.x > 0) ? 1 : -1;
        if (!checkForLedge(dir)) {
             reverseVelocity();
        }

        position.x += velocity.x * deltaTime;
        checkCollisionsX(deltaTime);
        
        position.y += velocity.y * deltaTime;
        checkCollisionsY(deltaTime);
        
        if (velocity.x > 0) isRight = true;
        else isRight = false;
    }

    private boolean checkForLedge(float dir) {
        
        float checkY = position.y - 20; 
        
        float leadingEdgeX = position.x + (size.x/2) + (size.x/2) * dir;
        
        float checkX = leadingEdgeX + (10f * dir);
        
        boolean pointSafe = false;
        for (Rectangle r : world.getCollisionRectangles()) {
            if (r.contains(checkX, checkY)) pointSafe = true;
        }
        if (!pointSafe) {
             for (Rectangle r : world.getSlopeUpRects()) {
                 if (checkX >= r.x && checkX <= r.x + r.width) {
                     if (checkY <= r.y + r.height + 10) pointSafe = true;
                 }
             }
        }
        if (!pointSafe) {
             for (Rectangle r : world.getSlopeDownRects()) {
                 if (checkX >= r.x && checkX <= r.x + r.width) {
                     if (checkY <= r.y + r.height + 10) pointSafe = true;
                 }
             }
        }
        
        return pointSafe;
    }

    private void checkCollisionsX(float delta) {
        Rectangle myRect = getBounds();
        List<Rectangle> walls = world.getCollisionRectangles();

        for (Rectangle wall : walls) {
             if (myRect.overlaps(wall)) {
                  if (velocity.x > 0) position.x = wall.x - size.x;
                  else if (velocity.x < 0) position.x = wall.x + wall.width;
                  reverseVelocity();
                  break;
             }
        }
    }

    private void reverseVelocity() {
        velocity.x = -velocity.x;

        if (velocity.x > 0) position.x += 1;
        else position.x -= 1;
    }

    private void checkCollisionsY(float delta) {
        Rectangle myRect = getBounds();
        List<Rectangle> walls = world.getCollisionRectangles();
        boolean onGround = false;

        for (Rectangle wall : walls) {
            if (myRect.overlaps(wall)) {
                if (velocity.y < 0) {
                    position.y = wall.y + wall.height;
                    velocity.y = 0;
                    onGround = true;
                } else if (velocity.y > 0) {
                    position.y = wall.y - size.y;
                    velocity.y = 0;
                }
                break;
            }
        }

    }

    @Override
    public void render() {
        if (runAnim == null) return;
        
        TextureRegion currentFrame = runAnim.getKeyFrame(stateTimer);
        
        float drawWidth = 64f; 
        float drawHeight = 64f;
        
        float drawX = position.x + (size.x - drawWidth) / 2;
        
        float drawY = position.y; 
        
        float scaleX = (velocity.x > 0) ? 1f : -1f;
        
        float originX = drawWidth / 2f;
        float originY = drawHeight / 2f;
        
        batch.draw(currentFrame, 
            drawX, drawY, 
            originX, originY, 
            drawWidth, drawHeight, 
            scaleX, 1f, 
            0);
    }

    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
