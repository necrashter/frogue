package io.github.necrashter.natural_revenge.ui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import io.github.necrashter.natural_revenge.GameScreen;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.player.Statistics;

public class GameOverDialog extends Dialog {
    private final GameWorld world;
    private final GameScreen screen;
    Array<CheckBox> checkBoxes = new Array<>();
    LinePlotRenderer renderer;
    public GameOverDialog(boolean win, GameWorld world, GameScreen screen) {
        super(win ? "Level Complete!" : "Game Over", Main.skin);
        this.world = world;
        this.screen = screen;
        button("Main Menu", 1);
        button("Next Level", 0);
        //padTop(new GlyphLayout(Main.skin.getFont("default-font"),"Pause Menu").height*1.2f);
        padLeft(16); padRight(16);
        padBottom(8);
        setFillParent(true);
    }

    public void create() {
        int minutes = MathUtils.floor(world.time / 60f);
        int seconds = MathUtils.floor(world.time - minutes*60f);
        String time = (minutes > 9 ? minutes : "0" + minutes) + ":" + (seconds > 9 ? seconds : "0" + seconds);
        int w = MathUtils.floor(getContentTable().getWidth() - 8);
        int h = MathUtils.floor(getContentTable().getHeight() - 64);

        if (w <= 0 || h <= 0) {
            // Failed to determine size
            w = 1280;
            h = 720;
        }

        TabPane tabPane = new TabPane(getContentTable());
        {
            Table contentTable = new Table(Main.skin);
            Label label = new Label(
                "Player Deaths: " + world.statistics.deaths
                    + "\nTime: " + time
                    + "\n"
                    + "\nMost Accurate (" + Main.float1Decimal(world.statistics.bestAccuracy * 100f) + "%): "
                    + world.statistics.bestAccuracyName
                    + "\nMost Damage (" + Main.float1Decimal(world.statistics.mostDamage) + "): "
                    + world.statistics.mostDamageName
                , Main.skin, "small");
            contentTable.add(label);
            tabPane.addPane("Overview", contentTable);
        }
        {
            Table contentTable = new Table(Main.skin);
            Table leftTable = new Table(Main.skin);

            int leftWidth = 256;
            contentTable.add(leftTable).width(leftWidth);
            Label label = new Label(
                    "Time: " + time + "\n\nLEGEND"
                , Main.skin, "small");
            label.setAlignment(Align.top | Align.left);
            leftTable.add(label).width(leftWidth).row();

            ChangeListener listener = new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    renderTimeline();
                }
            };
            for (int i = 0; i < world.statistics.recorders.size; ++i) {
                Statistics.FloatRecorder recorder = world.statistics.recorders.get(i);
                CheckBox checkBox = new CheckBox(recorder.name, Main.skin, "small");
                CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle(checkBox.getStyle());
                style.fontColor = recorder.color;
                checkBox.setStyle(style);
                leftTable.add(checkBox).left().row();
                checkBox.setChecked(true);
                checkBox.addListener(listener);
                checkBoxes.add(checkBox);
            }

            renderer = new LinePlotRenderer(w - leftWidth, h);
            renderTimeline();
            Image image = new Image(renderer.getTextureRegion());
            contentTable.add(image);

            tabPane.addPane("Timeline", contentTable);
        }
        tabPane.changePane(0);
    }

    private void renderTimeline() {
        renderer.begin();
        renderer.addGrid(10, 4);
        for (int i = 0; i < world.statistics.recorders.size; ++i) {
            if (checkBoxes.get(i).isChecked()) {
                Statistics.FloatRecorder recorder = world.statistics.recorders.get(i);
                renderer.plot(recorder.array, recorder.color);
            }
        }
        renderer.addAxes();
        renderer.end();
    }

    @Override
    protected void result(Object object) {
        int i = (int) object;
        switch (i) {
            case 0: screen.nextLevel(); break;
            case 1: screen.mainMenu(); break;
        }
    }
}
