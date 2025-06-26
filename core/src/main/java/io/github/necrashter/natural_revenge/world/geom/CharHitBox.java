package io.github.necrashter.natural_revenge.world.geom;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class CharHitBox {
    public final static float GRAVITY = 9f;
    public final static Vector3 sphere = new Vector3();
    /**
     * Bottom of capsule spine
     */
    public final Vector3 position = new Vector3();
    public final Vector3 velocity = new Vector3();
    public final Vector3 dimensions;
    /**
     * Length of capsule spine.
     */
    public final float height;
    public final float radius;
    public final float boundingRadius;
    public boolean onGround = false;
    public boolean onObject = false;

    public CharHitBox(float height, float radius) {
        this.height = height;
        this.radius = radius;
        this.boundingRadius = height / 2.0f + radius;
        dimensions = new Vector3(radius * 2, height, radius*2);
    }

    public Vector3 getBoundingSphere() {
        return sphere.set(position).add(0, height/2.0f, 0);
    }

    /**
     * Gets the closest sphere in this capsule to the given point.
     * @param y y coordinate of the given point
     * @return Closest sphere in the capsule
     */
    public Vector3 getClosestSphere(float y) {
        sphere.x = position.x;
        sphere.y = MathUtils.clamp(y, position.y, position.y + height);
        sphere.z = position.z;
        return sphere;
    }

    public Vector3 getClosestSphere(Vector3 position, float y) {
        sphere.x = position.x;
        sphere.y = MathUtils.clamp(y, position.y, position.y + height);
        sphere.z = position.z;
        return sphere;
    }

    public void update(float delta) {
        velocity.y -= delta * GRAVITY;
        position.mulAdd(velocity, delta);
    }

    public void getHit(Vector3 normal) {
        position.add(normal);
        if (velocity.x * normal.x < 0) {
            velocity.x *= -.5;
        }
        if (velocity.y * normal.y < 0) {
            velocity.y = 0;
            onObject = true;
        }
        if (velocity.z * normal.z < 0) {
            velocity.z *= -.5;
        }
    }

    public void getMinPoint(Vector3 p) {
        p.set(position);
        p.y -= radius;
        p.x -= radius;
        p.z -= radius;
    }

    public void getMaxPoint(Vector3 p) {
        p.set(position);
        p.y += height + radius;
        p.x += radius;
        p.z += radius;
    }

    private final static Vector3 intersectionRayPoint = new Vector3();
    private final static Vector3 intersectionVector = new Vector3();
    public float intersectionHeight = Float.NaN;

    public float intersectRay(Ray ray) {
        float rayT;
        if (Math.abs(ray.direction.x) < 1e-4 && Math.abs(ray.direction.z) < 1e-4) {
            intersectionVector.set(
                    position.x,
                    MathUtils.clamp(ray.origin.y, position.y, position.y + height),
                    position.z
            );
            rayT = ray.direction.dot(
                    intersectionVector.x - ray.origin.x,
                    intersectionVector.y - ray.origin.y,
                    intersectionVector.z - ray.origin.z
            );

            if (rayT < 0) {
                return Float.POSITIVE_INFINITY;
            }
            intersectionRayPoint.set(ray.origin).mulAdd(ray.direction, rayT);
        } else {
            // Ray origin to current position
            float rayToPosX = position.x - ray.origin.x;
            float rayToPosZ = position.z - ray.origin.z;
            // Dot product on XZ plane divided by squared length of ray direction on XZ plane
            // Dot product = |A||B|cos(theta), divide by |B|^2 = |A|cos(theta) / |B|
            rayT = (rayToPosX * ray.direction.x + rayToPosZ * ray.direction.z)
                    / (ray.direction.x * ray.direction.x + ray.direction.z * ray.direction.z);

            if (rayT < 0) {
                return Float.POSITIVE_INFINITY;
            }
            intersectionRayPoint.set(ray.origin).mulAdd(ray.direction, rayT);
            intersectionVector.set(
                    position.x,
                    MathUtils.clamp(intersectionRayPoint.y, position.y, position.y + height),
                    position.z
            );
        }

        intersectionHeight = intersectionVector.y - position.y;
        intersectionVector.sub(intersectionRayPoint);

        if (intersectionVector.len2() > radius * radius) {
            return Float.POSITIVE_INFINITY;
        }
        return rayT;
    }
}
