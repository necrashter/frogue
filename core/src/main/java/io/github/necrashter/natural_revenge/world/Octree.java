package io.github.necrashter.natural_revenge.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;
import io.github.necrashter.natural_revenge.world.geom.CharHitBox;
import io.github.necrashter.natural_revenge.world.geom.RayIntersection;

public class Octree {
    private static final Vector3 temp1 = new Vector3();
    private static final Vector3 minPosTemp = new Vector3();
    private static final Vector3 maxPosTemp = new Vector3();

    private static float rayDivX;
    private static float rayDivY;
    private static float rayDivZ;

    public static RayIntersection rayIntersection = new RayIntersection();
    public static GameEntity ignoredEntity = null;

    /**
     * Split octree node when it contains more than this many elements;
     */
    public static final int SPLIT_OBJECTS = 5;
    public static final float MIN_SIZE = 0.01f;

    public static class OctreeNode {
        public final Vector3 center;
        public final float size;
        public final OctreeNode parent;

        private OctreeNode[] children = null;
        Array<GameObject> objects = new Array<>();
        Array<GameEntity> entities = new Array<>();

        public OctreeNode(OctreeNode parent, Vector3 center, float size) {
            this.parent = parent;
            this.center = center;
            this.size = size;
        }

        public void split() {
            assert children == null;
            children = new OctreeNode[8];
            float childSize = size / 2.0f;
            for (int i = 0; i < 8; ++i) {
                Vector3 position = new Vector3(
                        center.x + ((i & 1) == 0 ? -childSize : childSize),
                        center.y + ((i & 2) == 0 ? -childSize : childSize),
                        center.z + ((i & 4) == 0 ? -childSize : childSize)
                );
                children[i] = new OctreeNode(this, position, childSize);
            }
            if (objects.size > 0) {
                Array<GameObject> oldObjects = objects;
                objects = new Array<>();
                for (GameObject object: oldObjects) {
                    addObject(object);
                }
                oldObjects.clear();
            }
        }

        /**
         * Get the index of the child which includes the given point.
         * @param p Point
         * @return index of the child [0..8)
         */
        public int getChild(Vector3 p) {
            return ((p.x < center.x) ? 0 : 1) +
                   ((p.y < center.y) ? 0 : 2) +
                   ((p.z < center.z) ? 0 : 4);
        }

        public void addObject(GameObject object, Vector3 minPos, Vector3 maxPos) {
//            assert inBounds(minPos, maxPos);
            if (children == null) {
                objects.add(object);
                object.octreeNode = this;
                if (objects.size >= SPLIT_OBJECTS && size > MIN_SIZE) {
                    split();
                }
                return;
            }
            int mini = getChild(minPos);
            int maxi = getChild(maxPos);
            if (mini == maxi) {
                children[mini].addObject(object, minPos, maxPos);
            } else {
                objects.add(object);
                object.octreeNode = this;
            }
        }

        public void addObject(GameObject object) {
            object.getMinPoint(minPosTemp);
            object.getMaxPoint(maxPosTemp);
            addObject(object, minPosTemp, maxPosTemp);
        }

        public void clearObjects() {
            objects.clear();
            if (children != null) {
                for (OctreeNode child: children) child.clearObjects();
                children = null;
            }
        }

        public void clearEntities() {
            entities.clear();
            if (children != null) {
                for (OctreeNode child: children) child.clearEntities();
                children = null;
            }
        }

        public boolean checkRay(Ray ray) {
            float minx = ((center.x - size) - ray.origin.x) * rayDivX;
            float maxx = ((center.x + size) - ray.origin.x) * rayDivX;
            if (minx > maxx) {
                final float t = minx;
                minx = maxx;
                maxx = t;
            }

            float miny = ((center.y - size) - ray.origin.y) * rayDivY;
            float maxy = ((center.y + size) - ray.origin.y) * rayDivY;
            if (miny > maxy) {
                final float t = miny;
                miny = maxy;
                maxy = t;
            }

            float minz = ((center.z - size) - ray.origin.z) * rayDivZ;
            float maxz = ((center.z + size) - ray.origin.z) * rayDivZ;
            if (minz > maxz) {
                final float t = minz;
                minz = maxz;
                maxz = t;
            }

            float min = Math.max(Math.max(minx, miny), minz);
            float max = Math.min(Math.min(maxx, maxy), maxz);

            return max >= 0 && max >= min && min < rayIntersection.t;
        }

        public boolean intersectRay(Ray ray) {
            if (!checkRay(ray))
                return false;

            boolean intersected = false;

            for (int i = 0; i < objects.size; ++i) {
                final GameObject object = objects.get(i);

                float t = object.intersectsGetRayT(ray);
                if (t < rayIntersection.t) {
                    rayIntersection.setTarget(t, object);
                    intersected = true;
                }
            }

            for (int i = 0; i < entities.size; ++i) {
                final GameEntity entity = entities.get(i);
                if (entity == ignoredEntity) continue;

                float t = entity.hitBox.intersectRay(ray);
                if (t < rayIntersection.t) {
                    rayIntersection.setTarget(t, entity);
                    intersected = true;
                }
            }

            if (children != null) {
                for (int i = 0; i < 8; ++i) {
                    boolean out = children[i].intersectRay(ray);
                    intersected = intersected || out;
                }
            }

            return intersected;
        }

        public void getClosestPoint(Vector3 storage, Vector3 p) {
            storage.set(
                    MathUtils.clamp(p.x, center.x - size, center.x + size),
                    MathUtils.clamp(p.y, center.y - size, center.y + size),
                    MathUtils.clamp(p.z, center.z - size, center.z + size)
            );
        }

        private boolean isVisible(GameWorld world) {
            getClosestPoint(temp1, world.cam.position);
            if (temp1.sub(world.cam.position).len2() > world.viewDistance * world.viewDistance) {
                return false;
            }
            return true;
//            return world.cam.frustum.boundsInFrustum(center.x, center.y, center.z, size, size, size);
        }

        public void render(GameWorld world) {
            if (!isVisible(world)) return;

            for (final GameObject object: objects) {
                object.render(world);
            }

            if (children != null) {
                for (int i = 0; i < 8; ++i) {
                    children[i].render(world);
                }
            }
        }

        /**
         * Checks whether box defined by min and max pos is in bounds of octree node.
         */
        public boolean inBounds(Vector3 minPos, Vector3 maxPos) {
            return minPos.x < center.x + size
                    && minPos.x > center.x - size
                    && minPos.y < center.y + size
                    && minPos.y > center.y - size
                    && minPos.z < center.z + size
                    && minPos.z > center.z - size
                    && maxPos.x < center.x + size
                    && maxPos.x > center.x - size
                    && maxPos.y < center.y + size
                    && maxPos.y > center.y - size
                    && maxPos.z < center.z + size
                    && maxPos.z > center.z - size
                    ;
        }

        /**
         * Checks whether box defined by min and max pos intersects octree node.
         */
        public boolean intersectsBounds(Vector3 minPos, Vector3 maxPos) {
            // test using separating axis theorem
            float lx = Math.abs(this.center.x - (maxPos.x + minPos.x)/2.0f);
            float sumX = size + (maxPos.x - minPos.x) / 2.0f;

            float ly = Math.abs(this.center.y - (maxPos.y + minPos.y)/2.0f);
            float sumY = size + (maxPos.y - minPos.y) / 2.0f;

            float lz = Math.abs(this.center.z - (maxPos.z + minPos.z)/2.0f);
            float sumZ = size + (maxPos.z - minPos.z) / 2.0f;

            return (lx <= sumX && ly <= sumY && lz <= sumZ);
        }

        public boolean intersects(CharHitBox hitBox) {
            Vector3 s = hitBox.getClosestSphere(center.y - size);
            getClosestPoint(temp1, s);
            return temp1.sub(s).len2() <= hitBox.radius * hitBox.radius;
        }

        public void collide(GameEntity entity, Vector3 minPos, Vector3 maxPos) {
            if (!intersects(entity.hitBox)) return;

            for (final GameObject object: objects) {
                object.hit(entity);
            }

            if (children != null) {
//                int mini = getChild(minPos);
//                int maxi = getChild(maxPos);
//                int incr = mini ^ maxi;
                // TODO;
                // In edge case mini = 000, maxi = 101; 010 and 011 will be checked redundantly
//                incr = ((incr&1) != 0) ? 1 : ((incr&2) != 0) ? 2 : ((incr&4) != 0) ? 4 : 8;

                int mini = 0;
                int maxi = 7;
                int incr = 1;

                for (int i = mini; i <= maxi; i += incr) {
                    children[i].collide(entity, minPos, maxPos);
                }
            }
        }

        public boolean moveCheck(GameEntity.MoveCheck moveCheck, Vector3 minPos, Vector3 maxPos) {
            if (!intersects(moveCheck.hitBox)) return false;

            for (final GameObject object: objects) {
                if (object.moveCheck(moveCheck)) return true;
            }

            if (children != null) {
//                int mini = getChild(minPos);
//                int maxi = getChild(maxPos);
//                int incr = mini ^ maxi;
                // TODO;
                // In edge case mini = 000, maxi = 101; 010 and 011 will be checked redundantly
//                incr = ((incr&1) != 0) ? 1 : ((incr&2) != 0) ? 2 : ((incr&4) != 0) ? 4 : 8;

                int mini = 0;
                int maxi = 7;
                int incr = 1;

                for (int i = mini; i <= maxi; i += incr) {
                    if (children[i].moveCheck(moveCheck, minPos, maxPos)) return true;
                }
            }
            return false;
        }

        public void printObjectCounts(int depth) {
            if (objects.size > 0 || children != null) {
            for (int i = 0; i < depth; ++i) System.out.print('\t');
            System.out.println(objects.size);
            }
            if (children != null) for (int i = 0; i < 8; ++i) {
                children[i].printObjectCounts(depth + 1);
            }
        }

        public void addEntity(GameEntity entity, Vector3 minPos, Vector3 maxPos) {
            if (children == null) {
                entity.octreeNode = this;
                entities.add(entity);
                return;
            }
            int mini = getChild(minPos);
            int maxi = getChild(maxPos);
            if (mini == maxi) {
                children[mini].addEntity(entity, minPos, maxPos);
            } else {
                entity.octreeNode = this;
                entities.add(entity);
            }
        }

        public void updateEntity(GameEntity entity) {
            entity.hitBox.getMinPoint(minPosTemp);
            entity.hitBox.getMaxPoint(maxPosTemp);
            updateEntity(entity, minPosTemp, maxPosTemp);
        }

        public void updateEntity(GameEntity entity, Vector3 minPos, Vector3 maxPos) {
            if (inBounds(minPos, maxPos)) {
                if (children == null) return;
                int mini = getChild(minPos);
                int maxi = getChild(maxPos);
                if (mini == maxi) {
                    entities.removeValue(entity, true);
                    children[mini].addEntity(entity, minPos, maxPos);
                }
            } else if (parent != null) {
                entities.removeValue(entity, true);
                parent.ascendEntity(entity, minPos, maxPos);
            }
        }

        private void ascendEntity(GameEntity entity, Vector3 minPos, Vector3 maxPos) {
            if (parent != null && !inBounds(minPos, maxPos)) {
                parent.ascendEntity(entity, minPos, maxPos);
            } else {
                addEntity(entity, minPos, maxPos);
            }
        }

        public void updateObject(GameObject object) {
            object.getMinPoint(minPosTemp);
            object.getMaxPoint(maxPosTemp);
            updateObject(object, minPosTemp, maxPosTemp);
        }

        public void updateObject(GameObject object, Vector3 minPos, Vector3 maxPos) {
            if (inBounds(minPos, maxPos)) {
                if (children == null) return;
                int mini = getChild(minPos);
                int maxi = getChild(maxPos);
                if (mini == maxi) {
                    objects.removeValue(object, true);
                    children[mini].addObject(object, minPos, maxPos);
                }
            } else if (parent != null) {
                objects.removeValue(object, true);
                parent.ascendObject(object, minPos, maxPos);
            }
        }

        private void ascendObject(GameObject object, Vector3 minPos, Vector3 maxPos) {
            if (parent != null && !inBounds(minPos, maxPos)) {
                parent.ascendObject(object, minPos, maxPos);
            } else {
                addObject(object, minPos, maxPos);
            }
        }

        /**
         * Entity collision within an octree node.
         */
        public void entityCollisions() {
            for (int i = 0; i < entities.size; ++i) {
                final GameEntity a = entities.get(i);
                for (int j = i+1; j < entities.size; ++j) {
                    a.hit(entities.get(j));
                }
            }
            if (children != null) {
                for (final OctreeNode node: children) {
                    node.entityCollisions();
                }
                for (final GameEntity e: entities) {
                    for (final OctreeNode node: children) {
                        node.entityCollisions(e);
                    }
                }
            }
        }

        /**
         * Entity collisions between the given entity and the entities in the octree node.
         * @param entity Entity
         */
        private void entityCollisions(GameEntity entity) {
            for (final GameEntity e: entities) {
                entity.hit(e);
            }
            if (children != null) {
                for (final OctreeNode node: children) {
                    node.entityCollisions(entity);
                }
            }
        }

        public void getClosestEntity(GameEntity source, Vector3 minPos, Vector3 maxPos, GameEntity.FilterFunction f) {
            if (!intersectsBounds(minPos, maxPos)) return;

            for (int i = 0; i < entities.size; ++i) {
                final GameEntity entity = entities.get(i);
                if (entity != source) {
                    float d2 = temp1.set(entity.hitBox.position).sub(source.hitBox.position).len2();
                    if (d2 < closestDist2 && f.check(entity)) {
                        closestDist2 = d2;
                        closestEntity = entity;
                    }
                }
            }

            if (children != null) {
//                int mini = getChild(minPos);
//                int maxi = getChild(maxPos);
//                int incr = mini ^ maxi;
                // TODO;
                // In edge case mini = 000, maxi = 101; 010 and 011 will be checked redundantly
//                incr = ((incr&1) != 0) ? 1 : ((incr&2) != 0) ? 2 : ((incr&4) != 0) ? 4 : 8;

                int mini = 0;
                int maxi = 7;
                int incr = 1;

                for (int i = mini; i <= maxi; i += incr) {
                    children[i].getClosestEntity(source, minPos, maxPos, f);
                }
            }
        }
    }

    public final GameWorld world;
    public OctreeNode node;
    public Octree(GameWorld world, Vector3 center, float size) {
        this.world = world;
        node = new OctreeNode(null, center, size);
    }

    public void clear() {
        node.clearObjects();
    }

    public void clearEntities() {
        for (GameEntity entity: entities) {
            entity.onRemove(true);
        }
        entities.clear();
        node.clearEntities();
    }

    public Array<GameEntity> entities = new Array<>();
    /**
     * Dynamic objects require updates every frame.
     */
    public Array<GameObject> dynamicObjects = new Array<>();
    public Array<GameObject> staticObjects = new Array<>();

    public void add(GameEntity entity) {
        entity.staticCollisions();
        entity.octree = this;
        entities.add(entity);
        entity.hitBox.getMinPoint(minPosTemp);
        entity.hitBox.getMaxPoint(maxPosTemp);
        node.addEntity(entity, minPosTemp, maxPosTemp);
    }

    public void remove(GameEntity entity) {
        entities.removeValue(entity, true);
        entity.octreeNode.entities.removeValue(entity, true);
        // Only clear octree field if entity wants to be removed.
        // Otherwise, entity will add itself back to the tree.
        if (entity.onRemove(false)) entity.octree = null;
    }

    public void update(float delta) {
        for (final GameObject object: dynamicObjects) {
            object.update(delta);
        }
        for (final GameEntity entity: entities) {
            entity.update(delta);
        }
        node.entityCollisions();
    }

    public void render(GameWorld world) {
        node.render(world);
    }

    public void renderEntities(GameWorld world) {
        for (final GameEntity entity: entities) {
            entity.render(world);
        }
    }

    public RayIntersection intersectRayManual(Ray ray) {
        rayDivX = 1f / ray.direction.x;
        rayDivY = 1f / ray.direction.y;
        rayDivZ = 1f / ray.direction.z;

        node.intersectRay(ray);

        return rayIntersection;
    }

    public RayIntersection intersectRay(Ray ray, GameEntity ignore) {
        rayIntersection.reset();
        ignoredEntity = ignore;
        rayDivX = 1f / ray.direction.x;
        rayDivY = 1f / ray.direction.y;
        rayDivZ = 1f / ray.direction.z;

        node.intersectRay(ray);

        return rayIntersection;
    }

    public RayIntersection intersectRay(Ray ray) {
        return intersectRay(ray, null);
    }

    public void add(GameObject object) {
        object.world = world;
        node.addObject(object);
        if (object.requiresUpdates) {
            dynamicObjects.add(object);
        } else {
            staticObjects.add(object);
        }
    }

    public void collide(GameEntity entity) {
        entity.hitBox.getMinPoint(minPosTemp);
        entity.hitBox.getMaxPoint(maxPosTemp);
        node.collide(entity, minPosTemp, maxPosTemp);
    }

    public boolean moveCheck(GameEntity.MoveCheck moveCheck) {
        moveCheck.hitBox.getMinPoint(minPosTemp);
        moveCheck.hitBox.getMaxPoint(maxPosTemp);
        return node.moveCheck(moveCheck, minPosTemp, maxPosTemp);
    }

    private static float closestDist2;
    private static GameEntity closestEntity;

    /**
     * Return the closest entity satisfying the given function.
     * @param source The output will be closest to source entity, but not source itself.
     * @param f Filter function
     */
    public GameEntity getClosestEntity(GameEntity source, float radius, GameEntity.FilterFunction f) {
        minPosTemp.set(source.hitBox.position).sub(radius, radius, radius);
        maxPosTemp.set(source.hitBox.position).add(radius, radius, radius);
        closestEntity = null;
        closestDist2 = Float.POSITIVE_INFINITY;
        node.getClosestEntity(source, minPosTemp, maxPosTemp, f);
        return closestEntity;
    }
}
