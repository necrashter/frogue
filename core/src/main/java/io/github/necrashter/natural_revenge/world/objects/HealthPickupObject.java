package io.github.necrashter.natural_revenge.world.objects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import io.github.necrashter.natural_revenge.world.geom.Shape;
import io.github.necrashter.natural_revenge.world.player.Player;

public class HealthPickupObject extends BasePickupObject {
    float amount;

    public HealthPickupObject(ModelInstance model, Shape shape, Vector3 position, float amount) {
        super(model, shape, position);
        this.amount = amount ;
    }

    @Override
    public boolean onTaken(Player player) {
        player.heal(amount);
        return true;
    }
}
