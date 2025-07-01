package io.github.necrashter.natural_revenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.text.MessageFormat;
import java.text.NumberFormat;

public class MenuScreen implements Screen {
    final Main game;
    private final Stage stage;
    private Label mouseSensitivityLabel;

    public MenuScreen(final Main game) {
        this.game = game;

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        final TextButton start=new TextButton("Start Game",game.skin);
        start.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startLevel(1);
            }
        });

        final TextButton levelSelect=new TextButton("Level Select",game.skin);
        levelSelect.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                levelSelectDialog();
            }
        });

        final TextButton optionsButton = new TextButton("Options", game.skin);
        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new OptionsDialog(game, null).show(stage);
            }
        });


        TextButton exit=new TextButton("Exit",game.skin);
        exit.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        Table table=new Table();
        table.setFillParent(true);


        // Center the whole table content for a cleaner look
        table.center();

        table.row().padTop(10);
        table.add(start);
        table.row().padTop(10);
        table.add(levelSelect);
        table.row().padTop(10);
        table.add(optionsButton);
        table.row().padTop(10);
        table.add(exit);

        stage.addActor(table);
    }

    public void startLevel(int level) {
        game.setScreen(game.getLevel(level, 1.0f));
        dispose();
    }

    public void levelSelectDialog() {
        Dialog dialog = new Dialog("Select Level", game.skin) {
            @Override
            protected void result(Object object) {
                int i = (int) object;
                if (i > 0) startLevel(i);
            }
        };
        dialog.button("Go Back", 0);
        dialog.getButtonTable().row();
        dialog.button("Level 1: Swamp", 1);
        dialog.getButtonTable().row();
        dialog.button("Level 2: Flying", 2);
        dialog.getButtonTable().row();
//        dialog.button("Level 3: Zombie", 3);
//        dialog.getButtonTable().row();
        dialog.button("Boss Rush", 3);
//        dialog.getButtonTable().row();
        dialog.padTop(new GlyphLayout(game.skin.getFont("default-font"),"Pause Menu").height*1.2f);
        dialog.padLeft(16); dialog.padRight(16);
        dialog.show(stage);
    }

    @Override
    public void show() {
        Main.music.fadeOut();
    }

    @Override
    public void render(float delta) {
        stage.act(delta);

        double s = (double) TimeUtils.millis() / 100.0;
        double y = 100 * Math.sin(s) + 100;
        double x = 100 * Math.cos(s) + 100;
        double b = y > 100 ? (y - 100) * 0.01 : 0;
//        ScreenUtils.clear((float)b, (float)b, (float)b, 1);
        ScreenUtils.clear(0, 0, 0, 1);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
