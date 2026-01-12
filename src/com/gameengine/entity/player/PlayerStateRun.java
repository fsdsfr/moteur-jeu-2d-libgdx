package com.gameengine.entity.player;

public class PlayerStateRun implements IPlayerState {
    
    @Override
    public IPlayerState doState(Player player, float deltaTime) {
        
        float direction = player.getDirectionX();
        if (direction != 0) {
            player.moveWithAcceleration(direction, deltaTime);
        }
        
        if (player.getDirectionX() == 0) {
            
            return player.getIdleState();
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
        return "Run";
    }
}
