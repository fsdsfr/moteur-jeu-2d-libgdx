package com.gameengine.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.gameengine.engine.core.Entity;

public class Portal extends Entity {
    
    public enum PortalType {
        ENTRANCE, 
        EXIT      
    }
    
    private Animation<TextureRegion> idleAnimation;
    private float stateTime;
    private boolean active;
    private PortalType type;
    
    private String nextMap = null;

    public void setNextMap(String mapName) {
        this.nextMap = mapName;
    }

    public String getNextMap() {
        return this.nextMap;
    }
    
    private static final int FRAME_WIDTH = 64; 
    
    public Portal(float x, float y, float width, float height, SpriteBatch batch, PortalType type) {
        super(x, y, width, height, batch);
        this.active = true; 
        this.type = type;
        init();
    }
    
    public Portal(float x, float y, SpriteBatch batch) {
         this(x, y, 64, 128, batch, PortalType.ENTRANCE);
    }

    private void init() {
        try {
            com.badlogic.gdx.utils.Array<TextureRegion> frames = new com.badlogic.gdx.utils.Array<>();
            
            if (type == PortalType.ENTRANCE) {
                
                for (int i = 0; i <= 2; i++) {
                    String path = "portal/portal_0" + i + ".png";
                    if (Gdx.files.internal("gfx/" + path).exists()) {
                         Texture t = new Texture(Gdx.files.internal("gfx/" + path));
                         t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                         frames.add(new TextureRegion(t));
                    }
                }
            } else {
                
                for (int i = 3; i <= 5; i++) {
                    String path = "portal/portal_0" + i + ".png";
                    if (Gdx.files.internal("gfx/" + path).exists()) {
                         Texture t = new Texture(Gdx.files.internal("gfx/" + path));
                         t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                         frames.add(new TextureRegion(t));
                    }
                }
            }
            
            if (frames.size > 0) {
                idleAnimation = new Animation<>(0.15f, frames, Animation.PlayMode.LOOP);
            } else {
                System.err.println("No portal frames loaded for type: " + type);
            }
            
            stateTime = 0f;
            
        } catch (Exception e) {
            System.err.println("Failed to load portal texture: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void update(float delta) {
        if (active) {
            stateTime += delta;
        }
    }
    
    @Override
    public void render() {
        if (active && idleAnimation != null && batch != null) {
            TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime, true);
            
            batch.draw(currentFrame, position.x, position.y, size.x, size.y);
        }
    }

    public void render(SpriteBatch batch) {
        if (active && idleAnimation != null) {
            TextureRegion currentFrame = idleAnimation.getKeyFrame(stateTime, true);
            batch.draw(currentFrame, position.x, position.y, size.x, size.y);
        }
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public boolean isActive() {
        return active;
    }
    
    @Override
    public Rectangle getBounds() {
        
        return new Rectangle(position.x + 10, position.y, size.x - 20, size.y);
    }

    public void dispose() {
        
    }
}
