package io.github.necrashter.natural_revenge.world.objects;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import io.github.necrashter.natural_revenge.AssetManager2;
import io.github.necrashter.natural_revenge.world.Spatial;
import io.github.necrashter.natural_revenge.world.geom.Shape;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;
import io.github.necrashter.natural_revenge.world.GameObject;
import io.github.necrashter.natural_revenge.world.GameWorld;

public class StaticGameObject extends GameObject implements Spatial {
    public ModelInstance model;
    /**
     * NOTE: modelShape must encapsulate physicsShape.
     */
    public Shape modelShape;
    public Shape physicsShape;

    public StaticGameObject(AssetManager2.GameObjectTemplate template) {
        this.model = template.model.copy();
        this.modelShape = template.modelShape;
        this.physicsShape = template.physicsShape;
    }

    public StaticGameObject(StaticGameObject copyFrom) {
        this.model = new ModelInstance(copyFrom.model);
        modelShape = copyFrom.modelShape;
        physicsShape = copyFrom.physicsShape;
    }

    public boolean isVisible(Camera cam) {
        return modelShape != null && modelShape.isVisible(model.transform, cam);
    }

    public boolean isInViewDistance(Camera cam, float viewDistance) {
        return modelShape != null && modelShape.isInViewDistance(model.transform, cam, viewDistance);
    }

    public float intersectsGetCenterDist2(Ray ray) {
        return physicsShape == null ? Float.POSITIVE_INFINITY : physicsShape.intersectsGetCenterDist2(model.transform, ray);
    }

    public float intersectsGetRayT(Ray ray) {
        return physicsShape == null ? Float.POSITIVE_INFINITY : physicsShape.intersectsGetRayT(model.transform, ray);
    }

    public void hit(GameEntity entity) {
        if (physicsShape != null) physicsShape.hit(model.transform, entity.hitBox);
    }

    @Override
    public boolean moveCheck(GameEntity.MoveCheck moveCheck) {
        if (physicsShape == null) return false;
        if (physicsShape.moveCheck(model.transform, moveCheck)) {
            moveCheck.object = this;
            return true;
        }
        return false;
    }

    @Override
    public void getPosition(Vector3 p) {
        model.transform.getTranslation(p);
    }

    public void getMinPoint(Vector3 p) {
        if (modelShape != null) modelShape.getMinPoint(model.transform, p);
    }

    public void getMaxPoint(Vector3 p) {
        if (modelShape != null) modelShape.getMaxPoint(model.transform, p);
    }

    public void render(GameWorld world) {
        if (isInViewDistance(world.cam, world.viewDistance) && isVisible(world.cam)) {
            world.modelBatch.render(model, world.environment);
            world.visibleCount++;
        }
    }
}
