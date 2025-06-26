package io.github.necrashter.natural_revenge.world.geom;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;

public class BoxShape extends SphereShape {
    public BoxShape(BoundingBox bounds) {
        super(bounds);
    }

    @Override
    public boolean isVisible(Matrix4 transform, Camera cam) {
        transform.getTranslation(position).add(center);
        return cam.frustum.boundsInFrustum(
                position.x, position.y, position.z,
                dimensions.x, dimensions.y, dimensions.z);
    }

    @Override
    public float intersectsGetCenterDist2(Matrix4 transform, Ray ray) {
        final float len = intersectsGetRayT(transform, ray);
        if (len < Float.POSITIVE_INFINITY) {
            return position.dst2(ray.origin.x+ray.direction.x*len, ray.origin.y+ray.direction.y*len, ray.origin.z+ray.direction.z*len);
        }
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public float intersectsGetRayT(Matrix4 transform, Ray ray) {
        transform.getTranslation(position).add(center);

        final float divX = 1f / ray.direction.x;
        final float divY = 1f / ray.direction.y;
        final float divZ = 1f / ray.direction.z;

        float minx = ((position.x - dimensions.x) - ray.origin.x) * divX;
        float maxx = ((position.x + dimensions.x) - ray.origin.x) * divX;
        if (minx > maxx) {
            final float t = minx;
            minx = maxx;
            maxx = t;
        }

        float miny = ((position.y - dimensions.y) - ray.origin.y) * divY;
        float maxy = ((position.y + dimensions.y) - ray.origin.y) * divY;
        if (miny > maxy) {
            final float t = miny;
            miny = maxy;
            maxy = t;
        }

        float minz = ((position.z - dimensions.z) - ray.origin.z) * divZ;
        float maxz = ((position.z + dimensions.z) - ray.origin.z) * divZ;
        if (minz > maxz) {
            final float t = minz;
            minz = maxz;
            maxz = t;
        }

        float min = Math.max(Math.max(minx, miny), minz);
        float max = Math.min(Math.min(maxx, maxy), maxz);

        if (max >= 0 && max >= min) {
            return min > 0 ? min : max;
        }
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public void hit(Matrix4 transform, CharHitBox hitBox) {
        transform.getTranslation(position).add(center);
        float totalRadius = radius + hitBox.height;
        if (normal.set(position).sub(hitBox.position).len2() > totalRadius * totalRadius) {
            return;
        }
        Vector3 s = hitBox.getClosestSphere(position.y - dimensions.y);
        normal.set(s).sub(
                MathUtils.clamp(s.x, position.x - dimensions.x, position.x + dimensions.x),
                MathUtils.clamp(s.y, position.y - dimensions.y, position.y + dimensions.y),
                MathUtils.clamp(s.z, position.z - dimensions.z, position.z + dimensions.z)
        );
        float len2 = normal.len2();
        if (len2 < 1e-6) {
            // push towards center of map
            normal.set(
                    hitBox.position.x < 0 ? dimensions.x : -dimensions.x,
                    0,
                    hitBox.position.z < 0 ? dimensions.z : -dimensions.z
            );
            hitBox.getHit(normal);
        } else if (len2 < hitBox.radius * hitBox.radius) {
            float dist = hitBox.radius - normal.len();
            normal.nor().scl(dist);
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
        Vector3 s = moveCheck.hitBox.getClosestSphere(moveCheck.hitBox.position, position.y - dimensions.y);
        normal.set(s).sub(
                MathUtils.clamp(s.x, position.x - dimensions.x, position.x + dimensions.x),
                MathUtils.clamp(s.y, position.y - dimensions.y, position.y + dimensions.y),
                MathUtils.clamp(s.z, position.z - dimensions.z, position.z + dimensions.z)
        );
        if (normal.len2() < moveCheck.hitBox.radius * moveCheck.hitBox.radius) {
            normal.y = 0;
            moveCheck.normal.set(normal.nor());
            if (Math.abs(normal.z) < 1e-6) {
                moveCheck.left = s.x - (position.x - dimensions.x);
                moveCheck.right = (position.x + dimensions.x) - s.x;
            } else if (Math.abs(normal.x) < 1e-6) {
                moveCheck.left = s.z - (position.z - dimensions.z);
                moveCheck.right = (position.z + dimensions.z) - s.z;
            } else {
                float h = (float) Math.sqrt(dimensions.x * dimensions.x + dimensions.z * dimensions.z);
                moveCheck.left = h;
                moveCheck.right = h;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean intersects(Matrix4 transform, CharHitBox hitBox) {
        transform.getTranslation(position).add(center);
        float totalRadius = radius + hitBox.height;
        if (normal.set(position).sub(hitBox.position).len2() > totalRadius * totalRadius) {
            return false;
        }
        Vector3 s = hitBox.getClosestSphere(position.y - dimensions.y);
        normal.set(s).sub(
                MathUtils.clamp(s.x, position.x - dimensions.x, position.x + dimensions.x),
                MathUtils.clamp(s.y, position.y - dimensions.y, position.y + dimensions.y),
                MathUtils.clamp(s.z, position.z - dimensions.z, position.z + dimensions.z)
        );
        return normal.len2() < hitBox.radius * hitBox.radius;
    }

    @Override
    public void getMinPoint(Matrix4 transform, Vector3 p) {
        transform.getTranslation(p).add(center).sub(dimensions);
    }

    @Override
    public void getMaxPoint(Matrix4 transform, Vector3 p) {
        transform.getTranslation(p).add(center).add(dimensions);
    }
}
