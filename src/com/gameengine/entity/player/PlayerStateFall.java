package com.gameengine.entity.player;

public class PlayerStateFall implements IPlayerState {
    
    @Override
    public IPlayerState doState(Player player, float deltaTime) {
        
        float direction = player.getDirectionX();
        if (direction != 0) {
            player.moveWithAcceleration(direction, deltaTime);
        }
        
        if (player.isOnGround()) {
            
            if (player.getDirectionX() != 0) {
                return player.getRunState();
            } else {
                return player.getIdleState();
            }
        }
        
        return this;
    }
    
    @Override
    public String getName() {
        return "Fall";
    }
}
