package io.github.necrashter.natural_revenge.world.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public abstract class WeaponMod {
    ///  Relative probability
    public float weight;
    public String hashName;
    public boolean once;

    public WeaponMod(String hashName, float weight, boolean once) {
        this.hashName = hashName;
        this.weight = weight;
        this.once = once;
    }

    public void applyMod(Firearm firearm) {
        mod(firearm);
        firearm.mods.add(hashName);
    }

    abstract void mod(Firearm firearm);

    static float totalWeight(WeaponMod[] weaponMods) {
        float total = 0f;
        for (WeaponMod mod: weaponMods) total += mod.weight;
        return total;
    }
}
