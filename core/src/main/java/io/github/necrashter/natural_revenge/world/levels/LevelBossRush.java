package io.github.necrashter.natural_revenge.world.levels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.Perlin;
import io.github.necrashter.natural_revenge.objectives.LevelObjective;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.Octree;
import io.github.necrashter.natural_revenge.world.Terrain;
import io.github.necrashter.natural_revenge.world.entities.Frog1;
import io.github.necrashter.natural_revenge.world.entities.Frog1Ranged;
import io.github.necrashter.natural_revenge.world.entities.Frog2;
import io.github.necrashter.natural_revenge.world.entities.Frog2Ranged;
import io.github.necrashter.natural_revenge.world.entities.Frog2Spinner;
import io.github.necrashter.natural_revenge.world.entities.NPC;
import io.github.necrashter.natural_revenge.world.entities.Zombie;
import io.github.necrashter.natural_revenge.world.objects.FrogParticle;
import io.github.necrashter.natural_revenge.world.objects.RandomGunPickup;
import io.github.necrashter.natural_revenge.world.player.Player;
import io.github.necrashter.natural_revenge.world.player.Statistics;

public class LevelBossRush extends GameWorld {
    Zombie.Pool zombiePool;
    Frog1 frog1;
    Frog1Ranged frog1ranged;
    Frog2 frog2;
    Frog2Ranged frog2ranged;
    Frog2Spinner frog2Spinner;
    NPC currentBoss;

    int phase = 0;

    PhaseCountdownObjective phaseCountdown;
    ClearPhaseObjective clearPhaseObjective;
    KillObjective killObjective;
    float baseBossHealth = 0f;
    boolean previousPhaseBoss = false;

    public LevelBossRush(Main game, int level, float easiness) {
        super(game, level, easiness);

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.25f, 0.2f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.38f, 0.55f, 0.42f, 1f));
        environment.add(new DirectionalLight().set(0.6f, 0.6f, 0.6f, -1f, -0.8f, -0.2f));

        RandomXS128 random = new RandomXS128(64);
        Perlin perlin = new Perlin();
        perlin.xShift = random.nextFloat() * 100.0f;
        perlin.yShift = random.nextFloat() * 100.0f;

        terrain = new Terrain(environment, perlin, 100, 100, random);
        octree = new Octree(this, new Vector3(0, 0, 0), Math.max(terrain.width, terrain.height));

        player = new Player(this);
        player.setPosition(-2, 0);
        octree.add(player);

        Terrain.CircleAreas spawnPoints = terrain.newCircleAreas();
        spawnPoints.add(0, 0, 6);
        generateForest(spawnPoints, 330, 2.0f);

        for (int i = 0; i < 6; ++i)
            player.addWeapon(RandomGunPickup.generateWeapon(Main.randomRoller), true);

        frogParticlePool = new FrogParticle.Pool(32);

        frog1 = new Frog1(this);
        frog1ranged = new Frog1Ranged(this);
        frog2 = new Frog2(this);
        frog2ranged = new Frog2Ranged(this);
        frog2Spinner = new Frog2Spinner(this);
        zombiePool = new Zombie.Pool(this, 32);

        phaseCountdown = new PhaseCountdownObjective();
        clearPhaseObjective = new ClearPhaseObjective();
        killObjective = new KillObjective();

        statistics.recorders.add(new Statistics.FloatRecorder("Boss Health", Color.GREEN) {
            @Override
            protected void update() {
                array.add(getBossHealth());
            }
        });
    }

    private BossPhase generatePhasesForPhase() {
        int roll = MathUtils.random(0, 7);
        if (previousPhaseBoss && roll < 5) {
            roll = 5;
        }
        switch (roll) {
            case 0:
                return new BossPhase(frog1, MathUtils.random(100f, 300f));
            case 1:
                return new BossPhase(frog1ranged, MathUtils.random(100f, 400f));
            case 2:
                return new BossPhase(frog2, MathUtils.random(100f, 400f));
            case 3:
                return new BossPhase(frog2ranged, MathUtils.random(100f, 400f));
            case 4:
                return new BossPhase(frog2Spinner, MathUtils.random(250f, 500f));
            case 5:
                return new WaitPhase(MathUtils.random(1f, 5f));
            default:
                return new ZombiePhase(
                    MathUtils.random(1, 12),
                    MathUtils.random(1, 6),
                    MathUtils.random(10f, 40f)
                );
        }
    }

    void initiatePhase() {
        BossPhase bossPhase = generatePhasesForPhase();
        bossPhase.init();
        if (bossPhase.npc != null) {
            previousPhaseBoss = true;
            screen.addProgress("Frog Threat", bossPhase.npc.maxHealth);
            screen.progressBar.setValue(bossPhase.npc.maxHealth);
            currentBoss = bossPhase.npc;
            killObjective.target = bossPhase.npc;
            killObjective.desc = "A FROG!?";
            setObjective(killObjective);
        } else {
            previousPhaseBoss = false;
        }
    }

    private void phaseDone() {
        screen.removeProgress();
        phase += 1;
        initiatePhase();
    }

    @Override
    public void gameUpdate(float dt) {
        super.gameUpdate(dt);
        if (currentBoss != null)
            screen.progressBar.setValue(baseBossHealth + currentBoss.health);
    }

    @Override
    public void addedToScreen() {
        this.setScriptedEvent(new Cutscene(new ScriptedEvent[]{
            screen.subtitle("Welcome to the Boss Rush mode!"),
            screen.subtitle("Survive endless waves of enemies and bosses.\nGood luck!"),
            new ScriptedEvent.OneTimeEvent() {
                @Override
                public void activate() {
                    initiatePhase();
                }
            }
        }));
        Main.music.start(Main.music.song1);
    }

    static class BossPhase {
        public NPC npc;
        public float health;

        BossPhase(NPC npc, float health) {
            this.npc = npc;
            this.health = health;
        }

        void init() {
            if (npc != null) {
                npc.maxHealth = health;
                npc.spawn();
            }
        }
    }

    class ZombiePhase extends BossPhase {
        int walkingCount, runningCount;
        float health;

        ZombiePhase(int walkingCount, int runningCount, float health) {
            super(null, 0f);
            this.walkingCount = walkingCount;
            this.runningCount = runningCount;
            this.health = health;
        }

        @Override
        void init() {
            zombiePool.spawn(walkingCount, runningCount, health);
            setObjective(clearPhaseObjective);
        }
    }

    class WaitPhase extends BossPhase {
        float time;

        WaitPhase(float time) {
            super(null, 0f);
            this.time = time;
        }

        @Override
        void init() {
            phaseCountdown.countdownTime = time;
            setObjective(phaseCountdown);
        }
    }

    class PhaseCountdownObjective implements LevelObjective {
        float countdownTime = 2.0f;
        float remainingTime;

        @Override
        public void init() {
            remainingTime = countdownTime;
        }

        @Override
        public void update(float delta) {
            remainingTime -= delta;
            if (remainingTime < 0) {
                phaseDone();
            }
        }

        @Override
        public void buildHudText(StringBuilder stringBuilder) {
            stringBuilder.append("Phase: ").append(phase).append("\n");
            stringBuilder.append("Next phase in ").append(MathUtils.ceil(remainingTime));
        }
    }

    class ClearPhaseObjective implements LevelObjective {
        int remaining = 0;

        @Override
        public void init() {
            remaining = zombiePool.getAlive();
        }

        @Override
        public void update(float delta) {
            remaining = zombiePool.getAlive();
            if (remaining == 0) {
                phaseDone();
            }
        }

        @Override
        public void buildHudText(StringBuilder stringBuilder) {
            stringBuilder.append("Zombies!\nRemaining: ").append(remaining);
        }
    }

    class KillObjective implements LevelObjective {
        NPC target;
        String desc;

        @Override
        public void init() {}

        @Override
        public void update(float delta) {
            if (target.dead) {
                phaseDone();
            }
        }

        @Override
        public void buildHudText(StringBuilder stringBuilder) {
            stringBuilder.append(desc);
        }
    }

    protected float getBossHealth() {
        if (currentBoss != null) return baseBossHealth + currentBoss.health;
        else return baseBossHealth;
    }
}
