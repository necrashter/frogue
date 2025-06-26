package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;

import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;

public class ZombieBoss1 extends ZombieBossBase {
    NPC.PursueEntity pursueEntity;
    NPC.RifleShoot shootOnce;

    public ZombieBoss1(GameWorld world) {
        super(world);
        modelInstance = new ModelInstance(Main.assets.npcModel, "ManArmature", "ZombieBoss", "M4A1");
        modelInstance.transform.setToTranslation(hitBox.position);
        animationController = new AnimationController(modelInstance);

        muzzlePoint.set(autoRifleMuzzlePoint);

        // States
        pursueEntity = new NPC.PursueEntity(true);
        pursueEntity.targetEntity = world.player;
//        pursueEntity.checkRay = true;
        shootOnce = new NPC.RifleShoot();
        shootOnce.target = world.player;

        // Transitions
        pursueEntity.onReached = shootOnce;
        shootOnce.onEnd = pursueEntity;

        maxHealth = 20f;
        health = maxHealth;

        emergeState = new SingleAnimState("emerge", pursueEntity);
        emergeState.animSpeed = 2.0f;

        reset();
    }

    @Override
    public void reset() {
        super.reset();
        pursueEntity.desiredDistance = 6.0f + MathUtils.random()*3.0f;
    }
}
