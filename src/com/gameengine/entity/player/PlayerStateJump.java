package com.gameengine.entity.player;

public class PlayerStateJump implements IPlayerState {
    
    @Override
    public IPlayerState doState(Player player, float deltaTime) {
        
        float direction = player.getDirectionX();
        if (direction != 0) {
            player.moveWithAcceleration(direction, deltaTime);
        }
        
        if (player.getVelocity().y <= 0) {
            
            return player.getFallState();
        }
        
        return this;
    }
    
    @Override
    public String getName() {
        return "Jump";
    }
}
