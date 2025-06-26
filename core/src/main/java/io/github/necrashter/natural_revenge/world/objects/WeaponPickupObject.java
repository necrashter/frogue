package io.github.necrashter.natural_revenge.world.objects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import io.github.necrashter.natural_revenge.world.geom.Shape;
import io.github.necrashter.natural_revenge.world.player.Player;
import io.github.necrashter.natural_revenge.world.player.PlayerWeapon;

public class WeaponPickupObject extends BasePickupObject {
    public PlayerWeapon weapon;

    public WeaponPickupObject(ModelInstance model, Shape shape, Vector3 position, PlayerWeapon weapon) {
        super(model, shape, position);
        this.weapon = weapon;
    }

    @Override
    public boolean onTaken(Player player) {
        player.addWeapon(weapon, true);
        return true;
    }
}
