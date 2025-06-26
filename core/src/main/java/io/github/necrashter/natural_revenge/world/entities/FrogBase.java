package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;

public class FrogBase extends NPC {
    SingleAnimState emergeState;

    public FrogBase(GameWorld world) {
        super(world, 1.5f, 1.5f/2f);
        deathAnim = "dive";
    }

    @Override
    public void reset() {
        dead = false;
        health = maxHealth;
    }

    @Override
    public boolean onRemove(boolean worldDisposal) {
        if (worldDisposal) return true;
        return true;
    }

    public void spawn() {
        dead = false;
        health = maxHealth;
        Vector2 point = world.inFrontOfPlayer(3f);
        setPosition(point.x, point.y);
        world.octree.add(this);
        init();
        // Look at player
        moveTo(world.player.hitBox.position, 0f);
        updateTransform();
        switchState(emergeState);
        world.playSound(Main.assets.frogEmerge, hitBox.position);
    }

    @Override
    public void die() {
        dead = true;
        world.playSound(Main.assets.frogDie, hitBox.position);
        animationController.setAnimation(deathAnim, animationListener);
    }
}
