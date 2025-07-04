package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.Damageable;
import io.github.necrashter.natural_revenge.world.GameObject;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.Octree;
import io.github.necrashter.natural_revenge.world.Spatial;
import io.github.necrashter.natural_revenge.world.geom.CharHitBox;
import io.github.necrashter.natural_revenge.world.geom.SphereShape;

public class GameEntity implements Damageable, Spatial {
    public CharHitBox hitBox;
    public final Vector3 movement = new Vector3();
    public final Vector3 forward = new Vector3(1, 0, 0);

    public GameWorld world;
    public Octree octree = null;
    public Octree.OctreeNode octreeNode = null;

    public boolean dead = false;
    public float health = 100f;
    public float maxHealth = 100f;

    public void die() {
        dead = true;
        world.playSound(Main.assets.death[MathUtils.random.nextInt(Main.assets.death.length)], hitBox.position);
    }

    public float damageCalculation(float amount, DamageAgent agent, DamageSource source) {
        return health = Math.max(0f, health - amount);
    }

    @Override
    public boolean takeDamage(float amount, DamageAgent agent, DamageSource source) {
        if (dead) return false;
        if (damageCalculation(amount, agent, source) <= 0f) {
            die();
            return true;
        }
        return false;
    }

    public void heal(float amount) {
        health = Math.min(maxHealth, health+amount);
    }

    public boolean onRemove(boolean worldDisposal) {
        return true;
    }

    public void remove() {
        octree.remove(this);
    }

    public GameEntity(final GameWorld world, float height, float radius) {
        this.world = world;
        hitBox = new CharHitBox(height, radius);
        hitBox.position.y = world.terrain.getHeight(0, 0) + radius;
    }

    public void jump(float velocity) {
        if (hitBox.onGround || hitBox.onObject)
            hitBox.velocity.y += velocity;
    }

    public void update(float delta) {
        hitBox.position.mulAdd(movement, delta);

        if (hitBox.onObject || hitBox.onGround)
            hitBox.velocity.scl(.9f, 1, .9f);
        hitBox.update(delta);

        staticCollisions();

        octreeNode.updateEntity(this);
    }

    public void staticCollisions() {
        hitBox.onObject = false;
        world.octree.collide(this);

        hitBox.position.x = world.terrain.clampX(hitBox.position.x, hitBox.radius);
        hitBox.position.z = world.terrain.clampZ(hitBox.position.z, hitBox.radius);

        float terrainHeight = world.terrain.getHeight(hitBox.position.x, hitBox.position.z);
        float diff = hitBox.position.y - hitBox.radius - terrainHeight;
        if (diff < 0 || (hitBox.velocity.y < 0 && hitBox.onGround)) {
            hitBox.position.y = terrainHeight + hitBox.radius;
            hitBox.velocity.y = 0;
            hitBox.onGround = true;
        } else {
            hitBox.onGround = false;
        }
    }

    public void setPosition(float x, float z) {
        hitBox.position.set(x, 0, z);
        hitBox.position.y = world.terrain.getHeight(x, z) + hitBox.radius;
        hitBox.velocity.setZero();
    }

    public boolean isVisible(Camera cam) {
        return cam.frustum.sphereInFrustum(hitBox.getBoundingSphere(), hitBox.boundingRadius);
    }

    public boolean isInViewDistance(Camera cam, float viewDistance) {
        SphereShape.position.set(hitBox.getBoundingSphere()).scl(-1).add(cam.position);
        return SphereShape.position.len2() <= viewDistance*viewDistance;
    }

    public void render(GameWorld world) {
    }

    public boolean hit(GameEntity other) {
        if (dead || other.dead) return false;

        Vector3 a = SphereShape.position.set(hitBox.getClosestSphere(other.hitBox.position.y));
        Vector3 b = other.hitBox.getClosestSphere(a.y);
        float totalRadius = hitBox.radius + other.hitBox.radius;
        if (a.sub(b).len2() >= totalRadius) {
            return false;
        }

        totalRadius -= a.len();
        totalRadius *= totalRadius;
        totalRadius *= 20;
//            totalRadius += (movement.len2() + other.movement.len2()) * 100;
        a.nor().scl(totalRadius);
//            a.scl(.5f);
        hitBox.velocity.add(a);
        a.scl(-1f);
        other.hitBox.velocity.add(a);

        return true;
    }

    public void applyImpulse(float x, float y, float z) {
        hitBox.velocity.add(x, y, z);
    }

    @Override
    public void getPosition(Vector3 p) {
        p.set(hitBox.position);
    }

    @Override
    public void getMinPoint(Vector3 p) {
        hitBox.getMinPoint(p);
    }

    @Override
    public void getMaxPoint(Vector3 p) {
        hitBox.getMaxPoint(p);
    }

    public static class MoveCheck {
        public CharHitBox hitBox = null;
        public final Vector3 oldPosition = new Vector3();
        public GameObject object = null;
        public final Vector3 normal = new Vector3();
        public float left, right;
        public boolean boundary = false;
    }
    public static MoveCheck moveCheck = new MoveCheck();

    public MoveCheck prepareMoveCheck(float delta) {
        moveCheck.hitBox = this.hitBox;
        moveCheck.oldPosition.set(hitBox.position);
        hitBox.position.mulAdd(movement, delta);
        moveCheck.object = null;
        moveCheck.boundary = false;
        return moveCheck;
    }

    public MoveCheck moveCheck(float delta) {
        MoveCheck moveCheck = prepareMoveCheck(delta);
        if (world.terrain.moveCheck(moveCheck) || world.octree.moveCheck(moveCheck)) {
            hitBox.position.set(moveCheck.oldPosition);
            return moveCheck;
        } else {
            hitBox.position.set(moveCheck.oldPosition);
            return null;
        }
    }

    public static interface FilterFunction {
        boolean check(GameEntity entity);
    }
    public GameEntity getClosestEntity(float radius, FilterFunction f) {
        return octree.getClosestEntity(this, radius, f);
    }
}
