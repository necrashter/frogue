package io.github.necrashter.natural_revenge.world.levels;

public interface ScriptedEvent {
    void activate();

    /**
     * Update scripted event for this frame.
     * @param delta delta time
     * @return true if the scripted event is done.
     */
    boolean update(float delta);

    abstract class OneTimeEvent implements ScriptedEvent {
        @Override
        public boolean update(float delta) {
            return true;
        }
    }
}
