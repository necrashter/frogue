package io.github.necrashter.natural_revenge.world.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;

import io.github.necrashter.natural_revenge.world.GameWorld;

public class Statistics {
    public String bestAccuracyName = "None";
    public float bestAccuracy = 0f;

    public String mostDamageName = "None";
    public float mostDamage = 0f;
    public int deaths = 0;

    public void update(Firearm firearm) {
        float accuracy = firearm.totalBulletsHit / firearm.totalBulletsShot;
        if (accuracy >= bestAccuracy) {
            bestAccuracyName = firearm.name;
            bestAccuracy = accuracy;
        }
        if (firearm.totalDamage >= mostDamage) {
            mostDamage = firearm.totalDamage;
            mostDamageName = firearm.name;
        }
    }

    public static abstract class FloatRecorder {
        public FloatArray array = new FloatArray(true, 1024);
        public final String name;
        public final Color color;

        protected FloatRecorder(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        protected abstract void update();
    }

    public Array<FloatRecorder> recorders = new Array<>(false, 8);

    public void updateRecorders() {
        for (int i = 0; i < recorders.size; ++i) recorders.get(i).update();
    }
}
