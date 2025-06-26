package io.github.necrashter.natural_revenge.world.objects;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.geom.Shape;
import io.github.necrashter.natural_revenge.world.player.AkRifle;
import io.github.necrashter.natural_revenge.world.player.Firearm;
import io.github.necrashter.natural_revenge.world.player.Pistol;
import io.github.necrashter.natural_revenge.world.player.Player;
import io.github.necrashter.natural_revenge.world.player.Roller;

public class RandomGunPickup extends BasePickupObject {
    Firearm weapon;

    public RandomGunPickup(ModelInstance model, Shape shape, Vector3 position, Firearm weapon) {
        super(model, shape, position);
        this.weapon = weapon;
    }

    public static Firearm generateWeapon(Roller roller) {
        boolean isPistol = roller.getBoolean(.2f);
        Firearm weapon;
        if (isPistol) {
            weapon = Pistol.generateRandom(roller);
        } else {
            weapon = AkRifle.generateRandom(roller);
        }
        return weapon;
    }

    public static RandomGunPickup generate(Vector3 position) {
        Firearm weapon = generateWeapon(Main.randomRoller);
        Shape shape;
        if (weapon instanceof Pistol) {
            shape = Main.assets.pistolTemplate.shape;
        } else {
            shape = Main.assets.autoRifleTemplate.shape;
        }
        return new RandomGunPickup(weapon.viewModel, shape, position, weapon);
    }

    @Override
    public boolean onTaken(Player player) {
        if (player.isInventoryFull()) return false;
        weapon.player = player;
        player.addWeapon(weapon, true);
        return true;
    }
}
