package io.github.necrashter.natural_revenge.world;

import com.badlogic.gdx.math.Vector3;

public interface Spatial {
    void getPosition(Vector3 p);
    void getMinPoint(Vector3 p);
    void getMaxPoint(Vector3 p);

    class FakeSpatial implements Spatial {
        public final Vector3 position;

        public FakeSpatial(Vector3 position) {
            this.position = position;
        }

        @Override
        public void getPosition(Vector3 p) {
            p.set(position);
        }

        @Override
        public void getMinPoint(Vector3 p) {
            p.set(position);
        }

        @Override
        public void getMaxPoint(Vector3 p) {
            p.set(position);
        }
    }
}
