package io.github.necrashter.natural_revenge.world.geom;

import com.badlogic.gdx.math.Vector3;

public class GridRayIt {
    public Vector3 rayStart = new Vector3();
    public Vector3 rayDir = new Vector3();
    public int gridX, gridY;
    public float t;
    private int xDir, yDir;

    public void prepare() {
        gridX = (int) rayStart.x;
        gridY = (int) rayStart.z;
        xDir = (int) Math.signum(rayDir.x);
        yDir = (int) Math.signum(rayDir.z);
        t = 0;
    }

    public boolean next() {
        float t = Float.POSITIVE_INFINITY;
        int newX = gridX;
        int newY = gridY;
        if (xDir != 0) {
            newX = gridX + xDir;
            t = ( ((float)newX) - rayStart.x) / rayDir.x;
        }
        if (yDir != 0) {
            float newT = ( ((float)gridY + yDir) - rayStart.z) / rayDir.z;
            if (newT < t) {
                t = newT;
                newX = gridX;
                newY = gridY + yDir;
            }
        }
        if (t != Float.POSITIVE_INFINITY) {
            rayStart.mulAdd(rayDir, t);
            this.t += t;
            gridX = newX;
            gridY = newY;
            return true;
        } else {
            return false;
        }
    }
}
