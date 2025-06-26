package io.github.necrashter.natural_revenge.world.entities;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.Damageable;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.Spatial;
import io.github.necrashter.natural_revenge.world.decals.DecayingDecal;
import io.github.necrashter.natural_revenge.world.geom.RayIntersection;
import io.github.necrashter.natural_revenge.world.objects.RandomGunPickup;

public class NPC extends GameEntity implements Pool.Poolable {
    private static final Vector3 temp = new Vector3();

    ModelInstance modelInstance;
    final Vector3 right = new Vector3();

    AnimationController animationController;
    boolean animationJustEnded = false;
    AnimationController.AnimationListener animationListener = new AnimationController.AnimationListener() {
        @Override
        public void onEnd(AnimationController.AnimationDesc animation) {
            animationJustEnded = true;
        }

        @Override
        public void onLoop(AnimationController.AnimationDesc animation) {
            animationJustEnded = true;
        }
    };

    public NPC(final GameWorld world) {
        super(world, 1.5f, 1.5f/4.0f);
    }

    public NPC(final GameWorld world, float height, float radius) {
        super(world, height, radius);
    }

    boolean initialized = false;
    public void init() {
        if (currentState != null) {
            initialized = true;
            currentState.init();
        }
    }

    @Override
    public void render(GameWorld world) {
        if (isInViewDistance(world.cam, world.viewDistance) && isVisible(world.cam)) {
            world.modelBatch.render(modelInstance, world.environment);
            world.visibleCount++;
        }
        if (!decal.decayed) {
            world.decalBatch.add(decal);
        }
    }

    public boolean removeOnDeath = true;

    @Override
    public void update(float delta) {
        if (!decal.decayed) {
            decal.fade(delta);
        }

        animationController.update(delta);

        if (dead) {
            if (removeOnDeath && animationJustEnded) {
                remove();
                animationJustEnded = false;
            }
            return;
        }

        stateUpdate(delta);

        super.update(delta);
        updateTransform();
        animationJustEnded = false;
    }

    void updateTransform() {
        modelInstance.transform.idt();
        modelInstance.transform.val[Matrix4.M02] = -forward.x;
        modelInstance.transform.val[Matrix4.M12] = -forward.y;
        modelInstance.transform.val[Matrix4.M22] = -forward.z;

        right.set(forward).crs(Vector3.Y).nor();
        modelInstance.transform.val[Matrix4.M00] = right.x;
        modelInstance.transform.val[Matrix4.M10] = right.y;
        modelInstance.transform.val[Matrix4.M20] = right.z;

        modelInstance.transform.trn(hitBox.position.x, hitBox.position.y-hitBox.radius, hitBox.position.z);
    }

    public String deathAnim = "fallback";
    @Override
    public void die() {
        super.die();
        animationController.setAnimation(deathAnim, animationListener);
    }

    /**
     * Default NPC onRemove: spawn health pickup
     * @param worldDisposal The entity is being deleted because the level ended
     */
    @Override
    public boolean onRemove(boolean worldDisposal) {
        if (worldDisposal) return true;
        Vector3 pickupPos;
        switch (MathUtils.random(3)) {
            case 0:
                pickupPos = new Vector3(world.terrain.getPoint(hitBox.position.x, hitBox.position.z));
                octree.add(Main.assets.createHealthPickup(pickupPos).spawnAnimation());
                break;
            case 1:
                pickupPos = new Vector3(world.terrain.getPoint(hitBox.position.x, hitBox.position.z));
                octree.add(RandomGunPickup.generate(pickupPos).spawnAnimation());
                break;
            default:
                break;
        }
        return true;
    }

    /* """AI""" Stuff */

    private float distanceToTarget = 0.0f;
    private float distanceToTargetB = 0.0f;
    public void moveTo(Vector3 target, float speed) {
        movement.set(target).sub(hitBox.position);
        distanceToTarget = movement.len();
        movement.y *= 0.5f;
        distanceToTargetB = movement.len();
        movement.y = 0;
        forward.set(movement.nor());
        movement.scl(speed);
    }

    public void checkedMoveTo(Vector3 target, float speed, float delta) {
        movement.set(target).sub(hitBox.position);
        distanceToTarget = movement.len();
        movement.y *= 0.5f;
        distanceToTargetB = movement.len();
        movement.y = 0;
        movement.nor();
        forward.set(movement);
        movement.scl(speed);

        MoveCheck moveCheck = moveCheck(delta);
        if (moveCheck != null) {
            // Hit an object; follow wall
            moveCheck.normal.rotateRad(Vector3.Y, MathUtils.HALF_PI);
            // Following line tries to shorten distance but introduces an edge-case:
            // When normal = -movement, NPC will be stuck, going left and right every frame.
//            if (moveCheck.normal.dot(movement) < 0) moveCheck.normal.scl(-1);
            movement.set(moveCheck.normal);
            forward.set(movement);
            movement.scl(speed);
        }
    }

    /* Ray */
    static final Vector3 pistolMuzzlePoint = new Vector3(0.058f * 3, 0.412f * 3, -0.325f * 3);
    static final Vector3 autoRifleMuzzlePoint = new Vector3(0.12f * 3, 0.4f * 3, -0.34f * 3);
    final Vector3 muzzlePoint = new Vector3(pistolMuzzlePoint);
    private final Vector3 globalMuzzlePoint = new Vector3();

    private final Ray ray = new Ray();
    private final RayIntersection rayIntersection = new RayIntersection();
    private final DecayingDecal decal = DecayingDecal.newDecayingDecal(Main.assets.muzzleFlashRegion, true);

    private RayIntersection intersectRay(Vector3 target) {
        // Some repeated operations here
        updateTransform();
        globalMuzzlePoint.set(muzzlePoint).mul(modelInstance.transform);
        ray.direction.set(target).sub(globalMuzzlePoint).nor();
        ray.origin.set(globalMuzzlePoint);
        rayIntersection.set(world.intersectRay(ray, this));
        return rayIntersection;
    }

    private final Vector3 aimTargetPosition = new Vector3();
    private RayIntersection aim(GameEntity target, float baseInaccuracy, float movementInaccuracy) {
        // Some repeated operations here
        aimTargetPosition.set(target.hitBox.position);
        aimTargetPosition.y += target.hitBox.height/2;
        updateTransform();
        globalMuzzlePoint.set(muzzlePoint).mul(modelInstance.transform);
        ray.direction.set(aimTargetPosition).sub(globalMuzzlePoint).nor();
        if (baseInaccuracy > 0 || movementInaccuracy > 1e-4) {
            // movement inaccuracy is scaled with perpendicular component of enemy movement
            float inaccuracy = baseInaccuracy;
            float targetMovementLen = target.movement.len();
            if (targetMovementLen > 1e-2) inaccuracy += movementInaccuracy*(1-Math.abs(target.movement.dot(forward) / targetMovementLen));
            shootTarget.set(MathUtils.random()-0.5f, MathUtils.random()-0.5f, MathUtils.random()-0.5f).nor();
            ray.direction.mulAdd(shootTarget, inaccuracy).nor();
        }
        ray.origin.set(globalMuzzlePoint);
        rayIntersection.set(world.intersectRay(ray, this));
        return rayIntersection;
    }

    private static final Vector3 shootTarget = new Vector3();
    private void showDecal() {
        decal.setScale(0.001f);
        decal.setPosition(globalMuzzlePoint);
        decal.setRotation(ray.direction, Vector3.Y);
        float decalRotation = MathUtils.random(0, MathUtils.PI2);
        decal.getRotation().mul(0, 0, MathUtils.sin(decalRotation), MathUtils.cos(decalRotation));
        decal.reset();
        shootTarget.set(ray.origin).mulAdd(ray.direction, Math.min(world.viewDistance, rayIntersection.t));
        world.decalPool.addBulletTrace(shootTarget, globalMuzzlePoint);
    }

    /* Finite State Machine */

    State currentState = null;

    void stateUpdate(float delta) {
        if (currentState != null) {
            currentState.update(delta);
        } else {
            movement.setZero();
        }
    }

    public void switchState(State newState) {
        movement.setZero();
        currentState = newState;
        newState.init();
    }

    @Override
    public void reset() {
        dead = false;
        initialized = false;
    }

    public void spawn(float newHealth) {
        maxHealth = newHealth;
        spawn();
    }

    public void spawn() {
        health = maxHealth;
        Vector2 point = world.randomPointOutsideView();
        setPosition(point.x, point.y);
        world.octree.add(this);
        init();
        updateTransform();
    }

    abstract class StateTransitionCondition {
        final State nextState;

        protected StateTransitionCondition(State nextState) {
            this.nextState = nextState;
        }

        abstract boolean check();
    }

    abstract class State {
        Array<StateTransitionCondition> stateSwitchConditions = new Array<>();

        void addStateSwitchCondition(StateTransitionCondition condition) {
            stateSwitchConditions.add(condition);
        }

        void init() {
        }

        boolean update(float delta) {
            for (StateTransitionCondition condition: stateSwitchConditions) {
                if (condition.check()) {
                    switchState(condition.nextState);
                    return true;
                }
            }
            return false;
        }
    }

    class SingleAnimState extends State {
        State nextState;
        String animName;
        float animSpeed = 1.0f;
        SingleAnimState(String anim, State next) {
            nextState = next;
            animName = anim;
        }

        @Override
        void init() {
            animationController.setAnimation(animName, -1, animSpeed, animationListener);
            animationController.update(0f);
        }

        public boolean update(float delta) {
            if (super.update(delta)) return true;
            if (animationJustEnded) {
                switchState(nextState);
                return true;
            }
            return false;
        }
    }

    class PursueToStrike extends State {
        public float strikeHitTime = 6f/30f;
        public float strikeBeginDist = 1.0f;
        public float strikeMaxDist = 2.0f;

        float movementSpeed;
        float moveAnimSpeed = 1f;
        float attackAnimSpeed = 1f;
        float damage = 5f;
        String movementAnim;
        String strikeAnim = "strike";
        GameEntity targetEntity;

        private boolean attacking = false;
        private boolean attackHit = false;
        private float attackDelta = 0.0f;

        PursueToStrike(boolean run) {
            if (run) {
                this.movementAnim = "run";
                this.movementSpeed = 5.0f;
            } else {
                this.movementAnim = "walk";
                this.movementSpeed = 2.5f;
            }
        }

        PursueToStrike(String movementAnim, float movementSpeed) {
            this.movementAnim = movementAnim;
            this.movementSpeed = movementSpeed;
        }

        void prepare(GameEntity target) {
            targetEntity = target;
        }

        public void init() {
            moveTo(targetEntity.hitBox.position ,0);
            if (distanceToTargetB < strikeBeginDist) {
                animationController.setAnimation(strikeAnim, -1, attackAnimSpeed, animationListener);
                onStrikeBegin();
            } else {
                animationController.setAnimation(movementAnim, -1, moveAnimSpeed, animationListener);
            }
        }

        public boolean update(float delta) {
            if (super.update(delta)) return true;
            if (attacking) {
                moveTo(targetEntity.hitBox.position, 0);
                attackDelta += delta;
                if (!attackHit && attackDelta >= strikeHitTime) {
                    // NOTE: No need to cast ray for close combat.
                    if (distanceToTargetB < strikeMaxDist) {
                        targetEntity.takeDamage(damage, DamageAgent.NPC, DamageSource.Melee);
                    }
                    attackHit = true;
                }
                if (animationJustEnded) {
                    attackDelta = 0.0f;
                    attackHit = false;
                    if (distanceToTargetB > strikeBeginDist) {
                        attacking = false;
                        animationController.animate(movementAnim, -1, moveAnimSpeed, animationListener, 0.2f);
                    } else {
                        onStrikeBegin();
                    }
                }
//                movement.setZero();
            } else {
                checkedMoveTo(targetEntity.hitBox.position, movementSpeed, delta);
                if (distanceToTargetB < strikeBeginDist) {
                    movement.setZero();
                    animationController.animate(strikeAnim, -1, attackAnimSpeed, animationListener, 0.15f);
                    attacking = true;
                    onStrikeBegin();
                }
            }
            return false;
        }

        public void onStrikeBegin() {}
    }

    class PursueEntity extends State {
        final float movementSpeed;
        final String movementAnim;
        GameEntity targetEntity;
        float desiredDistance = 1.0f;
        boolean checkRay = false;
        float moveAnimSpeed = 1f;

        State onReached = null;
        State onTargetDead = null;

        PursueEntity(boolean run) {
            if (run) {
                this.movementAnim = "run";
                this.movementSpeed = 5.0f;
            } else {
                this.movementAnim = "walk";
                this.movementSpeed = 2.5f;
            }
        }

        PursueEntity(String movementAnim, float movementSpeed) {
            this.movementAnim = movementAnim;
            this.movementSpeed = movementSpeed;
        }

        public void init() {
            if (onTargetDead != null && targetEntity.dead) {
                switchState(onTargetDead);
                return;
            }
            moveTo(targetEntity.hitBox.position, 0);
            if (distanceToTargetB < desiredDistance && (!checkRay || intersectRay(targetEntity.hitBox.position).entity == targetEntity)) {
                switchState(onReached);
            } else {
                animationController.setAnimation(movementAnim, -1, moveAnimSpeed, animationListener);
            }
        }

        public boolean update(float delta) {
            if (onTargetDead != null && targetEntity.dead) {
                switchState(onTargetDead);
                return true;
            }
            if (super.update(delta)) return true;
            checkedMoveTo(targetEntity.hitBox.position, movementSpeed, delta);
            if (distanceToTargetB < desiredDistance && (!checkRay || intersectRay(targetEntity.hitBox.position).entity == targetEntity)) {
                switchState(onReached);
            }
            return false;
        }
    }

    class PistolShoot extends State {
        GameEntity target;
        State onEnd;
        float damage = 5.0f;
        float baseInaccuracy = 0.05f;
        float movementInaccuracy = 0.2f;

        public void init() {
            moveTo(target.hitBox.position, 0);
            movement.setZero();
            animationController.setAnimation("shoot", -1, animationListener);
            aim(target, baseInaccuracy, movementInaccuracy);
            showDecal();
            world.playSound(Main.assets.enemyPistol, ray.origin);
            if (rayIntersection.object != null) {
                if (rayIntersection.object instanceof Damageable) {
                    final Damageable damageable = (Damageable) rayIntersection.object;
                    damageable.takeDamage(damage, Damageable.DamageAgent.NPC, Damageable.DamageSource.Firearm);
                }
            } else if (rayIntersection.entity != null) {
                rayIntersection.entity.takeDamage(damage, Damageable.DamageAgent.NPC, Damageable.DamageSource.Firearm);
            }
        }

        @Override
        boolean update(float delta) {
            if (super.update(delta)) return true;
            if (animationJustEnded) {
                switchState(onEnd);
                return true;
            }
            return false;
        }
    }

    class RifleShoot extends State {
        GameEntity target;
        State onEnd;
        float damage = 5.0f;
        float baseInaccuracy = 0.15f;
        float movementInaccuracy = 0.2f;
        int burst = 5;

        int remaining;

        public void init() {
            fire();
            animationController.setAnimation("autoshoot", -1, 2, animationListener);
            world.playSound(Main.assets.enemyRifle, ray.origin);
            remaining = burst;
        }

        public void fire() {
            moveTo(target.hitBox.position, 0);
            movement.setZero();
            aim(target, baseInaccuracy, movementInaccuracy);
            showDecal();
            if (rayIntersection.object != null) {
                if (rayIntersection.object instanceof Damageable) {
                    final Damageable damageable = (Damageable) rayIntersection.object;
                    damageable.takeDamage(damage, Damageable.DamageAgent.NPC, Damageable.DamageSource.Firearm);
                }
            } else if (rayIntersection.entity != null) {
                rayIntersection.entity.takeDamage(damage, Damageable.DamageAgent.NPC, Damageable.DamageSource.Firearm);
            }
        }

        @Override
        boolean update(float delta) {
            if (super.update(delta)) return true;
            if (animationJustEnded) {
                remaining -= 1;
                if (remaining > 0) {
                    fire();
                } else if (remaining == 0) {
                    animationController.setAnimation("rifle-idle", -1, animationListener);
                } else {
                    switchState(onEnd);
                    return true;
                }
            }
            return false;
        }
    }

    class FollowEntity extends State {
        private static final float distStartRunning = 7.0f;
        private static final float distStartWalking = 4.0f;
        private static final float distStartIdle = 2.0f;
        String idleAnimation = "idle";

        final float walkSpeed;
        final float runSpeed;
        GameEntity targetEntity;

        private boolean moving = false;
        private boolean running = false;

        FollowEntity() {
            this(2.5f, 5.0f);
        }

        FollowEntity(float walkSpeed, float runSpeed) {
            this.walkSpeed = walkSpeed;
            this.runSpeed = runSpeed;
        }

        void prepare(GameEntity target) {
            targetEntity = target;
        }

        public void init() {
            animationController.setAnimation(idleAnimation, -1, animationListener);
            moving = false;
        }

        public boolean update(float delta) {
            if (super.update(delta)) return true;
            float movementSpeed = running ? runSpeed : walkSpeed;
            checkedMoveTo(targetEntity.hitBox.position, movementSpeed, delta);
            if (moving) {
                if (distanceToTargetB < distStartIdle) {
                    moving = false;
                    running = false;
                    animationController.animate(idleAnimation, -1, animationListener, 0.25f);
                    movement.setZero();
                } else {
                    if (!running && distanceToTargetB >= distStartRunning) {
                        running = true;
                        animationController.animate("run", -1, animationListener, 0.25f);
                    }
                }
            } else {
                if (distanceToTargetB >= distStartRunning) {
                    moving = true;
                    animationController.animate("run", -1, animationListener, 0.25f);
                    running = true;
                } else if (distanceToTargetB >= distStartWalking) {
                    moving = true;
                    animationController.animate("walk", -1, animationListener, 0.25f);
                }
                movement.setZero();
            }
            return false;
        }
    }

    class WalkToPosition extends State {
        final float walkSpeed;
        float desiredDistance;
        final Vector3 targetPosition = new Vector3();

        boolean moving = false;

        State onReached = null;

        WalkToPosition() {
            this(2.5f, 3.0f);
        }

        WalkToPosition(float walkSpeed, float desiredDistance) {
            this.walkSpeed = walkSpeed;
            this.desiredDistance = desiredDistance;
        }

        void prepare(float x, float y, float z) {
            targetPosition.set(x, y, z);
        }

        void init() {
            animationController.setAnimation("idle", -1, animationListener);
            moving = false;
        }

        boolean update(float delta) {
            if (super.update(delta)) return true;
            checkedMoveTo(targetPosition, walkSpeed, delta);
            if (moving) {
                if (distanceToTargetB <= desiredDistance) {
                    if (onReached != null) {
                        switchState(onReached);
                        return true;
                    }
                    moving = false;
                    animationController.animate("idle", -1, animationListener, 0.25f);
                    movement.setZero();
                }
            } else {
                if (distanceToTargetB > desiredDistance) {
                    moving = true;
                    animationController.animate("walk", -1, animationListener, 0.25f);
                } else if (onReached != null) {
                    switchState(onReached);
                    return true;
                }
                movement.setZero();
            }
            return false;
        }
    }

    class WalkToTarget extends WalkToPosition {
        Spatial target;

        void prepare(Spatial target) {
            this.target = target;
        }

        boolean update(float delta) {
            target.getPosition(targetPosition);
            return super.update(delta);
        }
    }

    class WalkToRandomPosition extends WalkToTarget {
        FakeSpatial spatial = new FakeSpatial(new Vector3());

        @Override
        void init() {
            world.terrain.getRandomPoint(spatial.position);
            target = spatial;
            super.init();
        }
    }

    class WaitIdle extends State {
        final String idleAnim;
        float timePassed;
        float waitTime;
        State onTimeout = null;

        WaitIdle(String idleAnim, float waitTime) {
            this.idleAnim = idleAnim;
            this.waitTime = waitTime;
        }

        void init() {
            animationController.animate(idleAnim, -1, animationListener, 0.25f);
            timePassed = 0.0f;
        }

        @Override
        boolean update(float delta) {
            if (super.update(delta)) return true;
            timePassed += delta;
            if (timePassed >= waitTime && onTimeout != null) {
                switchState(onTimeout);
                return true;
            }
            return false;
        }
    }

    class RandomWaitIdle extends WaitIdle {
        final float minWaitTime, maxWaitTime;

        RandomWaitIdle(String idleAnim, float minWaitTime, float maxWaitTime) {
            super(idleAnim, minWaitTime);
            this.minWaitTime = minWaitTime;
            this.maxWaitTime = maxWaitTime;
        }

        @Override
        void init() {
            waitTime = MathUtils.random(minWaitTime, maxWaitTime);
            super.init();
        }
    }

    class RandomCheerOrIdle extends State {
        final float minWaitTime, maxWaitTime;

        float timePassed;
        float waitTime;
        State onTimeout = null;

        RandomCheerOrIdle(float minWaitTime, float maxWaitTime) {
            super();
            this.minWaitTime = minWaitTime;
            this.maxWaitTime = maxWaitTime;
        }

        void init() {
            waitTime = MathUtils.random(minWaitTime, maxWaitTime);
            animationController.animate(MathUtils.randomBoolean() ? "idle" : "charm", -1, animationListener, 0.25f);
            timePassed = 0.0f;
        }

        @Override
        boolean update(float delta) {
            if (super.update(delta)) return true;
            timePassed += delta;
            if (timePassed >= waitTime && onTimeout != null) {
                switchState(onTimeout);
                return true;
            }
            return false;
        }
    }

    class RunAwayFromEntity extends State {
        float movementSpeed;
        final String movementAnim;
        GameEntity targetEntity;
        float desiredDistance = 26.0f;

        State onEscaped = null;

        boolean followingBoundary = false;
        boolean turnLeft = false;
        final Vector3 boundaryMovement = new Vector3();

        RunAwayFromEntity(boolean run) {
            if (run) {
                this.movementAnim = "run";
                this.movementSpeed = 5.0f;
            } else {
                this.movementAnim = "walk";
                this.movementSpeed = 2.5f;
            }
        }

        RunAwayFromEntity(String movementAnim, float movementSpeed) {
            this.movementAnim = movementAnim;
            this.movementSpeed = movementSpeed;
        }

        public void init() {
            followingBoundary = false;
            moveTo(targetEntity.hitBox.position, -movementSpeed);
            if (onEscaped != null && distanceToTargetB > desiredDistance) {
                switchState(onEscaped);
            } else {
                animationController.setAnimation(movementAnim, -1, animationListener);
            }
        }

        public boolean update(float delta) {
            if (super.update(delta)) return true;
            if (followingBoundary) return onBoundary(delta);
            else return notOnBoundary(delta);
        }

        private boolean notOnBoundary(float delta) {
            movement.set(hitBox.position).sub(targetEntity.hitBox.position);
            distanceToTarget = movement.len();
            movement.y = 0;
            movement.nor();
            forward.set(movement);
            movement.scl(movementSpeed);

            if (onEscaped != null && distanceToTarget > desiredDistance) {
                switchState(onEscaped);
                return true;
            }

            MoveCheck moveCheck = moveCheck(delta);
            if (moveCheck != null) {
                if (moveCheck.boundary) {
                    // Follow boundary
                    followingBoundary = true;
                    if (Math.abs(moveCheck.normal.x) > 1e-6) {
                        turnLeft = movement.z < 0;
                        boundaryMovement.set(movement.set(0, 0, turnLeft ? -1 : 1));
                        if (moveCheck.normal.x < 0) turnLeft = !turnLeft;
                    } else {
                        turnLeft = movement.x < 0;
                        boundaryMovement.set(movement.set(turnLeft ? -1 : 1, 0, 0));
                        if (moveCheck.normal.z > 0) turnLeft = !turnLeft;
                    }
                } else {
                    // Hit an object; follow wall
                    moveCheck.normal.rotateRad(Vector3.Y, MathUtils.HALF_PI);
                    movement.set(moveCheck.normal);
                }
                forward.set(movement);
                movement.scl(movementSpeed);
            }
            return false;
        }

        private boolean onBoundary(float delta) {
            movement.set(hitBox.position).sub(targetEntity.hitBox.position);
            distanceToTarget = movement.len();
            movement.set(boundaryMovement);
            forward.set(movement);
            movement.scl(movementSpeed);

            if (onEscaped != null && distanceToTarget > desiredDistance) {
                switchState(onEscaped);
                return true;
            }

            MoveCheck moveCheck = moveCheck(delta);
            if (moveCheck != null) {
                if (moveCheck.boundary && Math.abs(moveCheck.normal.dot(boundaryMovement)) > 1e-6) {
                    // Follow boundary
                    float turn = turnLeft ? -MathUtils.HALF_PI : MathUtils.HALF_PI;
                    boundaryMovement.rotateRad(Vector3.Y, turn);
                    movement.set(boundaryMovement);
                } else {
                    // NOTE: This should never happen due to map generation.
                    // Hit an object; follow wall
                    moveCheck.normal.rotateRad(Vector3.Y, MathUtils.HALF_PI);
                    movement.set(moveCheck.normal);
                }
                forward.set(movement);
                movement.scl(movementSpeed);
            }
            return false;
        }
    }

    class RemoveSelfState extends State {
        @Override
        void init() {
            remove();
        }
    }
}
