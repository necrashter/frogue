package io.github.necrashter.natural_revenge;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

import io.github.necrashter.natural_revenge.world.GameWorld;

public class OptionsDialog extends Dialog {
    public OptionsDialog(Main game, GameWorld world) {
        super("Options", game.skin);

        // Layout inside dialog
        Table content = getContentTable();
        content.pad(20);

        // --- Volume Slider ---
        final Label volumeLabel = new Label("Music Volume:", game.skin);
        final Label volumeValue = new Label(String.valueOf((int)(Main.music.getVolume() * 100)), game.skin);

        final Slider volumeSlider = new Slider(0f, 100f, 1f, false, game.skin); // volume range from 0 to 100
        volumeSlider.setValue(Main.music.getVolume() * 100);
        volumeSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float volume = volumeSlider.getValue() / 100f;
                Main.music.setVolume(volume);
                volumeValue.setText(String.valueOf((int)(volume * 100)));
            }
        });

        // Layout Volume
        Table volumeRow = new Table();
        volumeRow.add(volumeLabel).padRight(10);
        volumeRow.add(volumeSlider).width(200).padRight(10);
        volumeRow.add(volumeValue).width(50);
        content.add(volumeRow).left();
        content.row().padTop(20);

        // Invert Mouse
        final CheckBox invertMouseCheckbox = new CheckBox(" Invert Mouse Y", game.skin);
        invertMouseCheckbox.setChecked(Main.invertMouseY);
        invertMouseCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Main.invertMouseY = invertMouseCheckbox.isChecked();
            }
        });

        // Sensitivity Slider
        // --- Sensitivity Slider ---
        final Label sensitivityLabel = new Label("Sensitivity:", game.skin);
        // Display sensitivity with one or two decimal places for better granularity
        final Label mouseSensitivityLabel = new Label(Main.float2Decimals(Main.mouseSensitivity), game.skin);

        // Adjust min, max, and step for sensitivity.
        // Example: 0.1f (very slow) to 2.0f (very fast), with steps of 0.05f
        final Slider sensitivitySlider = new Slider(0.1f, 2.0f, 0.05f, false, game.skin);
        sensitivitySlider.setValue(Main.mouseSensitivity);
        sensitivitySlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Main.mouseSensitivity = sensitivitySlider.getValue();
                mouseSensitivityLabel.setText(Main.float2Decimals(Main.mouseSensitivity));
            }
        });

        // --- End Sensitivity Slider ---

        // --- FOV Slider ---
        final Label fovLabel = new Label("Field of View:", game.skin);
        final Label fovValue = new Label(String.valueOf((int)Main.fov), game.skin);

        final Slider fovSlider = new Slider(30f, 120f, 1f, false, game.skin); // typical FOV range
        fovSlider.setValue(Main.fov);
        fovSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Main.fov = fovSlider.getValue();
                fovValue.setText(String.valueOf((int)Main.fov));
                if (world != null) {
                    world.cam.fieldOfView = Main.fov;
                    world.cam.update();
                }
            }
        });

        // Layout FOV
        Table fovRow = new Table();
        fovRow.add(fovLabel).padRight(10);
        fovRow.add(fovSlider).width(200).padRight(10);
        fovRow.add(fovValue).width(50);


        content.add(invertMouseCheckbox).left();
        content.row().padTop(20);

        Table sensitivityRow = new Table();
        sensitivityRow.add(sensitivityLabel).padRight(10);
        sensitivityRow.add(sensitivitySlider).width(200).padRight(10);
        sensitivityRow.add(mouseSensitivityLabel).width(50);
        content.add(sensitivityRow).left();
        content.row().padTop(20);

        content.add(fovRow).left();

        button("Close"); // Adds a close button
    }
}
