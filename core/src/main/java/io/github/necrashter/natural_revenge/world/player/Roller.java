package io.github.necrashter.natural_revenge.world.player;

import com.badlogic.gdx.utils.Array;

public interface Roller {
    WeaponMod getRandomMod(WeaponMod[] mods, Array<WeaponMod> currentMods);
    boolean getBoolean(float chance);
    int getInt(float[] weights);
}
