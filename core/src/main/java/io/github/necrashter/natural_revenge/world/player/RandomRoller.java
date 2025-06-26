package io.github.necrashter.natural_revenge.world.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class RandomRoller implements Roller {
    @Override
    public WeaponMod getRandomMod(WeaponMod[] mods, Array<WeaponMod> currentMods) {
        float total = 0;
        for (WeaponMod mod : mods) {
            if (mod.once && currentMods.contains(mod, true)) continue;
            total += mod.weight;
        }
        if (total == 0f) return null;
        float roll = MathUtils.random(total);

        for (WeaponMod mod : mods) {
            if (mod.once && currentMods.contains(mod, true)) continue;
            if (roll < mod.weight) {
                return mod;
            } else {
                roll -= mod.weight;
            }
        }
        return null;
    }

    @Override
    public boolean getBoolean(float chance) {
        return MathUtils.randomBoolean(chance);
    }

    @Override
    public int getInt(float[] weights) {
        float total = 0f;
        for (float w: weights) total += w;
        float roll = MathUtils.random(total);
        for (int i = 0; i < weights.length; ++i) {
            if (roll < weights[i]) return i;
            else roll -= weights[i];
        }
        return 0;
    }
}
