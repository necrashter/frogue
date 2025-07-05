package io.github.necrashter.natural_revenge.world.player;
import com.badlogic.gdx.utils.Array;

import java.io.IOException;

import io.github.necrashter.natural_revenge.world.objects.RandomGunPickup;

public class EnumeratingRoller implements Roller {
    int[] outputs = new int[128];
    int[] maxValues = new int[128];
    float[] probs = new float[128];
    int length = 0;
    int current = 0;

    public void reset() {
        length = 0;
        current = 0;
    }

    private int getNext(int max) {
        if (current < length) {
            return outputs[current++];
        } else {
            outputs[current] = 0;
            maxValues[current] = max;
            current++;
            length++;
            return 0;
        }
    }

    private void recordProbability(float p) {
        probs[current-1] = p;
    }

    public float getProbability() {
        float p = 1f;
        for (int i = 0; i < length; ++i) {
            p *= probs[i];
        }
        return p;
    }

    public boolean nextItem() {
        current = 0;
        while (length > 0) {
            outputs[length - 1] += 1;
            if (outputs[length - 1] < maxValues[length - 1]) return true;
            --length;
        }
        return false;
    }

    @Override
    public WeaponMod getRandomMod(WeaponMod[] mods, Array<WeaponMod> currentMods) {
        float total = 0;
        int max = 0;
        for (WeaponMod mod : mods) {
            if (mod.once && currentMods.contains(mod, true)) continue;
            total += mod.weight;
            ++max;
        }
        if (total == 0f) return null;
        int roll = getNext(max);

        WeaponMod mod = mods[roll];
        recordProbability(mod.weight / total);
        return mod;
    }

    @Override
    public boolean getBoolean(float chance) {
        boolean out = getNext(2) == 1;
        recordProbability(out ? chance : (1f-chance));
        return out;
    }

    @Override
    public int getInt(float[] weights) {
        int out = getNext(weights.length);
        float total = 0f;
        for (float w: weights) total += w;
        recordProbability(weights[out] / total);
        return out;
    }

    public static void appendAllWeaponsCSV(Appendable appendable) throws IOException {
        EnumeratingRoller roller = new EnumeratingRoller();
        appendable.append("HashName,P,DPS,Acc1,Acc5,Acc10,Acc15,Acc25\n");
        do {
            Firearm firearm = RandomGunPickup.generateWeapon(roller);
            float p = roller.getProbability();
            float dps = firearm.computeDPS();
            float acc1 = firearm.computeAccuracy(1f, 0.375f);
            float acc5 = firearm.computeAccuracy(5f, 0.375f);
            float acc10 = firearm.computeAccuracy(10f, 0.375f);
            float acc15 = firearm.computeAccuracy(15f, 0.375f);
            float acc25 = firearm.computeAccuracy(25f, 0.375f);

            appendable.append("\"").append(firearm.hashName()).append("\",");
            appendable.append(String.valueOf(p)).append(",")
                .append(String.valueOf(dps)).append(",")
                .append(String.valueOf(acc1)).append(",")
                .append(String.valueOf(acc5)).append(",")
                .append(String.valueOf(acc10)).append(",")
                .append(String.valueOf(acc15)).append(",")
                .append(String.valueOf(acc25)).append("\n");
        } while (roller.nextItem());
    }
}
