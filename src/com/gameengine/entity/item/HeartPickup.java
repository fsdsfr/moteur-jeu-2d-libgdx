package com.gameengine.entity.item;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.gameengine.engine.core.Entity;
import com.gameengine.engine.world.World;
import com.gameengine.entity.player.Player;

public class HeartPickup extends Entity {
    
    private Texture texture;
    private World world;
    private float floatTimer;
    private float initialY;
    
    public HeartPickup(float x, float y, World world, SpriteBatch batch) {
        super(x, y, 32, 32, batch); 
        this.world = world;
        this.initialY = y;
        
        try {
            texture = new Texture(Gdx.files.internal("gfx/hp/life_04.png"));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        } catch (Exception e) {
            
            texture = new Texture(32, 32, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        }
    }

    @Override
    public void update(float deltaTime) {
        if (!active) return;
        
        floatTimer += deltaTime;
        position.y = initialY + (float)Math.sin(floatTimer * 3) * 5; 
        
        checkCollision();
    }
    
    private void checkCollision() {
        for (Entity e : world.getEntities()) {
            if (e instanceof Player) {
                Player p = (Player)e;
                if (getBounds().overlaps(p.getBounds())) {
                    
                    if (p.getHealth() < p.getMaxHealth()) {
                        p.heal(4);
                        active = false;
                        world.removeEntity(this);
                    }
                }
            }
        }
    }

    @Override
    public void render() {
        if (active) {
            batch.draw(texture, position.x, position.y, size.x, size.y);
        }
    }
    
    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
