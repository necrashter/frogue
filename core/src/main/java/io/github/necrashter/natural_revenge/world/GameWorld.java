package io.github.necrashter.natural_revenge.world;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.necrashter.natural_revenge.GameScreen;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.decals.DecalPool;
import io.github.necrashter.natural_revenge.world.entities.GameEntity;
import io.github.necrashter.natural_revenge.world.geom.RayIntersection;
import io.github.necrashter.natural_revenge.objectives.LevelObjective;
import io.github.necrashter.natural_revenge.world.levels.ScriptedEvent;
import io.github.necrashter.natural_revenge.world.objects.FrogParticle;
import io.github.necrashter.natural_revenge.world.objects.TreeObject;
import io.github.necrashter.natural_revenge.world.player.Player;
import io.github.necrashter.natural_revenge.world.player.Statistics;

public class GameWorld implements GameWorldRenderer {
    public final Main game;
    public GameScreen screen = null;
    public final int level;

    public PerspectiveCamera cam;
    public boolean paused = true;
    public float time = 0.0f;
    Viewport viewport;
    public ModelBatch modelBatch;
    public DecalBatch decalBatch;
    public DecalPool decalPool = new DecalPool();
    public Environment environment;
    public int visibleCount;

    public Octree octree;
    public Player player;
    public Terrain terrain;

    public float viewDistance = 25.f;

    // Pools (not all of them are initialized in every level)
    public FrogParticle.Pool frogParticlePool;

    public final Statistics statistics = new Statistics();

    public final float easiness;

    public GameWorld(final Main game, int level, float easiness) {
        this.game = game;
        this.level = level;
        this.easiness = easiness;

        cam = new PerspectiveCamera(Main.fov, 1280, 720);
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0,0,0);
        cam.near = 0.06f;
        cam.far = viewDistance;
        cam.update(); // fov is updated

//        viewport = new ScreenViewport(cam);

//        String vert = Gdx.files.internal("shaders/l.vert").readString();
//        String frag = Gdx.files.internal("shaders/l.frag").readString();
//        modelBatch = new ModelBatch(vert, frag);
        modelBatch = new ModelBatch();

        decalBatch = new DecalBatch(new CameraGroupStrategy(cam));

        environment = new Environment();
    }

    /**
     * Called when the class is added to a GameScreen instance.
     */
    public void addedToScreen() {

    }

    public RayIntersection intersectRay(Ray ray) {
        float t = terrain.intersectRay(ray);
        Octree.rayIntersection.reset();
        if (t < Float.POSITIVE_INFINITY) Octree.rayIntersection.setTerrain(t);
        Octree.ignoredEntity = null;
        return octree.intersectRayManual(ray);
    }

    public RayIntersection intersectRay(Ray ray, GameEntity ignore) {
        float t = terrain.intersectRay(ray);
        Octree.rayIntersection.reset();
        if (t < Float.POSITIVE_INFINITY) Octree.rayIntersection.setTerrain(t);
        Octree.ignoredEntity = ignore;
        return octree.intersectRayManual(ray);
    }

    /**
     * Physics are simulated at this delta time or better.
     */
    public static final float MAX_DELTA = 1/60.0f;
    public static final float MIN_DELTA = 1/1000.0f;
    public static final float DELTA_LIMIT = 4/30.0f;
    public float accumulatedDelta = 0.0f;

    public static final float STAT_PERIOD = 1f;
    private float statTimer = 0f;

    public void gameUpdate(float dt) {
        octree.update(dt);
        decalPool.update(cam, dt);
        if (scriptedEvent != null && scriptedEvent.update(dt)) {
            scriptedEvent = null;
        }
        if (objective != null) {
            objective.update(dt);
        }
        if ((statTimer += dt) >= STAT_PERIOD) {
            statistics.updateRecorders();
            statTimer -= STAT_PERIOD;
        }
    }

    public void update(float delta) {
        if (paused) return;
        delta = Math.min(DELTA_LIMIT, delta);
        accumulatedDelta += delta;
        while (accumulatedDelta > MIN_DELTA) {
            float dt = Math.min(accumulatedDelta, MAX_DELTA);

            gameUpdate(dt);

            accumulatedDelta -= dt;
            time += dt;
        }
    }

    public void render() {
//        viewport.apply();
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//        Gdx.gl.glClearColor(, 1);
        ScreenUtils.clear(0.38f, 0.55f, 0.42f, 1, true);
//        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);

        modelBatch.begin(cam);

        visibleCount = 0;
//        player.renderViewModel(this);

        octree.render(this);
        octree.renderEntities(this);
        terrain.render(cam, modelBatch.getRenderContext());
        decalPool.render(this);
        modelBatch.end();

        decalBatch.flush();
    }

    public void renderViewModel() {
        ScreenUtils.clear(0f, 0f, 0f, 0.0f, true);

        modelBatch.begin(cam);
        if (player != null) {
            player.renderViewModel(this);
        }
        decalPool.render(this);
        modelBatch.end();
        decalBatch.flush();
    }

    public void screenResize(int width, int height) {
//        viewport.update(width, height, true);
    }

    public void generateForest(Terrain.CircleAreas spawnPoints, int trees, float margin) {
        int i;
        i = spawnPoints.circles.size;
        spawnPoints.generateCircles(trees, margin);
        System.out.println("Generated: " + (spawnPoints.circles.size - i) + "/200");
        for (; i < spawnPoints.circles.size; ++i) {
            TreeObject tree = Main.assets.createTree();
            tree.model.transform
                    .translate(spawnPoints.getCenterOnTerrain(i))
                    .rotate(Vector3.Y, MathUtils.random(360));
            octree.add(tree);
        }
    }

    public static final Vector2 tempPoint = new Vector2();
    public Vector2 randomPointOutsideView(float margin) {
        float maxDist2 = viewDistance +1.0f;
        maxDist2 *= viewDistance;
        while (true) {
            tempPoint.set(terrain.randomX(margin), terrain.randomZ(margin));
            float dx = tempPoint.x - cam.position.x;
            float dy = tempPoint.y - cam.position.z;
            float distance2 = dx*dx + dy*dy;
            if (distance2 > maxDist2)
                return tempPoint;
        }
    }
    public Vector2 randomPointOutsideView() {
        return randomPointOutsideView(2.0f);
    }
    public Vector2 randomPointNearPlayer(float maxDist) {
        while (true) {
            double angle = MathUtils.random() * Math.PI * 2.0;
            float r = MathUtils.random(maxDist);
            tempPoint.set(
                player.hitBox.position.x + (float)(Math.sin(angle)*r),
                player.hitBox.position.z + (float)(Math.cos(angle)*r)
            );
            if (terrain.isInBoundary(tempPoint, 2f)) return tempPoint;
        }
    }
    public Vector2 randomPointNearPlayer(float minDist, float maxDist) {
        while (true) {
            double angle = MathUtils.random() * Math.PI * 2.0;
            float r = MathUtils.random(minDist, maxDist);
            tempPoint.set(
                player.hitBox.position.x + (float)(Math.sin(angle)*r),
                player.hitBox.position.z + (float)(Math.cos(angle)*r)
            );
            if (terrain.isInBoundary(tempPoint, 2f)) return tempPoint;
        }
    }

    public Vector2 inFrontOfPlayer(float dist) {
        tempPoint.set(cam.direction.x, cam.direction.z).nor().scl(dist).add(cam.position.x, cam.position.z);
        return tempPoint;
    }

    @Override
    public void dispose() {
        octree.clear();
        octree.clearEntities();
        terrain.dispose();
        decalBatch.dispose();
        modelBatch.dispose();
    }

    /* Sounds */

    public static final Vector3 soundDir = new Vector3();

    /**
     * Play sound at position with attenuation.
     * @param sound Sound to play
     * @param position Position of sound source
     * @return Sound ID
     */
    public long playSound(Sound sound, Vector3 position) {
        soundDir.set(position).sub(cam.position);
        float volume = (float) Math.pow(1.1f, -soundDir.len());
        return sound.play(volume * Main.sfxVolume);
    }

    public long playSound(Sound sound, Vector3 position, float volume) {
        soundDir.set(position).sub(cam.position);
        volume *= (float) Math.pow(1.1f, -soundDir.len());
        return sound.play(volume * Main.sfxVolume);
    }

    /* HUD */

    public void buildHudText(StringBuilder stringBuilder) {
        if (player != null) player.buildHudText(stringBuilder);
        if (objective != null) objective.buildHudText(stringBuilder);
    }

    /* Cutscenes */
    /* Scripted Events */

    private ScriptedEvent scriptedEvent = null;

    public void setScriptedEvent(ScriptedEvent scriptedEvent) {
        this.scriptedEvent = scriptedEvent;
        scriptedEvent.activate();
    }

    /**
     * Cutscene is a sequence of ScriptedEvents during which player input is ignored.
     */
    public class Cutscene implements ScriptedEvent {
        private ScriptedEvent[] events;
        private ScriptedEvent currentEvent = null;
        private int currentEventIndex;

        public Cutscene(ScriptedEvent[] events) {
            this.events = events;
        }

        private boolean activateNextEvent() {
            ++currentEventIndex;
            if (currentEventIndex >= events.length) {
                return false;
            } else {
                currentEvent = events[currentEventIndex];
                currentEvent.activate();
                return true;
            }
        }

        @Override
        public void activate() {
            player.inputAdapter.disabled = true;
            currentEventIndex = -1;
            activateNextEvent();
        }

        @Override
        public boolean update(float delta) {
            if (currentEvent.update(delta) && !activateNextEvent()) {
                player.inputAdapter.disabled = false;
                return true;
            }
            return false;
        }
    }

    public Cutscene cutscene(ScriptedEvent... events) {
        return new Cutscene(events);
    }

    /* Objectives */
    private LevelObjective objective = null;

    public void setObjective(LevelObjective objective) {
        objective.init();
        this.objective = objective;
    }

    public void removeObjective() {
        this.objective = null;
    }

    public LevelObjective getObjective() {
        return objective;
    }

}
