package io.github.necrashter.natural_revenge.objectives;

public interface LevelObjective {
    void init();
    void update(float delta);
    void buildHudText(StringBuilder stringBuilder);

    class TextObjective implements LevelObjective {
        final String text;

        public TextObjective(String text) {
            this.text = text;
        }

        @Override
        public void init() {}

        @Override
        public void update(float delta) {}

        @Override
        public void buildHudText(StringBuilder stringBuilder) {
            stringBuilder.append(text);
        }
    }
}
