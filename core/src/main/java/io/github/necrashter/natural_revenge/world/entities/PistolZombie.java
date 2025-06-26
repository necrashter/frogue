package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pool;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;

public class PistolZombie extends NPC implements Pool.Poolable {
    PursueEntity pursueEntity;
    PistolShoot shootOnce;

    public PistolZombie(GameWorld world) {
        super(world);
        modelInstance = new ModelInstance(Main.assets.npcModel, "ManArmature", "ZombieMesh", "pistol");
        modelInstance.transform.setToTranslation(hitBox.position);
        animationController = new AnimationController(modelInstance);

        // States
        pursueEntity = new PursueEntity(true);
        pursueEntity.targetEntity = world.player;
//        pursueEntity.checkRay = true;
        shootOnce = new PistolShoot();
        shootOnce.target = world.player;

        // Transitions
        pursueEntity.onReached = shootOnce;
        shootOnce.onEnd = pursueEntity;

        maxHealth = 20f;
        health = maxHealth;

        reset();
    }

    @Override
    public void reset() {
        super.reset();
        pursueEntity.desiredDistance = 6.0f + MathUtils.random()*3.0f;
    }

    @Override
    public void spawn(float newHealth) {
        super.spawn(newHealth);
        switchState(pursueEntity);
    }

    public static class Pool extends com.badlogic.gdx.utils.Pool<PistolZombie> {
        final GameWorld level;

        public Pool(GameWorld level, int limit) {
            super(limit, limit);
            this.level = level;
            fill(limit);
        }

        @Override
        protected PistolZombie newObject() {
            return new PistolZombie(level) {
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

        public void spawn(int count) {
            for (int i = 0; i < count; ++i) {
                obtain().spawn();
            }
        }

        public void spawn(int count, float health) {
            for (int i = 0; i < count; ++i) {
                obtain().spawn(health);
            }
        }
    }
}
