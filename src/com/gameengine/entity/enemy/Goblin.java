package com.gameengine.entity.enemy;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.audio.Sound;
import com.gameengine.engine.world.World;
import com.gameengine.entity.MovableEntity;
import com.gameengine.entity.player.Player;
import com.gameengine.entity.projectile.Arrow;

import java.util.List;

public class Goblin extends MovableEntity {

    public enum Type {
        
        ARCHER("GoblinArcher", 600, 500, 8, 6, 9, 9, 0.13f, 500f, 1f, 6),
        SCOUT("GoblinScout", 600, 500, 8, 6, 6, 9, 0.13f, 60f, 2f, 3),
        TANK("GoblinTank", 1248, 848, 8, 6, 12, 9, 0.14f, 80f, 4f, 7);

        final String folder;
        final int frameWidth;
        final int frameHeight;
        final int idleFrames; 
        final int runFrames;
        final int attackFrames;
        final int deathFrames;
        final float scale;
        final float attackRange;
        final float damage;
        final int damageFrameIndex;

        Type(String folder, int frameWidth, int frameHeight, int idleFrames, int runFrames, int attackFrames, int deathFrames, float scale, float attackRange, float damage, int damageFrameIndex) {
            this.folder = folder;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.idleFrames = idleFrames;
            this.runFrames = runFrames;
            this.attackFrames = attackFrames;
            this.deathFrames = deathFrames;
            this.scale = scale;
            this.attackRange = attackRange;
            this.damage = damage;
            this.damageFrameIndex = damageFrameIndex;
        }
    }

    private static final java.util.Map<String, Animation<TextureRegion>> animationCache = new java.util.HashMap<>();
    private static final java.util.Map<Type, Sound> soundCache = new java.util.EnumMap<>(Type.class);

    private Type type;
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> runAnim;
    private Animation<TextureRegion> attackAnim;
    private Animation<TextureRegion> deathAnim;
    
    private float stateTimer;
    private boolean isRight;
    private World world;
    
    private float moveSpeed = 60f; 
    private static final float MOVE_DISTANCE = 320f; 
    private static final float AGGRO_RANGE = 900f;   
    private float startX;
    
    private boolean isAttacking = false;
    private float attackCooldown = 0f;
    private boolean hasDealtDamage = false;

    private float visualOffsetY;
    
    private int currentHealth;
    private int maxHealth;
    private boolean isDead = false;
    private float damageTimer = 0;
    
    private static Texture whitePixel;

    public Goblin(float x, float y, World world, SpriteBatch batch, Type type) {
        this(x, y, world, batch, type, -1);
    }

    public Goblin(float x, float y, World world, SpriteBatch batch, Type type, int customHp) {
        super(x, y + 20, type == Type.TANK ? 60 : 40, type == Type.TANK ? 80 : 60, batch); 
        this.world = world;
        this.type = type;
        this.startX = x;
        
        this.velocity = new Vector2(type == Type.SCOUT ? 100 : 60, 0); 
        this.isRight = true;
        this.stateTimer = 0;
        
        if (customHp > 0) {
            this.maxHealth = customHp;
        } else {
            if (type == Type.TANK) this.maxHealth = 50;
            else if (type == Type.SCOUT) this.maxHealth = 30;
            else this.maxHealth = 20; 
        }

        this.currentHealth = maxHealth;
        
        initAnimations();
        
        visualOffsetY = 0f; 
        
        checkSlopeAndGroundCollisions(0);

        if (whitePixel == null) {
            Pixmap p = new Pixmap(1,1, Pixmap.Format.RGBA8888);
            p.setColor(1,1,1,1);
            p.fill();
            whitePixel = new Texture(p);
            p.dispose();
        }
    }

    private void initAnimations() {
        String keyPrefix = type.name();
        if (!animationCache.containsKey(keyPrefix + "_idle")) {
            loadAndCacheAnimations();
        }
        this.idleAnim = animationCache.get(keyPrefix + "_idle");
        this.runAnim = animationCache.get(keyPrefix + "_run");
        this.attackAnim = animationCache.get(keyPrefix + "_attack");
        this.deathAnim = animationCache.get(keyPrefix + "_death");
    }

    private synchronized void loadAndCacheAnimations() {
        if (animationCache.containsKey(type.name() + "_idle")) return;

        try {
            String path = "gfx/enemies/" + type.folder + "/";
            Texture idleTex = new Texture(Gdx.files.internal(path + "idle.png"));
            Texture runTex = new Texture(Gdx.files.internal(path + "walk.png"));
            Texture attackTex = new Texture(Gdx.files.internal(path + "attack.png"));
            Texture deathTex = new Texture(Gdx.files.internal(path + "death.png"));

            Animation<TextureRegion> _idle = createAnimation(idleTex, type.frameWidth, type.frameHeight, type.idleFrames, Animation.PlayMode.LOOP);
            Animation<TextureRegion> _run = createAnimation(runTex, type.frameWidth, type.frameHeight, type.runFrames, Animation.PlayMode.LOOP);
            Animation<TextureRegion> _attack = createAnimation(attackTex, type.frameWidth, type.frameHeight, type.attackFrames, Animation.PlayMode.NORMAL); 
            Animation<TextureRegion> _death = createAnimation(deathTex, type.frameWidth, type.frameHeight, type.deathFrames, Animation.PlayMode.NORMAL);
            
            animationCache.put(type.name() + "_idle", _idle);
            animationCache.put(type.name() + "_run", _run);
            animationCache.put(type.name() + "_attack", _attack);
            animationCache.put(type.name() + "_death", _death);
        } catch (Exception e) {
            System.err.println("Asset Err: " + e.getMessage());
            Texture dummy = new Texture(1,1, Pixmap.Format.RGB888); 
            Animation<TextureRegion> dAnim = new Animation<>(1f, new TextureRegion(dummy));
            animationCache.put(type.name() + "_idle", dAnim);
            animationCache.put(type.name() + "_run", dAnim);
            animationCache.put(type.name() + "_attack", dAnim);
            animationCache.put(type.name() + "_death", dAnim);
        }
    }
    
    private boolean isFrameNotEmpty(Pixmap p, int x, int y, int w, int h) {
        
        return true;
    }

    private Animation<TextureRegion> createAnimation(Texture tex, int fw, int fh, int frameCount, Animation.PlayMode mode) {
        TextureRegion[][] tmp = TextureRegion.split(tex, fw, fh);
        
        Array<TextureRegion> frames = new Array<>();
        if (tmp.length > 0) {
            for (int i = 0; i < tmp[0].length && i < frameCount; i++) {
                frames.add(tmp[0][i]);
            }
        }
        
        if (frames.size == 0) {
             frames.add(new TextureRegion(tex));
        }

        return new Animation<>(0.11f, frames, mode);
    }

    @Override
    public void update(float deltaTime) {
        if (!active) return;
        
        stateTimer += deltaTime;
        
        if (isDead) {
            
            velocity.x = 0; 
            
            velocity.y -= 1000 * deltaTime;
            if (velocity.y < -800) velocity.y = -800;
            position.y += velocity.y * deltaTime;
            checkSlopeAndGroundCollisions(deltaTime);
            
            Player p = world.getPlayer();
            if (p != null) {
                float dist = Math.abs(p.getPosition().x - position.x);
                
                if (dist > 1500) {
                    active = false;
                }
            }
            return;
        }

        if (damageTimer > 0) damageTimer -= deltaTime;

        Player player = null;
        for (com.gameengine.engine.core.Entity e : world.getEntities()) {
            if (e instanceof Player) {
                player = (Player)e;
                break;
            }
        }

        if (player != null && !player.isDead() && player.isActive()) {
            float dist = Vector2.dst(position.x, position.y, player.getPosition().x, player.getPosition().y);
            
            if (!isAttacking && dist < type.attackRange && attackCooldown <= 0) {
                
                isRight = (player.getPosition().x > position.x);
                startAttack();
            }
            
            if (!isAttacking) {
                if (Math.abs(position.x - player.getPosition().x) < AGGRO_RANGE) {
                     float dir = (player.getPosition().x - position.x) > 0 ? 1 : -1;
                     
                     if (checkForLedge(dir)) {
                         boolean shouldMove = true;
                         
                         if (type == Type.ARCHER) {
                             if (dist < type.attackRange - 50) { 
                                 shouldMove = false; 
                             }
                         }
                         
                         velocity.x = shouldMove ? moveSpeed * dir : 0;
                         isRight = (player.getPosition().x > position.x);
                     } else {
                         velocity.x = 0; 
                         
                         isRight = (player.getPosition().x > position.x);
                     }
                } else {
                     patrol(deltaTime);
                }
            } else {
                 velocity.x = 0;
            }
        } else {
            patrol(deltaTime);
        }
        
        if (isAttacking) {
            int currentFrame = attackAnim.getKeyFrameIndex(stateTimer);
            if (currentFrame == type.damageFrameIndex && !hasDealtDamage) {
                
                playAttackSound();

                if (type == Type.ARCHER) {
                    shootArrow();
                } else {
                    if (player != null && Math.abs(player.getPosition().x - position.x) < type.attackRange) {
                        player.takeDamage((int)type.damage);
                    }
                }
                hasDealtDamage = true;
            }
            if (attackAnim.isAnimationFinished(stateTimer)) {
                isAttacking = false;
                attackCooldown = 0.5f; 
            }
        }
        
        if (attackCooldown > 0) attackCooldown -= deltaTime;

        velocity.y -= 1000 * deltaTime;
        if (velocity.y < -800) velocity.y = -800;
        
        position.x += velocity.x * deltaTime;
        checkCollisionsX(deltaTime);
        
        position.y += velocity.y * deltaTime;
        checkSlopeAndGroundCollisions(deltaTime);
    }
    
    private void patrol(float dt) {
        float dir = (velocity.x > 0) ? 1 : -1;
        
        isRight = (velocity.x > 0);
        
        if (position.x > startX + MOVE_DISTANCE) {
            velocity.x = -moveSpeed;
            isRight = false;
        } else if (position.x < startX - MOVE_DISTANCE) {
            velocity.x = moveSpeed;
            isRight = true;
        }
        
        if (!checkForLedge(dir)) {
             velocity.x = -velocity.x;
             isRight = (velocity.x > 0); 
        }
    }
    
    private boolean checkForLedge(float dir) {
        
        if (world.isSurvivalMode()) {
             
             float leadingEdgeX = position.x + (size.x/2) + (size.x/2 * dir);
             
             float lookAhead = 20f; 
             float forwardX = leadingEdgeX + (lookAhead * dir);
             
             boolean foundGround = false;
             
             for (float down = 0; down <= 80; down += 10) {
                 float cy = position.y - down;
                 for (Rectangle r : world.getCollisionRectangles()) {
                     if (r.contains(forwardX, cy)) foundGround = true;
                 }
                 
                 if (!foundGround) {
                     for (Rectangle r : world.getSlopeDownRects()) {
                         if (forwardX >= r.x && forwardX <= r.x + r.width) {
                             
                             if (cy <= r.y + r.height + 20 && cy >= r.y - 10) foundGround = true; 
                         }
                     }
                 }
                 if (!foundGround) {
                     for (Rectangle r : world.getSlopeUpRects()) {
                         if (forwardX >= r.x && forwardX <= r.x + r.width) {
                             if (cy <= r.y + r.height + 20 && cy >= r.y - 10) foundGround = true; 
                         }
                     }
                 }
                 if (foundGround) break;
             }
             
             return foundGround;
        }

        float leadingEdgeX = position.x + (size.x/2) + (size.x/2) * dir;
        
        float[] lookaheads = { 5f, 20f }; 
        
        for (float offset : lookaheads) {
             float checkX = leadingEdgeX + (offset * dir);
             boolean pointSafe = false;
             
             float[] depths = { 10f, 20f, 35f, 55f };
             
             for (float down : depths) {
                 float checkY = position.y - down;
                 
                 for (Rectangle r : world.getCollisionRectangles()) {
                     if (r.contains(checkX, checkY)) {
                         pointSafe = true;
                         break; 
                     }
                 }
                 
                 if (!pointSafe) {
                     for (Rectangle r : world.getSlopeUpRects()) {
                         if (checkX >= r.x && checkX <= r.x + r.width) {
                             if (checkY <= r.y + r.height + 15) { 
                                 pointSafe = true;
                                 break;
                             }
                         }
                     }
                 }
                 
                 if (!pointSafe) {
                     for (Rectangle r : world.getSlopeDownRects()) {
                         if (checkX >= r.x && checkX <= r.x + r.width) {
                             if (checkY <= r.y + r.height + 15) {
                                  pointSafe = true;
                                  break;
                             }
                         } 
                     }
                 }
                 
                 if (pointSafe) break; 
             }
             
             if (!pointSafe) return false; 
        }

        return true; 
    }
    
    private void startAttack() {
        isAttacking = true;
        hasDealtDamage = false;
        stateTimer = 0;
    }
    
    private void shootArrow() {
        float spawnX = isRight ? position.x + size.x : position.x - 32;
        Arrow arrow = new Arrow(spawnX, position.y + size.y/2, world, batch, isRight, (int)type.damage);
        world.addEntity(arrow);
    }

    @Override
    protected void applyGravity(float deltaTime) {
        
    }

    private void checkCollisionsX(float delta) {
        Rectangle myRect = getBounds();
        List<Rectangle> walls = world.getCollisionRectangles();
        for (Rectangle wall : walls) {
             if (myRect.overlaps(wall)) {
                  float overlapY = Math.min(myRect.y + myRect.height, wall.y + wall.height) - Math.max(myRect.y, wall.y);
                  
                  float wallTop = wall.y + wall.height;
                  float stepHeight = wallTop - position.y;
                  if (stepHeight <= 18f && stepHeight > -5f) {
                       position.y = wallTop;
                       
                       continue;
                  }

                  if (overlapY > 5) { 
                      if (velocity.x > 0) position.x = wall.x - size.x;
                      else if (velocity.x < 0) position.x = wall.x + wall.width;
                      velocity.x = -velocity.x;
                      
                      break;
                  }
             }
        }
    }

    private void checkSlopeAndGroundCollisions(float delta) {
        Rectangle myRect = getBounds();
        
        float contentGroundY = -9999f;
        boolean hitGround = false;
        
        List<Rectangle> walls = world.getCollisionRectangles();
        for (Rectangle wall : walls) {
            if (myRect.overlaps(wall)) {
                
                if (velocity.y <= 0 && myRect.y >= wall.y + 7) {
                     if (wall.y + wall.height > contentGroundY) {
                         contentGroundY = wall.y + wall.height;
                         hitGround = true;
                     }
                } else if (velocity.y > 0) {
                     
                     if (myRect.y < wall.y) {
                         position.y = wall.y - size.y;
                         velocity.y = 0;
                     }
                }
            }
        }

        float slopeY = -9999f;
        boolean hitSlope = false;
        
        float[] checkPoints = { position.x + 2, position.x + size.x / 2, position.x + size.x - 2 };
        float snapDist = 32f; 
        
        for (float cx : checkPoints) {
            
            for (Rectangle r : world.getSlopeUpRects()) {
                if (cx >= r.x && cx <= r.x + r.width) {
                     float relX = cx - r.x;
                     float target = r.y + relX;
                     
                     if (position.y <= target + snapDist && position.y >= r.y - 32) {
                         if (target > slopeY) {
                             slopeY = target;
                             hitSlope = true;
                         }
                     }
                }
            }
            
            for (Rectangle r : world.getSlopeDownRects()) {
                if (cx >= r.x && cx <= r.x + r.width) {
                     float relX = cx - r.x;
                     float target = r.y + (32 - relX);
                     if (position.y <= target + snapDist && position.y >= r.y - 32) {
                          if (target > slopeY) {
                               slopeY = target;
                               hitSlope = true;
                          }
                     }
                }
            }
        }
        
        if (hitSlope) {
             
             if (!hitGround || slopeY >= contentGroundY - 5) {
                position.y = slopeY;
                velocity.y = 0;
             } else {
                position.y = contentGroundY;
                velocity.y = 0;
             }
        } else if (hitGround) {
            position.y = contentGroundY;
            velocity.y = 0;
        }
    }

    @Override
    public void render() {
        if (!active) return;
        
        TextureRegion f;
        if (isDead) f = deathAnim.getKeyFrame(stateTimer);
        else if (isAttacking) f = attackAnim.getKeyFrame(stateTimer);
        else if (Math.abs(velocity.x) > 10) f = runAnim.getKeyFrame(stateTimer);
        else f = idleAnim.getKeyFrame(stateTimer);

        boolean flip = !isRight;
        
        float drawW = type.frameWidth * type.scale;
        float drawH = type.frameHeight * type.scale;
        
        float dx = position.x + (size.x - drawW)/2;
        float dy = position.y - visualOffsetY;
        
        batch.draw(f, 
            flip ? dx + drawW : dx, 
            dy, 
            drawW * (flip ? -1 : 1), 
            drawH);
            
        if (!isDead) {
            float barW = 40;
            float barH = 4;
            float barX = position.x + (size.x - barW) / 2;
            float barY = position.y + size.y + 10;
            
            batch.setColor(0,0,0,1);
            batch.draw(whitePixel, barX, barY, barW, barH);
            
            float pct = (float)currentHealth / maxHealth;
            if (pct < 0) pct = 0;
            batch.setColor(1,0,0,1);
            batch.draw(whitePixel, barX, barY, barW * pct, barH);
            
            batch.setColor(1,1,1,1); 
        }
            
    }
    
    public void takeDamage(int amount) {
        if (isDead) return;
        currentHealth -= amount;
        damageTimer = 0.5f; 
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            stateTimer = 0; 
            velocity.x = 0; 
            isAttacking = false; 
        }
    }
    
    public boolean isDead() { return isDead; }
    public Type getType() { return type; }

    private void playAttackSound() {
        if (!soundCache.containsKey(type)) {
            String soundFile = "";
            switch (type) {
                case ARCHER: soundFile = "sounds_mob/archer_hit.mp3"; break;
                case SCOUT:  soundFile = "sounds_mob/scout_hit.wav"; break;
                case TANK:   soundFile = "sounds_mob/tank_hit.wav"; break;
            }
            
            try {
                if (Gdx.files.internal(soundFile).exists()) {
                    Sound s = Gdx.audio.newSound(Gdx.files.internal(soundFile));
                    soundCache.put(type, s);
                }
            } catch (Exception e) {
                 System.out.println("Failed to load mob sound: " + soundFile + " " + e.getMessage());
            }
        }
        
        Sound s = soundCache.get(type);
        if (s != null) {
            float vol = 1.0f;
            try {
                com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("settings_prefs");
                vol = prefs.getFloat("volume", 1.0f) * prefs.getFloat("sfx_volume", 1.0f);
            } catch(Exception e) {}
            
            s.play(vol); 
        }
    }

    @Override
    public void dispose() {
        
    }
}
