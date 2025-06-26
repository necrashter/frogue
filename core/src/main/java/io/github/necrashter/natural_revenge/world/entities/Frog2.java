package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;

public class Frog2 extends FrogBase {
    PursueToStrike pursueToStrike;

    public Frog2(GameWorld world) {
        super(world);
        modelInstance = new ModelInstance(Main.assets.frogModel, "Armature", "frog2");
        modelInstance.transform.setToTranslation(hitBox.position);
        animationController = new AnimationController(modelInstance);

        // States
        pursueToStrike = new PursueToStrike("walk", 5f) {
            @Override
            public void onStrikeBegin() {
                world.playSound(Main.assets.frogSounds[MathUtils.random.nextInt(Main.assets.frogSounds.length)], hitBox.position);
            }
        };
        pursueToStrike.moveAnimSpeed = pursueToStrike.movementSpeed / 2.5f;
        pursueToStrike.damage = 20f;
        pursueToStrike.strikeAnim = "hit";
        pursueToStrike.strikeBeginDist *= 1.75f;
        pursueToStrike.strikeMaxDist *= 1.75f;
        deathAnim = "dive";
        pursueToStrike.prepare(world.player);
        emergeState = new SingleAnimState("emerge", pursueToStrike);
        emergeState.animSpeed = 2.0f;
    }
}
