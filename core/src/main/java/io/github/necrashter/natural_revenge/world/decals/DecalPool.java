package io.github.necrashter.natural_revenge.world.decals;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;

public class DecalPool {
    /**
     * Must be power of 2.
     */
    private static final int BULLET_TRACES = 256;
    /**
     * I guess java cannot optimize modulo for powers of two since it doesn't support unsigned
     * integers. We will do it ourselves.
     */
    private static final int BULLET_TRACES_MASK = BULLET_TRACES - 1;
    /**
     * Circular buffer. Old elements will be overwritten when full.
     */
    private final BeamDecal[] bulletTraces = new BeamDecal[BULLET_TRACES];
    private int bulletTracesStart = 0;
    private int bulletTracesLength = 0;
    private static final float BULLET_TRACE_FADE_SPEED = 1.0f;

    public DecalPool() {
        TextureRegion bulletTraceRegion = new TextureRegion(Main.assets.bulletTrace);
        for (int i = 0; i < BULLET_TRACES; ++i) {
            bulletTraces[i] = BeamDecal.newBeamDecal(bulletTraceRegion, true);
        }
    }

    public void update(Camera camera, float delta) {
        float fade = delta * BULLET_TRACE_FADE_SPEED;
        int removed = 0;
        for (int i = 0; i < bulletTracesLength; ++i) {
            BeamDecal decal = bulletTraces[(i + bulletTracesStart) & BULLET_TRACES_MASK];
            decal.fade(fade);
            decal.setCamera(camera.position);
            if (decal.decayed) {
                ++removed;
            }
        }
        bulletTracesStart = (bulletTracesStart + removed) & BULLET_TRACES_MASK;
        bulletTracesLength -= removed;
    }

    public void addBulletTrace(Vector3 start, Vector3 end) {
        int i = (bulletTracesStart + bulletTracesLength) & BULLET_TRACES_MASK;
        bulletTracesLength++;
        if (bulletTracesLength > BULLET_TRACES) {
            // Overwrite first
            bulletTracesStart += 1;
            bulletTracesLength = BULLET_TRACES;
        }
        bulletTraces[i].reset();
        bulletTraces[i].setPosition2(start);
        bulletTraces[i].setPosition(end);
    }

    public void render(GameWorld world) {
        for (int i = 0; i < bulletTracesLength; ++i) {
            BeamDecal decal = bulletTraces[(i + bulletTracesStart) & BULLET_TRACES_MASK];
            world.decalBatch.add(decal);
        }
    }
}
