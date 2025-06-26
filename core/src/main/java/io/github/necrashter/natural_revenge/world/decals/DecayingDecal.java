package io.github.necrashter.natural_revenge.world.decals;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalMaterial;

public class DecayingDecal extends Decal {
    float alpha = 0.0f;
    public boolean decayed = true;

    public void reset() {
        decayed = false;
        alpha = 1.0f;
    }

    public void fade(float alphaDecrease) {
        alpha -= alphaDecrease;
        if (alpha <= 0.0f) {
            alpha = 0.0f;
            decayed = true;
        }
        setColor(color.r, color.g, color.b, alpha);
    }

    public static DecayingDecal newDecayingDecal(float width, float height, TextureRegion textureRegion, int srcBlendFactor, int dstBlendFactor) {
        DecayingDecal decal = new DecayingDecal();
        decal.setTextureRegion(textureRegion);
        decal.setBlending(srcBlendFactor, dstBlendFactor);
        decal.dimensions.x = width;
        decal.dimensions.y = height;
        decal.setColor(1, 1, 1, 1);
        return decal;
    }

    public static DecayingDecal newDecayingDecal(TextureRegion textureRegion, boolean hasTransparency) {
        return newDecayingDecal(textureRegion.getRegionWidth(), textureRegion.getRegionHeight(), textureRegion,
                hasTransparency ? GL20.GL_SRC_ALPHA : DecalMaterial.NO_BLEND,
                hasTransparency ? GL20.GL_ONE_MINUS_SRC_ALPHA : DecalMaterial.NO_BLEND);
    }
}
