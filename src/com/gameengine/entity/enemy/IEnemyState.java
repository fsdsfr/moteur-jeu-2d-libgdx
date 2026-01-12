package com.gameengine.entity.enemy;

public interface IEnemyState {
    
    IEnemyState doState(Enemy enemy, float deltaTime);
    
    String getName();
}
