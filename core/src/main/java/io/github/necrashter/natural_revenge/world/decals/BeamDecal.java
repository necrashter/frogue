package io.github.necrashter.natural_revenge.world.decals;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;
import com.badlogic.gdx.math.Vector3;

public class BeamDecal extends DecayingDecal {
    private static final Vector3 tmp = new Vector3();
    private static final Vector3 tmp2 = new Vector3();
    private static final Vector3 tmp3 = new Vector3();

    public float size = 0.02f;
    public Vector3 position2 = new Vector3();
    public Vector3 cameraPosition = new Vector3();

    @Override
    protected void transformVertices() {
        updated = true;

        tmp2.set(position2).sub(position);

        tmp.set(position).sub(cameraPosition);
        tmp.crs(tmp2).nor().scl(size);
//        tmp.set(Vector3.Y);
        vertices[X1] = position.x + tmp.x;
        vertices[Y1] = position.y + tmp.y;
        vertices[Z1] = position.z + tmp.z;
        vertices[X3] = position.x - tmp.x;
        vertices[Y3] = position.y - tmp.y;
        vertices[Z3] = position.z - tmp.z;

        tmp.set(position2).sub(cameraPosition);
        tmp.crs(tmp2).nor().scl(size);
        vertices[X2] = position2.x + tmp.x;
        vertices[Y2] = position2.y + tmp.y;
        vertices[Z2] = position2.z + tmp.z;
        vertices[X4] = position2.x - tmp.x;
        vertices[Y4] = position2.y - tmp.y;
        vertices[Z4] = position2.z - tmp.z;
    }

    @Override
    protected void resetVertices () {
    }

    public static BeamDecal newBeamDecal(float width, float height, TextureRegion textureRegion, int srcBlendFactor, int dstBlendFactor) {
        BeamDecal decal = new BeamDecal();
        decal.setTextureRegion(textureRegion);
        decal.setBlending(srcBlendFactor, dstBlendFactor);
        decal.dimensions.x = width;
        decal.dimensions.y = height;
        decal.setColor(1, 1, 1, 1);
        return decal;
    }

    public static BeamDecal newBeamDecal(TextureRegion textureRegion, boolean hasTransparency) {
        return newBeamDecal(textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), textureRegion,
                hasTransparency ? GL20.GL_SRC_ALPHA : DecalMaterial.NO_BLEND,
                hasTransparency ? GL20.GL_ONE_MINUS_SRC_ALPHA : DecalMaterial.NO_BLEND);
    }

    public static BeamDecal newBeamDecal(float width, float height, TextureRegion textureRegion) {
        return newBeamDecal(width, height, textureRegion, DecalMaterial.NO_BLEND, DecalMaterial.NO_BLEND);
    }

    public void setPosition2(Vector3 position) {
        this.position2.set(position);
        this.updated = false;
    }

    public void setCamera(Vector3 c) {
        cameraPosition = c;
        this.updated = false;
    }
}
