package io.github.necrashter.natural_revenge.world.levels;

import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector3;

import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.Perlin;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.Octree;
import io.github.necrashter.natural_revenge.world.Terrain;

public class LevelMenuBg extends GameWorld {
    private final Vector3 camDir = new Vector3();

    public LevelMenuBg(Main game) {
        super(game, -1, 1f);

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

        Terrain.CircleAreas spawnPoints = terrain.newCircleAreas();
        spawnPoints.add(0, 0, 6);

        generateForest(spawnPoints, 180+150, 2.0f);

        camDir.set(cam.direction);
        camDir.y = 0f;
        paused = false;
    }

    @Override
    public void gameUpdate(float dt) {
        cam.position.x = MathUtils.sin(MathUtils.PI2*time*.02f)*30f;
        cam.position.z = MathUtils.cos(MathUtils.PI2*time*.02f)*30f;
        camDir.set(cam.position);
        camDir.x += MathUtils.sin(MathUtils.PI2*time*.04f)*10f;
        camDir.y -= 10f;
        camDir.z += MathUtils.cos(MathUtils.PI2*time*.04f)*10f;
        cam.up.set(Vector3.Y);
        cam.lookAt(camDir);
        cam.update();
        super.gameUpdate(dt);
    }
}
