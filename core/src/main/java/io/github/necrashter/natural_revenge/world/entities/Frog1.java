package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;

public class Frog1 extends FrogBase {
    PursueToStrike pursueToStrike;

    public Frog1(GameWorld world) {
        super(world);
        modelInstance = new ModelInstance(Main.assets.frogModel, "Armature", "frog");
        modelInstance.transform.setToTranslation(hitBox.position);
        animationController = new AnimationController(modelInstance);

        // States
        pursueToStrike = new PursueToStrike("walk", 2.5f) {
            @Override
            public void onStrikeBegin() {
                world.playSound(Main.assets.frogSounds[MathUtils.random.nextInt(Main.assets.frogSounds.length)], hitBox.position);
            }
        };
        pursueToStrike.strikeAnim = "hit";
        pursueToStrike.strikeBeginDist *= 1.75f;
        pursueToStrike.strikeMaxDist *= 1.75f;
        pursueToStrike.damage = 10f;
        deathAnim = "dive";
        pursueToStrike.prepare(world.player);
        emergeState = new SingleAnimState("emerge", pursueToStrike);
        emergeState.animSpeed = 2.0f;
    }
}
