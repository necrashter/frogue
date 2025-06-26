package io.github.necrashter.natural_revenge.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;

public abstract class GameObject {
    public GameWorld world = null;
    public Octree.OctreeNode octreeNode = null;
    /**
     * Determines whether update function should be called every frame.
     */
    boolean requiresUpdates = false;

    public void remove() {
        if (octreeNode != null) {
            octreeNode.objects.removeValue(this, true);
            octreeNode = null;
        }
        if (requiresUpdates) {
            world.octree.dynamicObjects.removeValue(this, true);
        } else {
            world.octree.staticObjects.removeValue(this, true);
        }
        world = null;
    }

    public void setRequiresUpdates(boolean b) {
        if (requiresUpdates == b) return;
        requiresUpdates = b;
        if (world != null) {
            if (requiresUpdates) {
                world.octree.staticObjects.removeValue(this, true);
                world.octree.dynamicObjects.add(this);
            } else {
                world.octree.dynamicObjects.removeValue(this, true);
                world.octree.staticObjects.add(this);
            }
        }
    }

    public void update(float delta) {}

    public abstract void render(GameWorld world);
    public abstract void getMinPoint(Vector3 p);
    public abstract void getMaxPoint(Vector3 p);
    public abstract float intersectsGetRayT(Ray ray);
    public abstract void hit(GameEntity entity);
    public boolean moveCheck(GameEntity.MoveCheck moveCheck) {
        return false;
    }
}
