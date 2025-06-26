package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;

import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.objects.FrogParticle;

public class Frog1Ranged extends FrogBase {
    PursueEntity pursueEntity;
    SpitOnce spitOnce;
    public Frog1Ranged(GameWorld world) {
        super(world);
        modelInstance = new ModelInstance(Main.assets.frogModel, "Armature", "frog");
        modelInstance.transform.setToTranslation(hitBox.position);
        animationController = new AnimationController(modelInstance);

        // States
        pursueEntity = new PursueEntity("walk", 4f);
        pursueEntity.moveAnimSpeed = pursueEntity.movementSpeed / 2.5f;
        pursueEntity.targetEntity = world.player;
        pursueEntity.checkRay = true;
        pursueEntity.desiredDistance = 6f;
        spitOnce = new SpitOnce();
        spitOnce.target = world.player;

        pursueEntity.onReached = spitOnce;
        spitOnce.onEnd = pursueEntity;

        deathAnim = "dive";

        emergeState = new SingleAnimState("emerge", pursueEntity);
        emergeState.animSpeed = 2.0f;
    }

    class SpitOnce extends State {
        GameEntity target;
        State onEnd;
        float attackDelta;
        boolean spitted;

        public void init() {
            moveTo(target.hitBox.position, 0);
            movement.setZero();
            animationController.setAnimation("spit", -1, animationListener);
            attackDelta = 0f;
            spitted = false;
        }

        private void spit() {
            FrogParticle f = world.frogParticlePool.obtain();
            f.ignoredEntity = Frog1Ranged.this;
            f.position.set(hitBox.position).mulAdd(forward, .25f).add(0f, .5f, 0f);
            f.velocity.set(world.player.hitBox.position).add(0f, 1.2f, 0f).sub(f.position).nor().scl(10f);
            f.spawn();
            world.octree.add(f);
        }

        @Override
        boolean update(float delta) {
            if (super.update(delta)) return true;
            attackDelta += delta;
            if (!spitted && attackDelta >= .5f) {
                spit();
                spitted = true;
            }
            if (animationJustEnded) {
                switchState(onEnd);
                return true;
            }
            return false;
        }
    }
}
