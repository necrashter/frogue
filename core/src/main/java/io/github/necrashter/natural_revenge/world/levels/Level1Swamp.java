package io.github.necrashter.natural_revenge.world.levels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector3;
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

public class Level1Swamp extends GameWorld {
    Zombie.Pool zombiePool;
    Frog1 frog1;
    Frog1Ranged frog1ranged;
    Frog2 frog2;
    Frog2Ranged frog2ranged;
    Frog2Spinner frog2Spinner;
    NPC currentBoss;
    public Level1Swamp(Main game, int level, float easiness) {
        super(game, level, easiness);

        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.25f, 0.2f, 1f));
        environment.set(new ColorAttribute(ColorAttribute.Fog, 0.38f, 0.55f, 0.42f, 1f));

        environment.add(new DirectionalLight().set(0.6f, 0.6f, 0.6f, -1f, -0.8f, -0.2f));

        RandomXS128 random = new RandomXS128(64);
        Perlin perlin = new Perlin();
        perlin.xShift = random.nextFloat() * 100.0f;
        perlin.yShift = random.nextFloat() * 100.0f;
        terrain = new Terrain(environment, perlin, 100, 100, random);
        octree = new Octree(
            this,
            new Vector3(0, 0, 0),
            Math.max(terrain.width, terrain.height)
        );

        player = new Player(this);
        player.setPosition(-2, 0);
        octree.add(player);

        Terrain.CircleAreas spawnPoints = terrain.newCircleAreas();
        spawnPoints.add(0, 0, 6);

        generateForest(spawnPoints, 180+150, 2.0f);

        for (int i = 0; i<3; ++i) player.addWeapon(RandomGunPickup.generateWeapon(Main.randomRoller), true);

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

        bossPhases = new BossPhase[]{
            new WaitPhase(4f),
            new ZombiePhase(5,0,10f),
            new WaitPhase(3f),
            new ZombiePhase(7,0,15f),
            new WaitPhase(3f),
            new ZombiePhase(5,0,30f),
            new WaitPhase(3f),
            new BossPhase(frog1, 250f),
            new ZombiePhase(6,1,15f),
            new BossPhase(frog1, 300f),
            new WaitPhase(3f),
            new BossPhase(frog1ranged, 100f),
            new WaitPhase(3f),
            new BossPhase(frog1ranged, 100f),
            new WaitPhase(3f),
            new BossPhase(frog1ranged, 100f),
            new ZombiePhase(3,4,40f),
            new BossPhase(frog2, 300f),
            new ZombiePhase(3,4,40f),
            new BossPhase(frog2, 300f),
            new ZombiePhase(5,2,40f),
            new BossPhase(frog2, 250f),
            new ZombiePhase(7,1,40f),
            new BossPhase(frog2, 200f),
            new ZombiePhase(7,1,40f),
            new BossPhase(frog2ranged, 50f),
            new WaitPhase(1f),
            new BossPhase(frog2ranged, 50f),
            new WaitPhase(1f),
            new BossPhase(frog2ranged, 75f),
            new WaitPhase(1f),
            new BossPhase(frog2ranged, 50f),
            new WaitPhase(7f),
            new BossPhase(frog2Spinner, 300f),
            new ZombiePhase(6,3,50f),
            new BossPhase(frog2Spinner, 300f),
            new ZombiePhase(6,3,40f),
            new BossPhase(frog2Spinner, 200f),
            new ZombiePhase(6,3,40f),
            new BossPhase(frog2, 350f) {
                @Override
                void init() {
                    super.init();
                    frog2.deathAnim = "die";
                    frog2.removeOnDeath = false;
                }
            },
        };

        statistics.recorders.add(new Statistics.FloatRecorder("Boss Health", Color.GREEN) {
            @Override
            protected void update() {
                array.add(getBossHealth());
            }
        });
    }

    void initiatePhase() {
        BossPhase bossPhase = bossPhases[phase];
        bossPhase.init();
        if (bossPhase.npc != null) {
            if (Float.isNaN(baseBossHealth)) {
                baseBossHealth = 0f;
                for (BossPhase b: bossPhases) {
                    baseBossHealth += b.health;
                }
                screen.addProgress("Badass Frog", baseBossHealth);
                screen.progressBar.setValue(baseBossHealth);
            }
            baseBossHealth -= bossPhase.npc.maxHealth;
            currentBoss = bossPhase.npc;
            killObjective.target = bossPhase.npc;
            killObjective.desc = "A FROG!?";
            setObjective(killObjective);
        }
    }

    private void phaseDone() {
        phase += 1;
        if (phase < bossPhases.length) {
            initiatePhase();
        } else {
            removeObjective();
            endOfPhases();
        }
    }

    @Override
    public void gameUpdate(float dt) {
        super.gameUpdate(dt);
        if (currentBoss != null) screen.progressBar.setValue(baseBossHealth + currentBoss.health);
    }

    static class BossPhase {
        public NPC npc;
        public float health;

        BossPhase(NPC npc, float health) {
            this.npc = npc;
            this.health = health;
        }

        void init() {
            if (this.npc != null) {
                this.npc.maxHealth = health;
                this.npc.spawn();
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

    BossPhase[] bossPhases;
    float baseBossHealth = Float.NaN;

    void endOfPhases() {
        this.setScriptedEvent(new Cutscene(new ScriptedEvent[]{
            screen.subtitle("Well done!"),
            screen.winGameEvent(),
        } ));
    }

    @Override
    public void addedToScreen() {
        if (Main.debugMode) {
            setObjective(phaseCountdown);
        } else {
            this.setScriptedEvent(new Cutscene(new ScriptedEvent[]{
                screen.subtitle("Welcome to the swamp.\nWe have been dumping our waste here for centuries."),
                screen.subtitle("But the nature decided to avenge!\nNow the undead is rising here."),
                screen.subtitle("There might be other threats in the region.\nGood luck!"),
                screen.subtitle(Main.isMobile()
                    ? "Use on-screen buttons.\nDrag to look around.\nDouble-tap + drag to shoot."
                    : "Use WASD to move, mouse to aim.\nLeft mouse button: Shoot\nRight mouse button: Aim down sights\nTAB: Weapon stats\nR: Reload"),
                new ScriptedEvent.OneTimeEvent() {
                    @Override
                    public void activate() {
                        setObjective(phaseCountdown);
                    }
                }
            } ));
        }
        Main.music.start(Main.music.song1);
    }


    int phase = 0;
    PhaseCountdownObjective phaseCountdown;
    ClearPhaseObjective clearPhaseObjective;
    KillObjective killObjective;

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
            stringBuilder.append("Zombies!");
            stringBuilder.append('\n');
            stringBuilder.append("Remaining: ").append(remaining);
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
