package com.gameengine.entity.projectile;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.Gdx;
import com.gameengine.entity.MovableEntity;
import com.gameengine.engine.world.World;
import com.gameengine.entity.player.Player;

public class Arrow extends MovableEntity {
    
    private World world;
    private Texture texture;
    private boolean isRight;
    private float lifeTime = 0;
    private static final float MAX_LIFETIME = 5.0f;
    private int damage;
    
    public Arrow(float x, float y, World world, SpriteBatch batch, boolean isRight, int damage) {
        super(x, y, 32, 10, batch); 
        this.world = world;
        this.isRight = isRight;
        this.damage = damage;
        
        try {
            if (Gdx.files.internal("gfx/enemies/Arrow.png").exists()) {
                texture = new Texture(Gdx.files.internal("gfx/enemies/Arrow.png"));
            } else {
                 
                 texture = new Texture(Gdx.files.internal("gfx/enemies/arrow.png"));
            }
        } catch (Exception e) {
             System.err.println("Error loading Arrow texture: " + e.getMessage());
             
             com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 10, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
             pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
             pixmap.fill();
             texture = new Texture(pixmap);
             pixmap.dispose();
        }
        
        this.velocity.x = isRight ? 400f : -400f;
        this.velocity.y = 0;
        this.affectedByGravity = false;
        this.gravityStrength = 0;
    }

    @Override
    public void update(float deltaTime) {
        super.move(deltaTime);
        lifeTime += deltaTime;
        
        if (lifeTime > MAX_LIFETIME) {
            this.active = false;
            return;
        }
        
        Rectangle bounds = getBounds();
        for (Rectangle wall : world.getCollisionRectangles()) {
            if (bounds.overlaps(wall)) {
                this.active = false;
                return;
            }
        }
        
        for (com.gameengine.engine.core.Entity entity : world.getEntities()) {
            if (entity instanceof Player) {
                 Player player = (Player) entity;
                 if (player.isActive() && bounds.overlaps(player.getBounds())) {
                     player.takeDamage(damage);
                     this.active = false;
                     return;
                 }
            }
        }
    }

    @Override
    public void render() {
        if (!active) return;
        
        batch.draw(new TextureRegion(texture), 
            position.x, position.y, 
            size.x/2f, size.y/2f, 
            size.x, size.y, 
            isRight ? 1 : -1, 1, 
            0);
    }

    @Override
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
