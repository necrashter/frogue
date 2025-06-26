package io.github.necrashter.natural_revenge.world.geom;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;

public class SphereShape implements Shape {
    public final static Vector3 position = new Vector3();
    public final static Vector3 normal = new Vector3();
    public final Vector3 center = new Vector3();
    /**
     * HALF dimensions.
     */
    public final Vector3 dimensions = new Vector3();
    public float radius;

    public SphereShape(BoundingBox bounds) {
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        dimensions.scl(0.5f);
        radius = dimensions.len();
    }

    public SphereShape(float radius) {
        this.radius = radius;
    }

    @Override
    public boolean isVisible(Matrix4 transform, Camera cam) {
        return cam.frustum.sphereInFrustum(transform.getTranslation(position).add(center), radius);
    }

    @Override
    public float intersectsGetCenterDist2(Matrix4 transform, Ray ray) {
        transform.getTranslation(position).add(center);
        final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
        if (len < 0f)
            return Float.POSITIVE_INFINITY;
        float dist2 = position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
        return (dist2 <= radius * radius) ? dist2 : Float.POSITIVE_INFINITY;
    }

    @Override
    public float intersectsGetRayT(Matrix4 transform, Ray ray) {
        // TODO: incorrect
        transform.getTranslation(position).add(center);
        final float len = ray.direction.dot(position.x-ray.origin.x, position.y-ray.origin.y, position.z-ray.origin.z);
        return len;
    }

    @Override
    public boolean isInViewDistance(Matrix4 transform, Camera cam, float viewDistance) {
        transform.getTranslation(position).add(center).scl(-1).add(cam.position);
        return position.len2() <= viewDistance*viewDistance;
    }

    @Override
    public void hit(Matrix4 transform, CharHitBox hitBox) {
        transform.getTranslation(position).add(center);
        float totalRadius = radius + hitBox.height;
        if (normal.set(position).sub(hitBox.position).len2() > totalRadius * totalRadius) {
            return;
        }
        Vector3 s = hitBox.getClosestSphere(position.y);
        normal.set(s).sub(position);
        totalRadius = radius + hitBox.radius;
        if (normal.len2() < totalRadius * totalRadius) {
            totalRadius -= normal.len();
            normal.nor().scl(totalRadius);
            hitBox.getHit(normal);
        }
    }

    @Override
    public boolean moveCheck(Matrix4 transform, GameEntity.MoveCheck moveCheck) {
        transform.getTranslation(position).add(center);
        float totalRadius = radius + moveCheck.hitBox.height;
        if (normal.set(position).sub(moveCheck.hitBox.position).len2() > totalRadius * totalRadius) {
            return false;
        }
        Vector3 s = moveCheck.hitBox.getClosestSphere(moveCheck.hitBox.position, position.y);
        normal.set(s).sub(position);
        totalRadius = radius + moveCheck.hitBox.radius;
        if (normal.len2() < totalRadius * totalRadius) {
            normal.y = 0;
            moveCheck.normal.set(normal.nor());
            moveCheck.left = radius;
            moveCheck.right = radius;
            return true;
        }
        return false;
    }

    @Override
    public boolean intersects(Matrix4 transform, CharHitBox hitBox) {
        transform.getTranslation(position).add(center);
        Vector3 s = hitBox.getClosestSphere(position.y);
        normal.set(s).sub(position);
        float totalRadius = radius + hitBox.radius;
        return normal.len2() < totalRadius * totalRadius;
    }

    @Override
    public void getMinPoint(Matrix4 transform, Vector3 p) {
        transform.getTranslation(p).add(center).add(-radius, -radius, -radius);
    }

    @Override
    public void getMaxPoint(Matrix4 transform, Vector3 p) {
        transform.getTranslation(p).add(center).add(radius, radius, radius);
    }
}
