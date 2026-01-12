package com.gameengine.entity.player;

public class PlayerStateAttackHeavy implements IPlayerState {

    @Override
    public IPlayerState doState(Player player, float deltaTime) {
        
        player.applyFriction(deltaTime);

        float timer = player.getStateTimer();
        if (timer > 0.3f && !player.hasDealtDamage()) {
             player.checkMeleeAttack(90f, 20); 
             player.setHasDealtDamage(true);
        }
        
        if (player.isAnimationFinished()) {
            return player.getIdleState();
        }
        
        return this;
    }

    @Override
    public String getName() {
        return "AttackHeavy";
    }
}
