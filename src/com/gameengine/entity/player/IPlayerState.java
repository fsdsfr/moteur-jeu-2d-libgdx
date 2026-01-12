package com.gameengine.entity.player;

public interface IPlayerState {
    
    IPlayerState doState(Player player, float deltaTime);
    
    String getName();
}
