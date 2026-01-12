package com.gameengine.entity.player;

public class PlayerStateIdle implements IPlayerState {
    
    @Override
    public IPlayerState doState(Player player, float deltaTime) {
        
        player.applyFriction(deltaTime);
        
        if (player.getDirectionX() != 0) {
            
            return player.getRunState();
        }
        
        if (!player.isOnGround()) {
            
            if (player.getVelocity().y > 0) {
                return player.getJumpState();
            } else {
                return player.getFallState();
            }
        }
        
        return this;
    }
    
    @Override
    public String getName() {
        return "Idle";
    }
}
