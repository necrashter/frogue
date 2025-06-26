package io.github.necrashter.natural_revenge.world.objects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import io.github.necrashter.natural_revenge.world.GameObject;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;
import io.github.necrashter.natural_revenge.world.geom.Shape;
import io.github.necrashter.natural_revenge.world.player.Player;

public abstract class BasePickupObject extends GameObject {
    public final Vector3 position;
    private ModelInstance model;
    public Shape shape;

    private boolean spawning = false;
    private float spawnProgress;

    public BasePickupObject(ModelInstance model, Shape shape, Vector3 position) {
        this.model = model.copy();
        this.shape = shape;
        this.position = position;
        computeModelTransform(0);
    }

    public boolean isVisible(Camera cam) {
        return shape != null && shape.isVisible(model.transform, cam);
    }

    public boolean isInViewDistance(Camera cam, float viewDistance) {
        return shape != null && shape.isInViewDistance(model.transform, cam, viewDistance);
    }

    private void computeModelTransform(float t) {
        float y = position.y;
        if (spawning) {
            y += MathUtils.sin(t) * 0.5f * spawnProgress;
            y += 1 - MathUtils.cos(spawnProgress * MathUtils.PI);
        } else {
            y += MathUtils.sin(t) * 0.5f;
        }
        model.transform
                .setToTranslation(
                        position.x,
                        y,
                        position.z
                )
                .rotateRad(Vector3.Y, t*MathUtils.PI);
    }

    public BasePickupObject spawnAnimation() {
        position.y -= 1.25f;
        spawning = true;
        spawnProgress = 0.0f;
        setRequiresUpdates(true);
        return this;
    }

    @Override
    public void update(float delta) {
        spawnProgress += delta;
        if (spawnProgress > 1.0f) {
            spawnProgress = 1.0f;
            spawning = false;
            setRequiresUpdates(false);
            position.y += 2.0f;
        }
    }

    @Override
    public void render(GameWorld world) {
        computeModelTransform(world.time);
        if (isInViewDistance(world.cam, world.viewDistance) && isVisible(world.cam)) {
            world.modelBatch.render(model, world.environment);
            world.visibleCount++;
        }
    }

    @Override
    public void getMinPoint(Vector3 p) {
        if (shape != null) shape.getMinPoint(model.transform, p);
    }

    @Override
    public void getMaxPoint(Vector3 p) {
        if (shape != null) shape.getMaxPoint(model.transform, p);
    }

    @Override
    public float intersectsGetRayT(Ray ray) {
        return Float.POSITIVE_INFINITY;
    }

    public abstract boolean onTaken(Player player);

    @Override
    public void hit(GameEntity entity) {
        if (spawning || !(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;
        if (!shape.intersects(model.transform, player.hitBox)) {
            return;
        }
        if (onTaken((Player) entity)) remove();
    }
}
