package io.github.necrashter.natural_revenge.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class TouchPad extends Touchpad {
    public TouchPad(Skin skin) {
        super(0f, skin);
    }

    public void draw (Batch batch, float parentAlpha) {
        validate();

        batch.setColor(1f, 1f, 1f, 0.4f * parentAlpha);

        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();

        TouchpadStyle style = getStyle();

        final Drawable bg = style.background;
        if (bg != null) bg.draw(batch, x, y, w, h);

        batch.setColor(1f, 1f, 1f, parentAlpha);

        final Drawable knob = style.knob;
        if (knob != null) {
            x += getKnobX() - knob.getMinWidth() / 2f;
            y += getKnobY() - knob.getMinHeight() / 2f;
            knob.draw(batch, x, y, knob.getMinWidth(), knob.getMinHeight());
        }
    }
}
