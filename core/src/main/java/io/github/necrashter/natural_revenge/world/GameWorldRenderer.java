package io.github.necrashter.natural_revenge.world;

import com.badlogic.gdx.utils.Disposable;

public interface GameWorldRenderer extends Disposable {
    void render();
    void screenResize(int width, int height);
}
