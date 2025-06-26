package io.github.necrashter.natural_revenge.world.geom;

import io.github.necrashter.natural_revenge.world.entities.GameEntity;
import io.github.necrashter.natural_revenge.world.GameObject;

public class RayIntersection {
    enum TargetType {
        NONE,
        OBJECT,
        ENTITY,
        TERRAIN,
    };
    public TargetType type;
    public GameObject object;
    public GameEntity entity;
    public float t;

    public void reset() {
        type = TargetType.NONE;
        object = null;
        entity = null;
        t = Float.POSITIVE_INFINITY;
    }

    public void setTarget(float t, GameObject object) {
        type = TargetType.OBJECT;
        this.object = object;
        this.entity = null;
        this.t = t;
    }

    public void setTarget(float t, GameEntity entity) {
        type = TargetType.ENTITY;
        this.object = null;
        this.entity = entity;
        this.t = t;
    }

    public void set(RayIntersection other) {
        this.type = other.type;
        this.object = other.object;
        this.entity = other.entity;
        this.t = other.t;
    }

    public void setTerrain(float t) {
        type = TargetType.TERRAIN;
        this.object = null;
        this.entity = null;
        this.t = t;
    }
}
