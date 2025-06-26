package io.github.necrashter.natural_revenge.world.levels;

import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector3;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.Perlin;
import io.github.necrashter.natural_revenge.objectives.LevelObjective;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.Octree;
import io.github.necrashter.natural_revenge.world.Terrain;
import io.github.necrashter.natural_revenge.world.entities.PistolZombie;
import io.github.necrashter.natural_revenge.world.entities.RifleZombie;
import io.github.necrashter.natural_revenge.world.entities.NPC;
import io.github.necrashter.natural_revenge.world.entities.ZombieBoss1;
import io.github.necrashter.natural_revenge.world.objects.FrogParticle;
import io.github.necrashter.natural_revenge.world.objects.RandomGunPickup;
import io.github.necrashter.natural_revenge.world.player.Player;

public class Level3ZombieBoss extends GameWorld {
    ZombieBoss1 zombie1;
    NPC currentBoss;
    PistolZombie.Pool pistolPool;
    RifleZombie.Pool riflePool;
    public Level3ZombieBoss(Main game, int level, float easiness) {
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
        for (int i = 0; i<6; ++i) player.addWeapon(RandomGunPickup.generateWeapon(Main.randomRoller), true);

        Terrain.CircleAreas spawnPoints = terrain.newCircleAreas();
        spawnPoints.add(0, 0, 6);

        generateForest(spawnPoints, 180+150, 2.0f);


        pistolPool = new PistolZombie.Pool(this, 32);
        riflePool = new RifleZombie.Pool(this, 32);
        for (int i = 0; i<3; ++i) player.addWeapon(RandomGunPickup.generateWeapon(Main.randomRoller), false);

        frogParticlePool = new FrogParticle.Pool(32);

        zombie1 = new ZombieBoss1(this);

        phaseCountdown = new PhaseCountdownObjective();
        clearPhaseObjective = new ClearPhaseObjective();
        killObjective = new KillObjective();

        bossPhases = new BossPhase[]{
            new WaitPhase(3f),
            new ZombiePhase(5,0,15f),
            new BossPhase(zombie1, 250f),
            new ZombiePhase(4,0,15f),
            new BossPhase(zombie1, 300f),
            new WaitPhase(3f),
            new BossPhase(zombie1, 100f),
            new WaitPhase(3f),
            new BossPhase(zombie1, 100f),
            new WaitPhase(3f),
            new BossPhase(zombie1, 100f),
            new ZombiePhase(5,0,25f),
            new BossPhase(zombie1, 300f) {
                @Override
                void init() {
                    super.init();
                    zombie1.deathAnim = "die";
                    zombie1.removeOnDeath = false;
                }
            },
        };
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
                screen.addProgress("Frog Boss", baseBossHealth);
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
            pistolPool.spawn(walkingCount, health);
            riflePool.spawn(runningCount, health);
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

    }

    @Override
    public void addedToScreen() {
        this.setScriptedEvent(new Cutscene(new ScriptedEvent[]{
            screen.subtitle("Your legs are broken.\nIt seems you are having trouble walking."),
            screen.subtitle("Luckily you have your special propulsion rifle!\nNow you have a way out of here."),
            screen.subtitle("Try moving using your gun.\nGood luck!"),
            new ScriptedEvent.OneTimeEvent() {
                @Override
                public void activate() {
                    setObjective(phaseCountdown);
                }
            }
        } ));
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
            stringBuilder.append("Phase: ").append(phase);
            stringBuilder.append('\n');
            stringBuilder.append("Next phase in ").append(MathUtils.ceil(remainingTime));
        }
    }

    class ClearPhaseObjective implements LevelObjective {
        int remaining = 0;
        @Override
        public void init() {
            remaining = pistolPool.getAlive() + riflePool.getAlive();
        }

        @Override
        public void update(float delta) {
            remaining = pistolPool.getAlive() + riflePool.getAlive();
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
}
