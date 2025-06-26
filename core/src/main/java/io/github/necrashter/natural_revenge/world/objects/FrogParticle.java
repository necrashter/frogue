package io.github.necrashter.natural_revenge.world.objects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pool;

import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.Damageable;
import io.github.necrashter.natural_revenge.world.GameObject;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;
import io.github.necrashter.natural_revenge.world.geom.Shape;
import io.github.necrashter.natural_revenge.world.geom.SphereShape;
import io.github.necrashter.natural_revenge.world.player.Player;

public class FrogParticle extends GameObject implements Pool.Poolable {
    private final ModelInstance model;
    public Vector3 position = new Vector3();
    public Vector3 velocity = new Vector3();
    public Shape shape;
    public static float DEFAULT_LIFETIME = 3f;
    public float lifetime = DEFAULT_LIFETIME;
    public GameEntity ignoredEntity = null;

    public FrogParticle() {
        model = new ModelInstance(Main.assets.frogParticleModel);
        shape = new SphereShape(0.2f);
        setRequiresUpdates(true);
    }

    public boolean isVisible(Camera cam) {
        return shape != null && shape.isVisible(model.transform, cam);
    }

    public boolean isInViewDistance(Camera cam, float viewDistance) {
        return shape != null && shape.isInViewDistance(model.transform, cam, viewDistance);
    }

    @Override
    public void render(GameWorld world) {
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

    @Override
    public void hit(GameEntity entity) {
        if (!shape.intersects(model.transform, entity.hitBox)) {
            return;
        }
        if (entity == ignoredEntity) {
            return;
        }
        if (entity instanceof Player) {
            Player player = (Player) entity;
            player.takeDamage(5f, Damageable.DamageAgent.NPC, Damageable.DamageSource.FrogParticle);
        }
        remove();
    }

    @Override
    public void reset() {
        lifetime = DEFAULT_LIFETIME;
        ignoredEntity = null;
    }

    public void spawn() {
        model.transform.setToTranslation(position);
    }

    @Override
    public void update(float delta) {
        position.mulAdd(velocity, delta);
        model.transform.setToTranslation(position);
        lifetime -= delta;
        if (lifetime <= 0f) {
            remove();
        } else {
            octreeNode.updateObject(this);
        }
    }
    public static class Pool extends com.badlogic.gdx.utils.Pool<FrogParticle> {
        public Pool(int initial_capacity) {
            super(initial_capacity);
        }
        @Override
        protected FrogParticle newObject() {
            return new FrogParticle();
        }
    }
}
