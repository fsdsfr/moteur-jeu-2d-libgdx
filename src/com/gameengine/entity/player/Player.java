package com.gameengine.entity.player;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.gameengine.engine.core.Entity;
import com.gameengine.engine.world.World;
import com.badlogic.gdx.math.Rectangle;
import com.gameengine.entity.enemy.Goblin;
import com.badlogic.gdx.Gdx;
import java.util.List;

public class Player extends Entity {

    private Vector2 velocity;
    private IPlayerState currentState;
    private PlayerConfig config;
    private World world;
    
    private Texture idleTexture;
    private Texture runTexture;
    private Texture deathTexture;
    private Texture attackLightTexture;
    private Texture attackHeavyTexture;
    
    private com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> idleAnim;
    private com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> runAnim;
    private com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> jumpAnim;
    private com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> fallAnim;
    private com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> initJumpAnim;
    private com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> deathAnim;
    private com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> attackLightAnim;
    private com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion> attackHeavyAnim;
    
    private float stateTimer;
    private boolean isRight;

    private boolean isOnGround;
    private float directionX;
    private float directionY;
    private float visualWidth;
    private float visualHeight;

    private int maxHealth = 12; 
    private int currentHealth;
    private float damageTimer = 0;
    private boolean isDamaged = false;
    private static final float DAMAGE_COOLDOWN = 1.0f; 
    
    private boolean isDead = false;

    private PlayerStateIdle idleState;
    private PlayerStateRun runState;
    private PlayerStateJump jumpState;
    private PlayerStateFall fallState;
    private PlayerStateAttackLight attackLightState;
    private PlayerStateAttackHeavy attackHeavyState;
    
    private float coyoteTimeCounter = 0;
    private static final float COYOTE_TIME = 0.15f; 

    private float jumpBufferCounter = 0;
    private static final float JUMP_BUFFER_TIME = 0.1f; 
    
    private boolean attackDamageDealt = false;

    public Player(float x, float y, SpriteBatch batch, PlayerConfig config, World world) {
        super(x, y, config.getWidth(), config.getHeight(), batch);
        this.config = config;
        this.world = world;
        
        this.velocity = new Vector2(0, 0);
        this.isRight = true;
        this.stateTimer = 0;
        
        this.currentHealth = maxHealth;
        
        idleState = new PlayerStateIdle();
        runState = new PlayerStateRun();
        jumpState = new PlayerStateJump();
        fallState = new PlayerStateFall();
        attackLightState = new PlayerStateAttackLight();
        attackHeavyState = new PlayerStateAttackHeavy();
        
        currentState = idleState;
        
        initAnimations();
    }

    public PlayerConfig getConfig() {
        return config;
    }
    
    private com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> createAnimation(Texture tex, int expectedFrames, float duration) {
        
        int frameWidth = 48; 
        int frameHeight = 48;
        
        TextureRegion[][] tmp = TextureRegion.split(tex, frameWidth, frameHeight);
        
        int rows = tmp.length;
        int cols = tmp[0].length;
        int maxFrames = Math.min(expectedFrames, rows * cols);
        
        TextureRegion[] framesArr = new TextureRegion[maxFrames];
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (index < maxFrames) {
                    framesArr[index++] = tmp[i][j];
                }
            }
        }
        
        return new com.badlogic.gdx.graphics.g2d.Animation<>(duration, framesArr);
    }
    
    private void initAnimations() {
         try {
             
             idleTexture = new Texture(Gdx.files.internal("gfx/player/Player_idle.png"));
             runTexture = new Texture(Gdx.files.internal("gfx/player/Player_run.png"));
             deathTexture = new Texture(Gdx.files.internal("gfx/player/Player_death.png"));
             attackLightTexture = new Texture(Gdx.files.internal("gfx/player/Player_attack_fast.png"));
             attackHeavyTexture = new Texture(Gdx.files.internal("gfx/player/Player_attack_long.png"));
             
             idleAnim = createAnimation(idleTexture, 4, 0.15f);
             runAnim = createAnimation(runTexture, 4, 0.1f);
             
             deathAnim = createAnimation(deathTexture, 5, 0.15f);
             
             attackLightAnim = createAnimation(attackLightTexture, 4, 0.08f);
             attackHeavyAnim = createAnimation(attackHeavyTexture, 4, 0.12f);
             
             jumpAnim = runAnim;
             fallAnim = runAnim;
             
             visualWidth = 144;
             visualHeight = 144;
             
         } catch (Exception e) {
             System.err.println("Error init animations: " + e.getMessage());
         }
    }

    @Override
    public void update(float deltaTime) {
        if (!active) return;
        
        if (isDead) {
            stateTimer += deltaTime;
            if (deathAnim.isAnimationFinished(stateTimer)) {
                
            }
            return;
        }

        if (isDamaged) {
            damageTimer -= deltaTime;
            if (damageTimer <= 0) {
                isDamaged = false;
            }
        }

        if (isOnGround) {
            coyoteTimeCounter = COYOTE_TIME;
        } else {
            coyoteTimeCounter -= deltaTime;
        }

        if (jumpBufferCounter > 0) {
            jumpBufferCounter -= deltaTime;
        }

        IPlayerState nextState = currentState.doState(this, deltaTime);
        if (nextState != currentState) {
            currentState = nextState;
            stateTimer = 0;
            attackDamageDealt = false; 
        }
        
        applyGravity(deltaTime);
        
        velocity.y = Math.max(velocity.y, -config.getMaxFallSpeed()); 
        
        position.x += velocity.x * deltaTime;
        checkCollisionsX();
        
        position.y += velocity.y * deltaTime;
        checkCollisionsY();
        checkSlopeCollisions(); 

        if (velocity.x > 0) isRight = true;
        else if (velocity.x < 0) isRight = false;
        
        stateTimer += deltaTime;
        
        if (position.y < -500) {
            
            takeDamage(999);
        }
    }

    public void performLightAttack() {
        if (!isAttacking() && !isDead) {
            currentState = attackLightState;
            stateTimer = 0;
            velocity.x = 0; 
            attackDamageDealt = false; 
        }
    }

    public void performHeavyAttack() {
        if (!isAttacking() && !isDead) {
            currentState = attackHeavyState;
            stateTimer = 0;
            velocity.x = 0;
            attackDamageDealt = false; 
        }
    }

    public boolean hasDealtDamage() {
        return attackDamageDealt;
    }
    public void setHasDealtDamage(boolean v) {
        this.attackDamageDealt = v;
    }

    public void checkMeleeAttack(float range, int damage) {
        
        float hitX = isRight ? position.x + (size.x/2) : position.x + (size.x/2) - range;
        
        float hitY = position.y - 10; 
        float hitHeight = size.y + 20;
        
        Rectangle hitRect = new Rectangle(hitX, hitY, range, hitHeight);
        
        List<Entity> entities = world.getEntities();
        for (Entity e : entities) {
            if (e instanceof com.gameengine.entity.enemy.Enemy || e instanceof Goblin) {
                
                 if (e.isActive() && hitRect.overlaps(e.getBounds())) {
                     
                     if (e instanceof com.gameengine.entity.enemy.Enemy) {
                         
                     } else if (e instanceof Goblin) {
                         if (((Goblin)e).isDead()) continue; 
                         ((Goblin)e).takeDamage(damage);
                     }
                 }
            }
        }
        
    }

    public boolean isAttacking() {
        return currentState == attackLightState || currentState == attackHeavyState;
    }

    public boolean isAnimationFinished() {
        if (currentState == attackLightState) {
            return attackLightAnim.isAnimationFinished(stateTimer);
        }
        if (currentState == attackHeavyState) {
            return attackHeavyAnim.isAnimationFinished(stateTimer);
        }
        return false;
    }

    public float getStateTimer() { return stateTimer; }

    public PlayerStateIdle getIdleState() { return idleState; }
    public PlayerStateRun getRunState() { return runState; }
    public PlayerStateJump getJumpState() { return jumpState; }
    public PlayerStateFall getFallState() { return fallState; }

    public void takeDamage(int amount) {
        if (isDead || isDamaged) return;
        
        currentHealth -= amount;
        isDamaged = true;
        damageTimer = DAMAGE_COOLDOWN;
        
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            
        }
        
        if (velocity.x == 0) velocity.x = isRight ? -200 : 200;
        else velocity.x = -velocity.x * 1.5f;
        velocity.y = 200;
    }

    public int getHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    
    public void heal(int amount) {
        if (isDead) return;
        currentHealth += amount;
        if (currentHealth > maxHealth) currentHealth = maxHealth;
    }
    
    public void applyFriction(float delta) {
        
        if (isOnGround) {
            float friction = config.getFriction();
            if (friction <= 0) friction = 0.9f;
            
             if (Math.abs(velocity.x) < 50) {
                 velocity.x = 0;
                 return;
             }

            float brakingForce = 3000f; 
            if (velocity.x > 0) {
                velocity.x -= brakingForce * delta; 
                if (velocity.x < 0) velocity.x = 0;
            } else if (velocity.x < 0) {
                velocity.x += brakingForce * delta;
                if (velocity.x > 0) velocity.x = 0;
            }
        } else {
            
            float airFriction = 500f;
            if (velocity.x > 0) {
                velocity.x -= airFriction * delta;
                if (velocity.x < 0) velocity.x = 0;
            } else if (velocity.x < 0) {
                velocity.x += airFriction * delta;
                if (velocity.x > 0) velocity.x = 0;
            }
        }
    }

    public void moveWithAcceleration(float dirX, float delta) {
        velocity.x += dirX * config.getMovementAcceleration() * 60 * delta; 
        
        if (velocity.x > config.getMaxMovementSpeed()) velocity.x = config.getMaxMovementSpeed();
        if (velocity.x < -config.getMaxMovementSpeed()) velocity.x = -config.getMaxMovementSpeed();
    }
    
    protected void applyGravity(float delta) {
        velocity.y -= config.getGravityStrength() * delta;
    }
    
    private void checkCollisionsX() {
        Rectangle myRect = getBounds();
        List<Rectangle> walls = world.getCollisionRectangles();

        for (Rectangle wall : walls) {
            if (myRect.overlaps(wall)) {
                
                float wallTop = wall.y + wall.height;
                float stepHeight = wallTop - position.y;
                
                if (stepHeight <= 18f && stepHeight > -5f) {
                     
                     position.y = wallTop;
                     isOnGround = true;
                     
                     continue;
                }

                if (velocity.x > 0) {
                    position.x = wall.x - size.x;
                } else if (velocity.x < 0) {
                    position.x = wall.x + wall.width;
                }
                velocity.x = 0;
                break; 
            }
        }
    }

    private void checkCollisionsY() {
        Rectangle myRect = getBounds();
        List<Rectangle> walls = world.getCollisionRectangles();
        isOnGround = false;

        for (Rectangle wall : walls) {
            if (myRect.overlaps(wall)) {
                if (velocity.y < 0) {
                    
                    position.y = wall.y + wall.height;
                    isOnGround = true;
                    velocity.y = 0;
                } else if (velocity.y > 0) {
                    
                    position.y = wall.y - size.y;
                    velocity.y = 0;
                }
                break;
            }
        }
    }
    
    private void checkSlopeCollisions() {
        if (velocity.y > 0) return; 
        
        float pCenterX = position.x + size.x / 2;
        float pBottomY = position.y;
        float snapTolerance = 25f; 
        
        for (Rectangle rect : world.getSlopeUpRects()) {
            if (pCenterX >= rect.x && pCenterX <= rect.x + rect.width) {
                float slopeY = rect.y + (pCenterX - rect.x);
                if (pBottomY <= slopeY + snapTolerance && pBottomY >= rect.y - 35f) { 
                    position.y = slopeY;
                    velocity.y = 0;
                    isOnGround = true;
                }
            }
        }
        
        for (Rectangle rect : world.getSlopeDownRects()) {
            if (pCenterX >= rect.x && pCenterX <= rect.x + rect.width) {
                float slopeY = rect.y + rect.height - (pCenterX - rect.x);
                if (pBottomY <= slopeY + snapTolerance && pBottomY >= rect.y - 35f) {
                    position.y = slopeY;
                    velocity.y = 0;
                    isOnGround = true;
                }
            }
        }
    }
    
    public void jump() {
        
        if (isOnGround || coyoteTimeCounter > 0) {
            velocity.y = config.getJumpForce();
            isOnGround = false;
            coyoteTimeCounter = 0;
            currentState = jumpState; 
        } else {
            
            jumpBufferCounter = JUMP_BUFFER_TIME;
        }
    }
    
    public void checkJumpBuffer() {
         if (isOnGround && jumpBufferCounter > 0) {
              jump();
              jumpBufferCounter = 0;
         }
    }

    @Override
    public void render() {
        if (!active) return;

        TextureRegion frame = null;
        
        if (isDead) {
            frame = deathAnim.getKeyFrame(stateTimer, false);
        } else if (currentState == attackLightState) {
            frame = attackLightAnim.getKeyFrame(stateTimer, false);
        } else if (currentState == attackHeavyState) {
            frame = attackHeavyAnim.getKeyFrame(stateTimer, false);
        } else if (currentState == jumpState) {
            frame = jumpAnim.getKeyFrame(stateTimer, false);
        } else if (currentState == fallState) {
            frame = fallAnim.getKeyFrame(stateTimer, true);
        } else if (currentState == runState) {
            frame = runAnim.getKeyFrame(stateTimer, true);
        } else {
            frame = idleAnim.getKeyFrame(stateTimer, true);
        }
        
        if (frame == null) return;

        boolean flipX = !isRight;
        
        float x = position.x - 56f;
        
        float y = position.y; 
        
        batch.draw(frame, 
            x, y, 
            visualWidth / 2f, visualHeight / 2f, 
            visualWidth, visualHeight, 
            flipX ? -1 : 1, 1, 
            0);
            
    }

    @Override
    public void dispose() {
        if (idleTexture != null) idleTexture.dispose();
        if (runTexture != null) runTexture.dispose();
        
    }
    
    public void setDirectionX(float v) { this.directionX = v; }
    public float getDirectionX() { return directionX; }

    public Vector2 getVelocity() { return velocity; }
    public void setVelocityX(float x) { this.velocity.x = x; }
    public void setVelocityY(float y) { this.velocity.y = y; }
    public boolean isOnGround() { return isOnGround; }
    public void setOnGround(boolean onGround) { this.isOnGround = onGround; }

    public boolean isDead() { return isDead; }

}
