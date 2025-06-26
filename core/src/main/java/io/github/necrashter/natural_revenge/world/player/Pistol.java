package io.github.necrashter.natural_revenge.world.player;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import io.github.necrashter.natural_revenge.Main;

public class Pistol extends Firearm {
    Array<String> meshNames = new Array<>(16);
    public Pistol(Player player) {
        super(player, Main.assets.pistolTemplate);
        ammoInClip = 7;
        maxAmmoInClip = ammoInClip;
        reloadSpeed = 2.0f;
        recoveryTranslateZ = 0.125f;
        recoveryRoll = 20f;
        recoveryPitch = 30f;
        recoverySpeed = 4.0f;
        knockback = 2f;
        name = "Pistol";
        meshNames.add("pistol");
        speedMod = 1.4f;
        mods.add("Pistol");
    }

    static final float[] MOD_COUNT_WEIGHTS = new float[] {2f, 33f, 65f};
    public static Pistol generateRandom(Roller roller) {
        Array<WeaponMod> mods = new Array<>();
        if (roller.getBoolean(.6f)) {
            mods.add(roller.getRandomMod(magMods, mods));
        }
        int modCount = roller.getInt(MOD_COUNT_WEIGHTS);
        for (int i = 0; i < modCount; ++i) {
            mods.add(roller.getRandomMod(weaponMods, mods));
        }
        return generateFrom(mods);
    }

    public static Pistol generateFrom(Array<WeaponMod> mods) {
        Pistol pistol = new Pistol(null);
        for (WeaponMod mod: mods) {
            if (mod != null) mod.applyMod(pistol);
        }
        pistol.updateModel();
        return pistol;
    }
    private void updateModel() {
        viewModel = new ModelInstance(Main.assets.weaponsModel, meshNames);
    }

    static WeaponMod[] magMods = {
        new WeaponMod("ExtendedMag", 100f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Extended " + weapon.name;
                // Extended magazine but slower to reload.
                weapon.maxAmmoInClip += 10;
                weapon.ammoInClip += 10;
                weapon.reloadSpeed *= .85f;
                ((Pistol)weapon).meshNames.add("pistol-ext-mag");
            }
        },
        new WeaponMod("SpeedloaderMag", 100f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Speedloader " + weapon.name;
                weapon.maxAmmoInClip = MathUtils.ceil((float) weapon.maxAmmoInClip / 3.f);
                weapon.ammoInClip = weapon.maxAmmoInClip;
                weapon.reloadSpeed *= 2.f;
                weapon.clips *= 3;
            }
        },
        new WeaponMod("DrumMag",100f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Drum-Mag " + weapon.name;
                weapon.maxAmmoInClip = 30;
                weapon.ammoInClip = 30;
                weapon.reloadSpeed *= .5f;
                weapon.clips = 2;
                ((Pistol)weapon).meshNames.add("pistol-drum-mag");
            }
        },
    };

    static WeaponMod[] weaponMods = {
        new WeaponMod("Zippy", 100f, false) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Zippy " + weapon.name;
                weapon.recoverySpeed *= 1.25f;  // Faster firing
                weapon.scaleZ *= .5f;  // Make the gun shorter
                weapon.soundPitchBase = 1.125f;  // Higher frequency sound
                weapon.spread *= 1.7f; // Less accurate
            }
        },
        new WeaponMod("Chunky", 100f, false) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Chunky " + weapon.name;
                weapon.damage *= 1.25f;  // Increase damage
                weapon.scaleX *= 2f;  // Make the model thicker
                weapon.soundPitchBase = 0.8f;  // Lower frequency sound
                weapon.soundVolume = 1.2f; // Louder
                weapon.recoverySpeed *= .85f; // A bit slower
                weapon.knockForward *= 2.0f; // More knock back
                // NOTE: Knockback is represented by negative values in knockForward
            }
        },
        new WeaponMod("Scattershot", 100f, false) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Scattershot " + weapon.name;
                weapon.bulletsPerShot *= 2;
                weapon.recoverySpeed *= .65f;
                weapon.spread *= 3.0f;
            }
        },
        new WeaponMod("LongBarrel", 100f, false) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Long-barrel " + weapon.name;
                weapon.scaleZ *= 1.5f;  // Make the gun longer
                weapon.recoverySpeed *= .6f;
                weapon.damage *= 1.5f;  // Increase damage
                weapon.spread = .0f;
            }
        },
        new WeaponMod("Vocal", 100f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Vocal " + weapon.name;
                weapon.shootSound = Main.assets.voiceShoot;
                weapon.reloadSound = Main.assets.voiceReload;
            }
        },
        new WeaponMod("BurstFire", 80f, false) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Burst-Fire " + weapon.name;
                weapon.burstCount += 1;
                weapon.recoverySpeed *= 2.5f;
                // Time required to shoot these bullets
                // Burst-fire does not modify overall bullets/second
                weapon.noAutoWaitTime += weapon.burstCount/weapon.recoverySpeed;
                weapon.isAuto = false;
                // Better knockback and accuracy
                weapon.knockback *= .5f;
                weapon.spread *= .5f;
            }
        },
        new WeaponMod("Scoped", 100f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Scoped " + weapon.name;
                weapon.aimSightFov = 30f;
                weapon.aimSightY = -.315f;
                weapon.aimSightZ = -.5f;
                ((Pistol)weapon).meshNames.add("pistol-scope");
            }
        },
    };
}
