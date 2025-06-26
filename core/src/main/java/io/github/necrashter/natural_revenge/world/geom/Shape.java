package io.github.necrashter.natural_revenge.world.geom;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;

public interface Shape {
    boolean isVisible(Matrix4 transform, Camera cam);
    /** @return -1 on no intersection, or when there is an intersection: the squared distance between the center of this
     * object and the point on the ray closest to this object when there is intersection. */
    float intersectsGetCenterDist2(Matrix4 transform, Ray ray);

    float intersectsGetRayT(Matrix4 transform, Ray ray);

    boolean isInViewDistance(Matrix4 transform, Camera cam, float viewDistance);

    void hit(Matrix4 transform, CharHitBox hitBox);
    boolean moveCheck(Matrix4 transform, GameEntity.MoveCheck moveCheck);

    boolean intersects(Matrix4 transform, CharHitBox hitBox);

    void getMinPoint(Matrix4 transform, Vector3 p);
    void getMaxPoint(Matrix4 transform, Vector3 p);
}
