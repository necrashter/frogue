package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;

public class Zombie extends NPC {
    SingleAnimState emergeState;
    PursueToStrike pursueToStrike;

    public Zombie(GameWorld world) {
        super(world);
        modelInstance = new ModelInstance(Main.assets.npcModel, "ManArmature", "ZombieMesh");
        modelInstance.transform.setToTranslation(hitBox.position);
        animationController = new AnimationController(modelInstance);

        // States
        pursueToStrike = new PursueToStrike("zombie-walk", 2.5f) {
            @Override
            public void onStrikeBegin() {
                world.playSound(Main.assets.zombieSounds[MathUtils.random.nextInt(Main.assets.frogSounds.length)], hitBox.position, 3f);
            }
        };
        pursueToStrike.strikeAnim = "zombie-strike";
        deathAnim = "zombie-die";
        pursueToStrike.prepare(world.player);
        emergeState = new SingleAnimState("zombie-emerge", pursueToStrike);

        maxHealth = 30f;
        health = maxHealth;

        switchState(emergeState);
    }

    @Override
    public void reset() {
        dead = false;
        health = maxHealth;
    }

    public void spawn(float h, boolean running) {
        if (running) {
            pursueToStrike.movementSpeed = 5f;
            pursueToStrike.movementAnim = "run";
        } else {
            pursueToStrike.movementSpeed = 2.5f;
            pursueToStrike.movementAnim = "zombie-walk";
        }
        maxHealth = h;
        health = maxHealth;
        Vector2 point = world.randomPointNearPlayer(10.f);
        setPosition(point.x, point.y);
        world.octree.add(this);
        init();
        updateTransform();
        switchState(emergeState);
        world.playSound(Main.assets.zombieEmerge, hitBox.position, 0.25f);
    }

    @Override
    public void die() {
        dead = true;
        world.playSound(Main.assets.zombieDieSounds[MathUtils.random.nextInt(Main.assets.frogSounds.length)], hitBox.position, 3f);
        animationController.setAnimation(deathAnim, animationListener);
    }
    public static class Pool extends com.badlogic.gdx.utils.Pool<Zombie> {
        final GameWorld level;

        public Pool(GameWorld level, int limit) {
            super(limit, limit);
            this.level = level;
            fill(limit);
        }

        @Override
        protected Zombie newObject() {
            return new Zombie(level) {
                @Override
                public boolean onRemove(boolean worldDisposal) {
                    boolean out = super.onRemove(worldDisposal);
                    free(this);
                    return out;
                }
            };
        }

        public int getAlive() {
            return max - getFree();
        }

        public void spawn(int walkingCount, int runningCount, float health) {
            for (int i = 0; i < walkingCount; ++i) {
                obtain().spawn(health, false);
            }
            for (int i = 0; i < runningCount; ++i) {
                obtain().spawn(health, true);
            }
        }

        public void spawn(int count, float health) {
            for (int i = 0; i < count; ++i) {
                obtain().spawn(health);
            }
        }
    }
}
