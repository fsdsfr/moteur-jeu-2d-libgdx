package com.gameengine.entity.player;

public class PlayerStateAttackLight implements IPlayerState {

    @Override
    public IPlayerState doState(Player player, float deltaTime) {
        
        player.applyFriction(deltaTime);
        
        float timer = player.getStateTimer();
        if (timer > 0.1f && !player.hasDealtDamage()) {
             player.checkMeleeAttack(60f, 10); 
             player.setHasDealtDamage(true);
        }
        
        if (player.isAnimationFinished()) {
            return player.getIdleState();
        }
        
        return this;
    }

    @Override
    public String getName() {
        return "AttackLight";
    }
}
