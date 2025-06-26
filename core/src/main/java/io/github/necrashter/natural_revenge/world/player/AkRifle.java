package io.github.necrashter.natural_revenge.world.player;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import io.github.necrashter.natural_revenge.Main;

public class AkRifle extends Firearm {
    Array<String> meshNames = new Array<>(16);
    String magName = "default-mag";
    public AkRifle(Player player) {
        super(player, Main.assets.autoRifleTemplate);
        name = "Assault Rifle";
        knockback = 2f;
        meshNames.add("ak");
    }

    static final float[] MOD_COUNT_WEIGHTS = new float[] {5f, 35f, 60f};
    public static AkRifle generateRandom(Roller roller) {
        boolean isM4 = roller.getBoolean(.5f);
        Array<WeaponMod> mods = new Array<>();
        if (roller.getBoolean(.6f)) {
            mods.add(roller.getRandomMod(magMods, mods));
        }
        if (roller.getBoolean(.9f)) {
            mods.add(roller.getRandomMod(stockMods, mods));
        }
        int modCount = roller.getInt(MOD_COUNT_WEIGHTS);
        for (int i = 0; i < modCount; ++i) {
            mods.add(roller.getRandomMod(weaponMods, mods));
        }
        return generateFrom(isM4, mods);
    }

    public static AkRifle generateFrom(boolean isM4, Array<WeaponMod> mods) {
        AkRifle weapon = new AkRifle(null);
        if (isM4) {
            weapon.meshNames.set(0, "M4A1");
            weapon.shootSound = Main.assets.m4shoot;
            weapon.mods.add("M4A1");
        } else {
            weapon.spread *= 2f;
            weapon.speedMod = 1.2f;
            weapon.mods.add("AK");
        }
        for (WeaponMod mod: mods) {
            if (mod != null) mod.applyMod(weapon);
        }
        weapon.updateModel();
        return weapon;
    }

    private void updateModel() {
        meshNames.add(meshNames.get(0) + '-' + magName);
        viewModel = new ModelInstance(Main.assets.weaponsModel, meshNames);
    }

    static WeaponMod[] stockMods = {
        new WeaponMod("Woodstock", 100f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.knockback = 1.5f;
                Array<String> meshNames = ((AkRifle)weapon).meshNames;
                meshNames.add(meshNames.get(0) + "-woodstock");
            }
        },
        new WeaponMod("NormalStock", 100f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.knockback = 1f;
                Array<String> meshNames = ((AkRifle)weapon).meshNames;
                meshNames.add(meshNames.get(0) + "-stock");
            }
        },
    };

    static WeaponMod[] magMods = {
        new WeaponMod("ExtendedMag", 100f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Extended " + weapon.name;
                // Extended magazine but slower to reload.
                weapon.maxAmmoInClip += 10;
                weapon.ammoInClip += 10;
                weapon.reloadSpeed *= .85f;
                ((AkRifle)weapon).magName = "ext-mag";
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
                ((AkRifle)weapon).magName = "speed-mag";
            }
        },
        new WeaponMod("DrumMag",50f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Drum-Mag " + weapon.name;
                // Extended magazine but slower to reload.
                weapon.maxAmmoInClip = 90;
                weapon.ammoInClip = 90;
                weapon.reloadSpeed *= .4f;
                weapon.clips = 2;
                ((AkRifle)weapon).magName = "drum-mag";
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
                weapon.spread *= 1.5f; // Less accurate
            }
        },
        new WeaponMod("Chunky", 100f, false) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Chunky " + weapon.name;
                weapon.damage *= 1.35f;  // Increase damage
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
                weapon.recoverySpeed *= .65f;
                weapon.damage *= 2f;  // Increase damage
                weapon.spread = .0f;
            }
        },
        new WeaponMod("Vocal", 10f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Vocal " + weapon.name;
                weapon.shootSound = Main.assets.voiceShoot;
                weapon.reloadSound = Main.assets.voiceReload;
            }
        },
        new WeaponMod("Lightweight", 50f, false) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Lightweight " + weapon.name;
                weapon.speedMod *= 1.6f; // Make player %50 faster
                weapon.scaleX *= 0.8f;  // Make the model smaller
                weapon.scaleZ *= 0.8f;
                weapon.damage *= 0.6f;  // Less damage
                weapon.recoverySpeed *= 1.4f; // Fast reload
            }
        },
        new WeaponMod("BurstFire", 80f, false) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Burst-Fire " + weapon.name;
                weapon.burstCount += 2;
                weapon.recoverySpeed *= 2f;
                // Time required to shoot these bullets
                // Burst-fire does not modify overall bullets/second
                weapon.noAutoWaitTime += weapon.burstCount/weapon.recoverySpeed;
                weapon.isAuto = false;
                // Better knockback and accuracy
                weapon.knockback *= .75f;
                weapon.spread *= .5f;
            }
        },
        new WeaponMod("Overclocked", 80f, false) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Overclocked " + weapon.name;
                weapon.damage *= 1.85f; // more damage
                weapon.knockForward *= 2.5f; // unstable
                weapon.spread *= 2.0f; // varied accuracy
                weapon.reloadSpeed *= 0.7f; // slow
            }
        },
        new WeaponMod("Scoped", 80f, true) {
            @Override
            void mod(Firearm weapon) {
                weapon.name = "Scoped " + weapon.name;
                weapon.aimSightFov = 30f;
                weapon.aimSightY = -.36f;
                weapon.aimSightZ = -.5f;
                Array<String> meshNames = ((AkRifle)weapon).meshNames;
                meshNames.add(meshNames.get(0) + "-scope");
            }
        },
    };
}
