package io.github.necrashter.natural_revenge.world.player;

public class Statistics {
    public String bestAccuracyName = "None";
    public float bestAccuracy = 0f;

    public String mostDamageName = "None";
    public float mostDamage = 0f;

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
}
