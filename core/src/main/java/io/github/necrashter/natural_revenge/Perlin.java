package io.github.necrashter.natural_revenge;

import static com.badlogic.gdx.math.MathUtils.floor;
import static com.badlogic.gdx.math.MathUtils.lerp;
import static java.lang.Math.abs;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Perlin {
	public float xScale = 0.125f;
	public float yScale = 0.125f;
	public float xShift = 0.0f;
	public float yShift = 0.0f;
	public float terrainHeight = 4.0f;

	final Vector2 gradients[] = new Vector2[] {
        new Vector2(1, 1),
		new Vector2(-1, 1),
		new Vector2(1, -1),
		new Vector2(-1, -1),
		new Vector2(1, 0),
		new Vector2(-1, 0),
		new Vector2(0, 1),
		new Vector2(0, -1),
    };

    final int permuted[] = new int[]{
        0, 2, 7, 3, 2, 1, 4, 3, 7, 6, 5, 0, 4, 6, 5, 1
    };

    Vector2 gradient(int x, int y) {
        return gradients[permuted[abs(y + permuted[abs(x) % permuted.length]) % permuted.length]];
    }

    public float perlin(float x, float y) {
		x = xScale * x + xShift;
		y = yScale * y + yShift;

		final float xb = floor(x);
		final float yb = floor(y);
		final float xf = x - xb;
		final float yf = y - yb;
		final int xi = (int)(xb);
		final int yi = (int)(yb);

		final Vector2 gaa = gradient(xi, yi);
		final Vector2 gab = gradient(xi, yi+1);
		final Vector2 gba = gradient(xi+1, yi);
		final Vector2 gbb = gradient(xi+1, yi+1);

		final float aa = gaa.dot(xf, yf);
		final float ab = gab.dot(xf, yf-1.0f);
		final float ba = gba.dot(xf-1.0f, yf);
		final float bb = gbb.dot(xf-1.0f, yf-1.0f);

		// Fade
		final float u = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
		final float v = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);

		return (lerp(
				lerp(aa, ba, u),
				lerp(ab, bb, u),
				v) + 1.0f) / 2.0f;
	}

	// Analytical derivative of perlin
	// y = height value, (x,z) = gradient
	public Vector3 dperlin(float x, float y) {
		x = xScale * x + xShift;
		y = yScale * y + yShift;

		final float xb = floor(x);
		final float yb = floor(y);
		final float xf = x - xb;
		final float yf = y - yb;
		final int xi = (int)(xb);
		final int yi = (int)(yb);

		final Vector2 gaa = gradient(xi, yi);
		final Vector2 gab = gradient(xi, yi+1);
		final Vector2 gba = gradient(xi+1, yi);
		final Vector2 gbb = gradient(xi+1, yi+1);

		final float aa = gaa.dot(xf, yf);
		final float ab = gab.dot(xf, yf-1.0f);
		final float ba = gba.dot(xf-1.0f, yf);
		final float bb = gbb.dot(xf-1.0f, yf-1.0f);

		// Fade
		final float u = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
		final float v = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
		final float dudx = 30.0f * xf * xf * (xf * (xf - 2.0f) + 1.0f);
		final float dvdy = 30.0f * yf * yf * (yf * (yf - 2.0f) + 1.0f);

		// Mix along x (u)
		final float ma    = lerp(aa, ba, u);
		final float mb    = lerp(ab, bb, u);
		final float dmadx = lerp(gaa.x, gba.x, u) + dudx * (ba - aa);
		final float dmbdx = lerp(gab.x, gbb.x, u) + dudx * (bb - ab);
		final float dmady = lerp(gaa.y, gba.y, u);
		final float dmbdy = lerp(gab.y, gbb.y, u);

		// Mix along y (v)
		float h    = lerp(ma, mb, v);
		float dhdx = lerp(dmadx, dmbdx, v);
		float dhdy = lerp(dmady, dmbdy, v) + dvdy * (mb - ma);

		return new Vector3(dhdx, (h + 1.0f) / 2.0f, dhdy);
	}

	// Numerical derivative
	public Vector3 perlinTan(float x, float y, float h) {
		final float dx = 0.0001f;
		float dh = perlin(x+dx, y);
		Vector3 v = new Vector3(dx, (dh-h)*terrainHeight, 0);
		v.setLength(1.0f);
		return v;
	}

	public Vector3 perlinBitan(float x, float y, float h) {
		final float dy = 0.0001f;
		float dh = perlin(x, y+dy);
		Vector3 v = new Vector3(0, (dh-h)*terrainHeight, dy);
		v.setLength(1.0f);
		return v;
	}
}
