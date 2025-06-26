package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.objects.RandomGunPickup;

public class RifleZombie extends NPC implements Pool.Poolable {
    PursueEntity pursueEntity;
    RifleShoot shootOnce;

    public RifleZombie(GameWorld world) {
        super(world);
        modelInstance = new ModelInstance(Main.assets.npcModel, "ManArmature", "ZombieMesh", "M4A1");
        modelInstance.transform.setToTranslation(hitBox.position);
        animationController = new AnimationController(modelInstance);

        muzzlePoint.set(autoRifleMuzzlePoint);

        // States
        pursueEntity = new PursueEntity(true);
        pursueEntity.targetEntity = world.player;
//        pursueEntity.checkRay = true;
        shootOnce = new RifleShoot();
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
        dead = false;
        pursueEntity.desiredDistance = 6.0f + MathUtils.random()*3.0f;
        initialized = false;
    }

    @Override
    public void spawn() {
        super.spawn();
        switchState(pursueEntity);
    }

    public static class Pool extends com.badlogic.gdx.utils.Pool<RifleZombie> {
        final GameWorld level;
        boolean firstKill;

        public Pool(GameWorld level, int limit) {
            super(limit, limit);
            this.level = level;
            firstKill = true;
            fill(limit);
        }

        @Override
        protected RifleZombie newObject() {
            return new RifleZombie(level) {
                @Override
                public boolean onRemove(boolean worldDisposal) {
                    if (worldDisposal) return true;
                    if (firstKill) {
                        Vector3 pickupPos = new Vector3(world.terrain.getPoint(hitBox.position.x, hitBox.position.z));
                        octree.add(RandomGunPickup.generate(pickupPos).spawnAnimation());
                        firstKill = false;
                    } else {
                        super.onRemove(false);
                    }
                    free(this);
                    return true;
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
