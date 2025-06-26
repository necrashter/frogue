package io.github.necrashter.natural_revenge.world.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.Damageable;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.geom.Shape;

public class Firearm extends PlayerWeapon {
    public String name = "Firearm";
    Array<String> mods = new Array<>();
    public static final Vector3 temp1 = new Vector3();
    public static final Vector3 temp2 = new Vector3();
    public Decal decal = Decal.newDecal(Main.assets.muzzleFlashRegion, true);
    public Sound shootSound;
    public Sound reloadSound;
    public Vector3 muzzlePoint;

    public float damage = 5.0f;
    public float knockForward = -.375f;
    public float knockback = 2f;

    private float nextRoll = 0.0f;
    private float decalRotation = 0.0f;

    private float progress = 0.0f;
    private boolean noSoundYet = true;
    private enum State {
        Ready,
        Firing,
        Reloading,
        AwaitRelease,
    }
    private State state = State.Ready;

    public float totalBulletsShot = 0;
    public float totalBulletsHit = 0;
    public float totalDamage = 0f;

    public static class Template {
        public final ModelInstance model;
        /**
         * Used for pickup objects.
         */
        public final Shape shape;
        public final Vector3 muzzlePoint;

        public final Sound shootSound;
        public final Sound reloadSound;

        public Template(ModelInstance model, Shape shape, Vector3 muzzlePoint, Sound shootSound, Sound reloadSound) {
            this.model = model;
            this.shape = shape;
            this.muzzlePoint = muzzlePoint;
            this.shootSound = shootSound;
            this.reloadSound = reloadSound;
        }
    }

    public Firearm(Player player, Template template) {
        super(player);
        this.viewModel = template.model.copy();
        this.muzzlePoint = template.muzzlePoint;
        this.shootSound = template.shootSound;
        this.reloadSound = template.reloadSound;
    }

    public float soundPitchBase = 1.0f;
    public float soundVolume = 1.0f;
    public void playShootSound() {
        float ammoRatio = ((float) ammoInClip / maxAmmoInClip);
        shootSound.play(soundVolume, ammoRatio*0.25f+soundPitchBase, 0);
        if (ammoRatio < 0.5f) {
            Main.assets.gunEmpty.play((0.5f-ammoRatio)*2);
        }
    }

    /**
     * Reset some variables on equip.
     */
    public void onEquip() {
        aimSightRatio = 0f;
    }

    public boolean isAuto = true;
    public int burstCount = 1;
    public int remainingBurst = 1;
    public float noAutoWaitTime = 0f;
    public float noAutoTimer = 0f;
    public int bulletsPerShot = 1;
    public float spread = 0.02f;
    @Override
    public void update(float delta) {
        if (state == State.Ready) {
            if (player.firing1) {
                shoot();
                remainingBurst = burstCount - 1;
            } else if (player.shouldReload && ammoInClip < maxAmmoInClip) {
                player.shouldReload = false;
                beginReload();
            }
        } else if (state == State.AwaitRelease) {
            noAutoTimer -= delta;
            if (!player.firing1 && noAutoTimer <= 0f) {
                state = State.Ready;
            }
        } else if (state == State.Reloading) {
            progress += delta * reloadSpeed;
            if (progress > 1.0f) {
                progress = 0.0f;
                state = State.Ready;
                ammoInClip = maxAmmoInClip;
            }
        } else if (state == State.Firing) {
            progress += delta * recoverySpeed;
            decal.setColor(1.0f, 1.0f, 1.0f, 1.0f - progress);
            if (noSoundYet && progress > 0.5f) {
                noSoundYet = false;
            }
            if (progress > 1.0f) {
                noSoundYet = true;
                if (--ammoInClip > 0) {
                    progress = 0.0f;
                    if (isAuto) {
                        state = State.Ready;
                    } else if (remainingBurst > 0) {
                        shoot();
                        --remainingBurst;
                    } else {
                        state = State.AwaitRelease;
                        noAutoTimer = noAutoWaitTime;
                    }
                } else {
                    beginReload();
                }
                nextRoll = 0;
            }
        }

        if (state == State.Reloading || !player.firing2) {
            aimSightRatio = Math.max(0f, aimSightRatio - delta * 5f);
        } else {
            aimSightRatio = Math.min(1f, aimSightRatio + delta * 5f);
        }
    }

    private void shoot() {
        playShootSound();
        state = State.Firing;
        decalRotation = MathUtils.random(0, MathUtils.PI2);
        nextRoll = MathUtils.random(-recoveryRoll, recoveryRoll);
        for (int i = 0; i < bulletsPerShot; ++i) {
            player.castShootRay(spread);
            player.world.decalPool.addBulletTrace(decal.getPosition(), player.getShootTargetPoint());
            if (player.shootIntersection.object != null) {
                if (player.shootIntersection.object instanceof Damageable) {
                    Damageable damageable = (Damageable) player.shootIntersection.object;
                    damageable.takeDamage(damage, Damageable.DamageAgent.Player, Damageable.DamageSource.Firearm);
                }
            } else if (player.shootIntersection.entity != null) {
                player.shootIntersection.entity.takeDamage(damage, Damageable.DamageAgent.Player, Damageable.DamageSource.Firearm);
                totalBulletsHit++;
                totalDamage += damage;
            }
        }
        totalBulletsShot += bulletsPerShot;
        // Knockback
        float horizontalLength = knockForward;
        player.hitBox.velocity.add(
            player.camera.direction.x * horizontalLength,
            player.camera.direction.y * horizontalLength,
            player.camera.direction.z * horizontalLength);
        player.pitchMod += knockback;
        player.world.statistics.update(this);
    }

    void beginReload() {
        if (clips > 1) {
            if (clips < Integer.MAX_VALUE) clips -= 1;
            reloadSound.play();
            state = State.Reloading;
            progress = 0.0f;
        } else {
            reloadSound.play(1.0f, 3.0f, 0.0f);
            player.removeActiveWeapon();
        }
    }

    public float recoveryTranslateZ = 0.125f;
    public float recoveryRoll = 20f;
    public float recoveryPitch = 10f;
    public float recoverySpeed = 8.0f;

    public float reloadSpeed = 1.0f;
    public int ammoInClip = 30;
    public int maxAmmoInClip = 30;
    public int clips = 3;

    public float scaleX = 1.0f;
    public float scaleY = 1.0f;
    public float scaleZ = 1.0f;

    public float aimSightRatio = 0.0f;
    public float aimSightFov = 70f;
    public float aimSightY = -.4f;
    public float aimSightZ = -.75f;

    public void setView(Camera camera) {
        if (viewModel != null) {
            final float o = 0.75f;
            final float ratio = Interpolation.pow2.apply(aimSightRatio);
            final float tx = MathUtils.lerp(o, 0f, ratio);
            final float ty = MathUtils.lerp(-.65f, aimSightY, ratio);
            final float tz = MathUtils.lerp(-.75f, aimSightZ, ratio);
            player.camera.fieldOfView = MathUtils.lerp(Main.fov, aimSightFov, ratio);
            viewModel.transform
                    .set(camera.view).inv()
                    .scale(.33f, .33f, .33f)
                    .translate(tx, ty, tz)
            ;

            if (state == State.Firing) {
                decal.setScale(0.0008f);
                temp1.set(muzzlePoint.x * scaleX, muzzlePoint.y * scaleY, muzzlePoint.z * scaleZ).mul(viewModel.transform);
                decal.setPosition(temp1);
                temp2.set(camera.position).mulAdd(camera.direction, 4f).sub(temp1).scl(-1);
                decal.setRotation(temp2.nor(), temp1.set(camera.up).nor());
                decal.getRotation().mul(0, 0, MathUtils.sin(decalRotation), MathUtils.cos(decalRotation));

                float progressCos = (1.0f-(float) Math.cos(progress * MathUtils.PI * 2.0f)) * 0.5f;
                progressCos *= Math.max(1f-aimSightRatio, .25f);
                viewModel.transform
                        .translate(0, 0, progressCos * recoveryTranslateZ)
                        .rotate(Vector3.Z, nextRoll * progressCos)
                        .rotate(Vector3.X, progressCos * recoveryPitch)
                ;
            } else if (state == State.Reloading) {
                float progressCos = (1.0f-(float) Math.cos(progress * MathUtils.PI * 2.0f)) * 0.5f;
                viewModel.transform
                        .translate(0, 0, progressCos * -0.5f)
                        .rotate(Vector3.X, (MathUtils.cos(progress * MathUtils.PI)*.5f-.5f) * 360)
                ;
            }
            viewModel.transform.scale(scaleX, scaleY, scaleZ);
        }
    }

    public void render(GameWorld world) {
        super.render(world);
        if (state == State.Firing) {// && noSoundYet) {
            world.decalBatch.add(decal);
        }
    }

    @Override
    public void buildText(StringBuilder stringBuilder) {
        stringBuilder.append(name).append('\n');
        stringBuilder.append("Ammo: ").append(ammoInClip).append('/').append(maxAmmoInClip).append('\n');
        if (clips != Integer.MAX_VALUE) {
            stringBuilder.append("Clips: ").append(clips).append('\n');
        }
    }

    /**
     * Compute the overall DPS emptying one magazine, assuming all bullets hit.
     * @return DPS
     */
    public float computeDPS() {
        float overallDamage = damage * bulletsPerShot * maxAmmoInClip;
        float overallTime = (maxAmmoInClip/recoverySpeed) + (noAutoWaitTime * (MathUtils.ceil(maxAmmoInClip/(float)burstCount) - 1));
        return overallDamage / overallTime;
    }

    public float computeAccuracy(float distance, float targetRadius) {
        float realSpread = spread * distance;
        return MathUtils.clamp(targetRadius / realSpread, 0f, 1f);
    }

    public String hashName() {
        StringBuilder builder = new StringBuilder();
        for (String s: mods) builder.append(s).append(' ');
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
}
