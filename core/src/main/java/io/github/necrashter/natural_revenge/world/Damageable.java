package io.github.necrashter.natural_revenge.world;

public interface Damageable {
    enum DamageSource {
        Melee,
        Firearm, FrogParticle,
    }
    enum DamageAgent {
        Player,
        NPC,
    }


    boolean takeDamage(float amount, DamageAgent agent, DamageSource source);
}
