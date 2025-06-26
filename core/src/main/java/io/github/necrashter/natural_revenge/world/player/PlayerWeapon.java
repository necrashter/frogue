package io.github.necrashter.natural_revenge.world.player;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import io.github.necrashter.natural_revenge.world.GameWorld;

public abstract class PlayerWeapon {
    public Player player;
    public ModelInstance viewModel;
    public float speedMod = 1.f;

    public PlayerWeapon(Player player) {
        this.player = player;
    }

    public void setView(Camera camera) {
        if (viewModel != null) {
            viewModel.transform
                    .set(camera.view).inv()
                    .scale(0.33f, 0.33f, 0.33f)
                    .translate(0.5f, -0.75f, -0.5f)
            ;
        }
    }

    public abstract void update(float delta);

    public void render(GameWorld world) {
        world.modelBatch.render(viewModel, world.environment);
    }

    public abstract void buildText(StringBuilder stringBuilder);


    /**
     * Reset some variables on equip.
     */
    public abstract void onEquip();
}
