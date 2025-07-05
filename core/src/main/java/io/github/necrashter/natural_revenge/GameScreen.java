package io.github.necrashter.natural_revenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Queue;

import io.github.necrashter.natural_revenge.ui.GameOverDialog;
import io.github.necrashter.natural_revenge.ui.TouchPad;
import io.github.necrashter.natural_revenge.world.GameWorld;
import io.github.necrashter.natural_revenge.world.GameWorldRenderer;
import io.github.necrashter.natural_revenge.world.LowResWorldRenderer;
import io.github.necrashter.natural_revenge.world.levels.ScriptedEvent;
import io.github.necrashter.natural_revenge.world.player.PlayerWeapon;
import io.github.necrashter.natural_revenge.world.player.Firearm;

public class GameScreen implements Screen {
    public static final float CROSSHAIR_SIZE = 48f;
    final Main game;

    public final GameWorld world;
    private final GameWorldRenderer worldRenderer;

    private final Stage stage;

    private final WidgetGroup hudGroup;
    private final Label topLeftLabel;
    private final Label topRightWeaponsLabel;
    private final Label topRightLabel;
    private final Table topRightTable;
    private final Label bottomLabel;

    private final WidgetGroup subtitleGroup;
    private final Label subtitleLabel;

    private final Dialog pauseDialog;
    private Dialog currentDialog = null;
    private final Queue<Dialog> dialogQueue = new Queue<>();
    private boolean wasPausedBeforeDialog = false;

    private final Image hurtOverlay;

    public TouchPad movementTouch;

    public GameScreen(final Main game, final GameWorld world) {
        this.game = game;

        this.world = world;
        world.screen = this;
        worldRenderer = new LowResWorldRenderer(world);
//        worldRenderer = world;

        stage = new Stage(Main.createViewport());

        {
            hurtOverlay = new Image(Main.assets.hurtOverlay);
            Container<Image> container = new Container<>(hurtOverlay);
            container.setFillParent(true);
            container.fill();
            stage.addActor(container);
            hurtOverlay.setColor(1, 1, 1, 0);
        }

        {
            hudGroup = new WidgetGroup();
            hudGroup.setFillParent(true);

            topLeftLabel = new Label("f", Main.skin);
            Container<Label> labelContainer = new Container<>(topLeftLabel);
            labelContainer.setFillParent(true);
            labelContainer.top().left().pad(20);
            hudGroup.addActor(labelContainer);

            topRightWeaponsLabel = new Label("f", Main.skin);
            topRightWeaponsLabel.setAlignment(Align.right);
            topRightLabel = new Label("f", Main.skin);
            topRightLabel.setAlignment(Align.right);
            topRightTable = new Table();
            topRightTable.setFillParent(true);
            topRightTable.top().right().pad(20);
            topRightTable.add(topRightWeaponsLabel).right().row();
            topRightTable.add(topRightLabel).right().row();
            hudGroup.addActor(topRightTable);

            bottomLabel = new Label("", Main.skin);
            Container<Label> labelContainer1 = new Container<>(bottomLabel);
            labelContainer1.setFillParent(true);
            labelContainer1.center().padTop(200f);
            hudGroup.addActor(labelContainer1);

            Texture crosshairTexture = Main.assets.get("crosshair010.png");
            Image crosshairImage = new Image(crosshairTexture);
            Container<Image> crosshairContainer = new Container<>(crosshairImage);
            crosshairContainer.setFillParent(true);
            crosshairContainer.size(CROSSHAIR_SIZE).center();
            hudGroup.addActor(crosshairContainer);

            stage.addActor(hudGroup);
        }

        {
            subtitleGroup = new WidgetGroup();
            subtitleGroup.setFillParent(true);

            final Image backgroundImage = new Image(Main.assets.bottomGrad);
            Container<Image> backgroundContainer = new Container<>(backgroundImage);
            backgroundContainer.setFillParent(true);
            backgroundContainer.fill(1.0f, 0.33f).bottom();
            subtitleGroup.addActor(backgroundContainer);

            subtitleLabel = new Label("", Main.skin);
            Container<Label> labelContainer1 = new Container<>(subtitleLabel);
            labelContainer1.setFillParent(true);
            labelContainer1.center().bottom().padBottom(80f);
            subtitleGroup.addActor(labelContainer1);

            if (Main.isMobile()) {
                TextButton nextButton = new TextButton("Next", Main.skin);
                nextButton.addListener(new InputListener() {
                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        if (activeSubtitle != null) activeSubtitle.shouldFade = true;
                        return true;
                    }
                });
                Container<TextButton> nextButtonContainer = new Container<>(nextButton);
                nextButtonContainer.setFillParent(true);
                nextButtonContainer.pad(40).align(Align.right | Align.center);
                subtitleGroup.addActor(nextButtonContainer);
            } else {
                Label label1 = new Label("Press SPACE to continue...", Main.skin, "old-font", Color.WHITE);
                Container<Label> labelContainer2 = new Container<>(label1);
                labelContainer2.setFillParent(true);
                labelContainer2.center().bottom().padBottom(10f);
                subtitleGroup.addActor(labelContainer2);
            }

//            stage.addActor(subtitleGroup);

            subtitleGroup.setColor(1, 1, 1, 0);
        }

        {
            pauseDialog = new Dialog("Pause Menu", Main.skin);

            pauseDialog.padTop(new GlyphLayout(Main.skin.getFont("default-font"),"Pause Menu").height*1.2f);
            pauseDialog.padLeft(16); pauseDialog.padRight(16);

            {
                final TextButton button = new TextButton("Resume", Main.skin);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        togglePause();
                    }
                });
                pauseDialog.getButtonTable().add(button).height(button.getHeight()).width(button.getWidth()).row();
            }

            {
                final TextButton button = new TextButton("Restart Level", Main.skin);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        restart();
                    }
                });
                pauseDialog.getButtonTable().add(button).height(button.getHeight()).width(button.getWidth()).row();
            }

            {
                final TextButton button = new TextButton("Weapon Stats", Main.skin);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        showWeaponInventoryDialog();
                    }
                });
                pauseDialog.getButtonTable().add(button).height(button.getHeight()).width(button.getWidth()).row();
            }

            {
                final TextButton button = new TextButton("Options", Main.skin);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        new OptionsDialog(world).show(stage);
                    }
                });
                pauseDialog.getButtonTable().add(button).height(button.getHeight()).width(button.getWidth()).row();
            }

            {
                final TextButton button = new TextButton("Exit Game", Main.skin);
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        mainMenu();
                    }
                });
                pauseDialog.getButtonTable().add(button).height(button.getHeight()).width(button.getWidth()).row();
            }
        }
        Gdx.input.setInputProcessor(new InputMultiplexer(
                stage, world.player.inputAdapter
        ));
        if (Main.isMobile()) {
            movementTouch = new TouchPad(Main.skin);
            float touchpadSize = 240f;
            Container<TouchPad> movementTouchContainer = new Container<>(movementTouch);
            movementTouchContainer.setFillParent(true);
            movementTouchContainer.bottom().left().pad(20).size(touchpadSize);
            hudGroup.addActor(movementTouchContainer);

            TextButton jumpButton = new TextButton("JUMP", Main.skin);
            jumpButton.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (!world.player.inputAdapter.disabled) {
                        world.player.jump();
                    }
                    return true;
                }
            });
            Container<TextButton> shootButtonContainer = new Container<>(jumpButton);
            shootButtonContainer.setFillParent(true);
            shootButtonContainer.pad(40).align(Align.bottomRight);
            hudGroup.addActor(shootButtonContainer);

            TextButton menuButton = new TextButton("Menu", Main.skin);
            menuButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    pause();
                }
            });
            Container<TextButton> menuButtonContainer = new Container<>(menuButton);
            menuButtonContainer.setFillParent(true);
            menuButtonContainer.pad(20).align(Align.center | Align.top);
            stage.addActor(menuButtonContainer);

            TextButton useButton = new TextButton("USE", Main.skin);
            useButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    world.player.useKeyPressed();
                }
            });
            TextButton reloadButton = new TextButton("RELOAD", Main.skin);
            reloadButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    world.player.shouldReload = true;
                }
            });
            Container<TextButton> reloadContainer = new Container<>(reloadButton);
            reloadContainer.setFillParent(true);
            reloadContainer.pad(20).align(Align.left | Align.center).padBottom(100f);
            stage.addActor(reloadContainer);

            TextButton prevWeaponButton = new TextButton("PREV", Main.skin);
            prevWeaponButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    world.player.prevWeapon();
                }
            });
            TextButton nextWeaponButton = new TextButton("NEXT", Main.skin);
            nextWeaponButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    world.player.nextWeapon();
                }
            });
            Table prevNextTable = new Table();
            prevNextTable.add(prevWeaponButton).padRight(10f);
            prevNextTable.add(nextWeaponButton);
            topRightTable.add(prevNextTable).right().row();

            TextButton adsButton = new TextButton("ADS", Main.skin);
            adsButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    world.player.firing2 = !world.player.firing2;
                }
            });
            topRightTable.add(adsButton).right().padTop(32f).row();

            topRightWeaponsLabel.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    int weapon = MathUtils.floor(x / topRightWeaponsLabel.getWidth() * 6f);
                    if (weapon < 0) weapon = 0;
                    if (weapon < world.player.weapons.size)
                        world.player.equipWeapon(weapon);
                }
            });
        }
        setPaused(false);

        world.addedToScreen();
    }

    public ProgressBar progressBar;
    public Label progressBarLabel;
    public Container<ProgressBar> progressBarContainer;
    public Container<Label> progressBarLabelContainer;
    public void addProgress(String name, float max) {
        progressBar = new ProgressBar(0.0f, max, 1.0f, false, Main.skin2);
        progressBarLabel = new Label(name, Main.skin);
        progressBarContainer = new Container<>(progressBar);
        progressBarContainer.setFillParent(true);
        progressBarContainer.center().top().padTop(90f);
        progressBarContainer.width(560f);
        progressBarLabelContainer = new Container<>(progressBarLabel);
        progressBarLabelContainer.setFillParent(true);
        progressBarLabelContainer.center().top().padTop(90f);
        hudGroup.addActor(progressBarContainer);
        hudGroup.addActor(progressBarLabelContainer);
    }
    public void removeProgress() {
        if (progressBarContainer != null) {
            progressBarContainer.remove();
            progressBarContainer = null;
        }
        if (progressBarLabelContainer != null) {
            progressBarLabelContainer.remove();
            progressBarLabelContainer = null;
        }
    }

    public void getPlayerMovement(Vector2 movement) {
        float x = 0.0f;
        float y = 0.0f;
        if (movementTouch != null) {
            x += movementTouch.getKnobPercentY();
            y += movementTouch.getKnobPercentX();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            x += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            x -= 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            y += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            y -= 1.0f;
        }
        movement.set(x, y);
        if (movement.len2() > 1) movement.nor();
    }

    @Override
    public void render(float delta) {
        // Input handling
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (currentDialog != null) {
                currentDialog.hide();
            } else {
                togglePause();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            if (currentDialog != null) {
                currentDialog.hide();
            } else {
                showWeaponInventoryDialog();
            }
        }
        if (!world.player.inputAdapter.disabled) {
            getPlayerMovement(world.player.movementInput);
        } else {
            world.player.movementInput.setZero();
        }
        if (Main.debugMode) {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                delta *= 4f;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
                delta *= 0.1f;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
                gameOver(false);
            }
        }

        // Update
        if (currentDialog != null && currentDialog.getStage() == null) {
            currentDialog = null;
            if (dialogQueue.notEmpty()) {
                showDialog(dialogQueue.removeFirst(), false);
            } else {
                if (!wasPausedBeforeDialog) {
                    setPaused(false);
                }
            }
        }
        world.update(delta);

        bottomLabel.setText(world.player.getHoverInfo());
        stage.act(delta);

//        double s = (double) TimeUtils.millis() / 500.0;
//        double y = 100 * Math.sin(s) + 100;
//        double b = y > 100 ? (y - 100) * 0.01 : 0;
//        ScreenUtils.clear((float)b, (float)b, (float)b, 1);
//        ScreenUtils.clear(1, 0, 0, 1, true);

        worldRenderer.render();

        stage.getViewport().apply();
        stage.draw();

        StringBuilder stringBuilder = new StringBuilder();
        world.buildHudText(stringBuilder);
        topLeftLabel.setText(stringBuilder);
//        stringBuilder.append("FPS: ").append(Gdx.graphics.getFramesPerSecond());
//        stringBuilder.append(" Visible: ").append(world.visibleCount);
//        stringBuilder.append('\n');
//        stringBuilder.append("x ").append(world.player.hitBox.position.x);
//        stringBuilder.append(" y ").append(world.player.hitBox.position.y);
//        stringBuilder.append(" z ").append(world.player.hitBox.position.z);
//        stringBuilder.append('\n');
//        stringBuilder.append("Hit: ").append(world.player.aimIntersection.type);
//        stringBuilder.append(" at t ").append(world.player.aimIntersection.t);
//        stringBuilder.append('\n');

        if (world.player != null) {
            stringBuilder = new StringBuilder();
            world.player.buildWeaponsText(stringBuilder);
            topRightWeaponsLabel.setText(stringBuilder);
            stringBuilder = new StringBuilder();
            world.player.buildWeaponText(stringBuilder);
            topRightLabel.setText(stringBuilder);
        }
    }

    @Override
    public void resize(int width, int height) {
        worldRenderer.screenResize(width, height);
        stage.getViewport().update(width, height, true);
    }

    public void togglePause() {
        setPaused(!world.paused);
        if (world.paused) pauseDialog.show(stage);
        else pauseDialog.hide();
    }

    @Override
    public void pause() {
        if (world.paused) return;
        pauseDialog.show(stage);
        setPaused(true);
    }

    @Override
    public void resume() {
    }

    public void setPaused(boolean v) {
        if (world.paused == v) return;
        world.paused = v;
        if (!Main.isMobile()) Gdx.input.setCursorCatched(!world.paused);
        if (world.paused) {
            Main.music.paused();
        } else {
            Main.music.resumed();
            world.player.resetMouse();
        }
    }

    public void showDialog(Dialog dialog, boolean pause) {
        if (currentDialog != null) {
            dialogQueue.addLast(dialog);
        } else {
            currentDialog = dialog;
            dialog.show(stage);
            wasPausedBeforeDialog = world.paused; // tab key
            if (pause) setPaused(true);
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        // World renderer is supposed to dispose world as well.
        worldRenderer.dispose();
        stage.dispose();
    }

    public void playerDied() {
        world.statistics.deaths += 1;
        final Dialog autoRespawnDialog = new Dialog("You Died!", Main.skin);
        final Label countdownLabel = new Label("Respawning in 5...", Main.skin);
        autoRespawnDialog.getContentTable().add(countdownLabel).pad(20);
        showDialog(autoRespawnDialog, false);
        world.player.inputAdapter.disabled = true;
        world.player.firing1 = false;
        world.player.firing2 = false;

        // schedule respawn
        stage.addAction(Actions.sequence(
            Actions.delay(1f),
            Actions.run(() -> countdownLabel.setText("Respawning in 4...")),
            Actions.delay(1f),
            Actions.run(() -> countdownLabel.setText("Respawning in 3...")),
            Actions.delay(1f),
            Actions.run(() -> countdownLabel.setText("Respawning in 2...")),
            Actions.delay(1f),
            Actions.run(() -> countdownLabel.setText("Respawning in 1...")),
            Actions.delay(1f),
            Actions.run(() -> countdownLabel.setText("Respawning...")),
            Actions.run(autoRespawnDialog::hide),
            Actions.run(() -> {
                world.player.inputAdapter.disabled = false;
                world.player.health = world.player.maxHealth;
                world.player.dead = false;
                Vector2 spawnPoint = world.randomPointNearPlayer(10f, 20f);
                world.player.setPosition(spawnPoint.x, spawnPoint.y);
                world.player.hitBox.position.y += 2f;
            })
        ));
    }


//    public void playerDied() {
//        Dialog dialog = new Dialog("You Died!", game.skin) {
//            @Override
//            protected void result(Object object) {
//                int i = (int) object;
//                switch (i) {
//                    case 0: restart(); break;
//                    case 1: restartEasier(); break;
//                    default: mainMenu(); break;
//                }
//            }
//        };
//        dialog.button("Restart", 0);
//        dialog.getButtonTable().row();
//        dialog.button("Restart (Easier)", 1);
//        dialog.getButtonTable().row();
//        dialog.button("Main Menu", 2);
//        dialog.padTop(new GlyphLayout(game.skin.getFont("default-font"),"Pause Menu").height*1.2f);
//        dialog.padLeft(16); dialog.padRight(16);
//        showDialog(dialog, true);
//    }

    public void playerHurt() {
        hurtOverlay.setColor(1, 1, 1, 1);
        hurtOverlay.clearActions();
        hurtOverlay.addAction(Actions.fadeOut(0.5f));
    }

    public void gameOver(boolean win) {
        GameOverDialog dialog = new GameOverDialog(win, world, this);
        showDialog(dialog, true);
        dialog.create();
    }

    public void mainMenu() {
        game.setScreen(new MenuScreen(game));
        dispose();
    }

    public void restart() {
        game.setScreen(game.getLevel(world.level, world.easiness));
        dispose();
    }

    public void restartEasier() {
        game.setScreen(game.getLevel(world.level, world.easiness + 1.0f));
        dispose();
    }

    public void nextLevel() {
        game.setScreen(game.getLevel(world.level + 1, 1.0f));
        dispose();
    }

    private SubtitleScriptedEvent activeSubtitle = null;
    public class SubtitleScriptedEvent implements ScriptedEvent {
        final String text;
        boolean shouldFade = false;

        public SubtitleScriptedEvent(String text) {
            this.text = text;
        }

        @Override
        public void activate() {
            activeSubtitle = this;
            subtitleLabel.setText(text);
            if (subtitleGroup.getStage() == stage) {
                hudGroup.clearActions();
                subtitleGroup.clearActions();
                return;
            }
            hudGroup.addAction(Actions.fadeOut(0.3f));
            stage.addActor(subtitleGroup);
            subtitleGroup.addAction(Actions.fadeIn(0.3f));
        }

        @Override
        public boolean update(float delta) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || shouldFade) {
                activeSubtitle = null;
                hudGroup.addAction(Actions.fadeIn(0.3f));
                subtitleGroup.addAction(Actions.fadeOut(0.3f));
                subtitleGroup.addAction(Actions.removeActor());
                return true;
            }
            return false;
        }
    }

    public SubtitleScriptedEvent subtitle(String text) {
        return new SubtitleScriptedEvent(text);
    }

    public class WinGameEvent extends ScriptedEvent.OneTimeEvent {
        @Override
        public void activate() {
            gameOver(true);
        }
    }

    public WinGameEvent winGameEvent() {
        return new WinGameEvent();
    }

    private void showWeaponInventoryDialog() {
        final Dialog weaponDialog = new Dialog("Current Weapon", Main.skin);

        // Set dialog size to fit screen (1280x720)
        weaponDialog.setSize(900, 600);
        weaponDialog.setPosition((1280 - 900) / 2f, (720 - 600) / 2f);

        // Create content table
        Table contentTable = new Table(Main.skin);
        contentTable.pad(30);

        // Check if player has an equipped weapon
        if (world.player.activeWeapon == null) {
            Label noWeaponLabel = new Label("No weapon equipped", Main.skin);
            noWeaponLabel.setFontScale(1.5f);
            noWeaponLabel.setColor(Color.GRAY);
            contentTable.add(noWeaponLabel).center().padTop(100);
        } else {
            PlayerWeapon weapon = world.player.activeWeapon;

            // Create main weapon info section
            Table weaponInfoTable = new Table(Main.skin);

            // Weapon name with type indicator
            String weaponName = getWeaponDisplayName(weapon);
            String weaponType = getWeaponType(weapon);
            Label weaponNameLabel = new Label(weaponName, Main.skin);
            weaponNameLabel.setFontScale(1.25f);
            weaponNameLabel.setColor(Color.YELLOW);

            Label weaponTypeLabel = new Label("(" + weaponType + ")", Main.skin, "small");
            weaponTypeLabel.setFontScale(1.0f);
            weaponTypeLabel.setColor(Color.LIGHT_GRAY);

            weaponInfoTable.add(weaponNameLabel).center().row();
            weaponInfoTable.add(weaponTypeLabel).center().padTop(5).row();

            contentTable.add(weaponInfoTable).center().padBottom(8).row();

            // Create stats section
            Table statsTable = createDetailedStatsTable(weapon);
            contentTable.add(statsTable).expand().fill();

            if (weapon instanceof Firearm) {
                contentTable.row();
                Table calculatedTable = createCalculatedStats((Firearm) weapon);
                contentTable.add(calculatedTable).expand().fill();
            }
        }

        // Add content to dialog
        weaponDialog.getContentTable().add(contentTable);

        // Add close button
        TextButton closeButton = new TextButton("Close", Main.skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                weaponDialog.hide();
            }
        });
        weaponDialog.button(closeButton);

        // Use the proper dialog management system instead of directly showing
        showDialog(weaponDialog, true);
    }

    private String getWeaponType(PlayerWeapon weapon) {
        if (weapon instanceof Firearm) {
            Firearm firearm = (Firearm) weapon;
            if (firearm.name.toLowerCase().contains("pistol")) {
                return "Pistol";
            } else if (firearm.name.toLowerCase().contains("rifle")) {
                return "Rifle";
            } else {
                return "Firearm";
            }
        } else {
            return "Unknown";
        }
    }

    private Table createDetailedStatsTable(PlayerWeapon weapon) {
        Table statsTable = new Table(Main.skin);
        statsTable.pad(20);

        if (weapon instanceof Firearm) {
            Firearm firearm = (Firearm) weapon;

            // Primary stats section
            Table primaryStats = new Table(Main.skin);
            primaryStats.setBackground(Main.skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.5f)));

//            Label primaryTitle = new Label("PRIMARY STATS", game.skin);
//            primaryTitle.setFontScale(1.1f);
//            primaryTitle.setColor(Color.ORANGE);
//            primaryStats.add(primaryTitle).center().padBottom(15).row();

            // Damage with color coding
            float damageRatio = Math.min(firearm.damage / 10.0f, 1.0f); // Normalize to 0-1
//            Color damageColor = new Color(1.0f, 1.0f - damageRatio * 0.5f, 0.0f, 1.0f); // Green to Red
            Label damageLabel = new Label("Damage: " + Main.float1Decimal(firearm.damage), Main.skin);
//            damageLabel.setColor(damageColor);
//            damageLabel.setFontScale(1.0f);
            primaryStats.add(damageLabel).left().row();

//            ProgressBar ammoBar = new ProgressBar(0, firearm.maxAmmoInClip, 1, false, game.skin2);
//            ammoBar.setValue(firearm.ammoInClip);
//            ammoBar.setSize(200, 15);
//            primaryStats.add(ammoBar).width(200).height(15).padBottom(8).row();

            // Spread
            Label spreadLabel = new Label("Spread: " + Main.float2Decimals(firearm.spread), Main.skin);
            spreadLabel.setFontScale(1.0f);
            primaryStats.add(spreadLabel).left().row();

            // Bullets per shot
            if (firearm.bulletsPerShot > 1) {
                Label burstLabel = new Label("Bullet/Shot: " + firearm.bulletsPerShot, Main.skin);
//                burstLabel.setColor(Color.ORANGE);
                burstLabel.setFontScale(1.0f);
                primaryStats.add(burstLabel).left().row();
            }

            if (firearm.burstCount > 1) {
                Label burstLabel = new Label("Burst: " + firearm.burstCount + " bullets", Main.skin);
//                burstLabel.setColor(Color.ORANGE);
                burstLabel.setFontScale(1.0f);
                primaryStats.add(burstLabel).left().row();
            }

            // Knockback
            if (Math.abs(firearm.knockback) > 0.1f) {
                Label knockLabel = new Label("Knockback: " + Main.float2Decimals(Math.abs(firearm.knockback)), Main.skin);
                knockLabel.setFontScale(1.0f);
                primaryStats.add(knockLabel).left().row();
            }

            statsTable.add(primaryStats).width(300).padRight(20);

            // Secondary stats section
            Table secondaryStats = new Table(Main.skin);
            secondaryStats.pad(10);
            secondaryStats.setBackground(Main.skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.5f)));

//            Label secondaryTitle = new Label("WEAPON PROPERTIES", game.skin);
//            secondaryTitle.setFontScale(1.1f);
//            secondaryTitle.setColor(Color.CYAN);
//            secondaryStats.add(secondaryTitle).center().padBottom(15).row();

            // Fire rate with color coding
            float fireRateRatio = Math.min(firearm.recoverySpeed / 10.0f, 1.0f);
//            Color fireRateColor = new Color(0.0f, 0.5f + fireRateRatio * 0.5f, 1.0f, 1.0f); // Blue to Cyan
            Label fireRateLabel = new Label("Fire Rate: " + Main.float1Decimal(firearm.recoverySpeed), Main.skin);
//            fireRateLabel.setColor(fireRateColor);
            fireRateLabel.setFontScale(1.0f);
            secondaryStats.add(fireRateLabel).left().row();

            // Ammo section with progress bar
            Label ammoLabel = new Label("Clip Size: " + firearm.maxAmmoInClip, Main.skin);
            secondaryStats.add(ammoLabel).left().row();

            // Reload speed
            Label reloadLabel = new Label("Reload Speed: " + Main.float1Decimal(firearm.reloadSpeed), Main.skin);
            secondaryStats.add(reloadLabel).left().row();

            statsTable.add(secondaryStats).width(300).padLeft(20);
        }

        return statsTable;
    }
    private Table createCalculatedStats(Firearm firearm) {
        Table calculatedStats = new Table(Main.skin);
        calculatedStats.pad(10);
        calculatedStats.setBackground(Main.skin.newDrawable("white", new Color(0.1f, 0.1f, 0.1f, 0.5f)));

        float dps = firearm.computeDPS();
        float[] distances = new float[] {1f, 5f, 10f, 15f, 20f, 25f};

        // Top-left empty cell
        Label distanceTitle = new Label("Distance", Main.skin);
        calculatedStats.add(distanceTitle).padRight(16);

        // Add column headers: distances
        for (float distance : distances) {
            Label distanceLabel = new Label(String.valueOf((int)distance) + 'm', Main.skin);
            calculatedStats.add(distanceLabel).center().padRight(16);
        }
        calculatedStats.row().padBottom(16);

        // Accuracy row
        Label accuracyTitle = new Label("Accuracy", Main.skin);
        calculatedStats.add(accuracyTitle).padRight(16);
        for (float distance : distances) {
            float accuracy = firearm.computeAccuracy(distance, 0.375f);
            Color accuracyColor = Color.RED.cpy().lerp(Color.GREEN, MathUtils.clamp((accuracy - .5f) * 2f, 0f, 1f));
            Label accuracyLabel = new Label(String.valueOf(MathUtils.round(accuracy * 100f)) + '%', Main.skin);
            accuracyLabel.setColor(accuracyColor);
            accuracyLabel.setFontScale(1.0f);
            calculatedStats.add(accuracyLabel).center().padRight(16);
        }
        calculatedStats.row();

        // DPS row
        Label dpsTitle = new Label("DPS", Main.skin);
        calculatedStats.add(dpsTitle).padRight(16);
        for (float distance : distances) {
            float accuracy = firearm.computeAccuracy(distance, 0.375f);
            float effectiveDps = accuracy * dps;
            Color dpsColor = Color.RED.cpy().lerp(Color.GREEN, MathUtils.clamp((effectiveDps - 5f) / 50f, 0f, 1f));
            Label dpsLabel = new Label(Main.float2Decimals(effectiveDps), Main.skin);
            dpsLabel.setColor(dpsColor);
            dpsLabel.setFontScale(1.0f);
            calculatedStats.add(dpsLabel).center().padRight(16);
        }

        return calculatedStats;
    }


    private String getWeaponDisplayName(PlayerWeapon weapon) {
        if (weapon instanceof Firearm) {
            return ((Firearm) weapon).name;
        } else {
            return "Unknown Weapon";
        }
    }
}
