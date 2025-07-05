package io.github.necrashter.natural_revenge;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.necrashter.natural_revenge.world.levels.Level1Swamp;
import io.github.necrashter.natural_revenge.world.levels.Level2Flying;
import io.github.necrashter.natural_revenge.world.levels.LevelBossRush;
import io.github.necrashter.natural_revenge.world.player.EnumeratingRoller;
import io.github.necrashter.natural_revenge.world.player.RandomRoller;

public class Main extends Game {
    public static boolean debugMode = false;
    public static AssetManager2 assets;
    public static RandomRoller randomRoller;
    public static MusicManager music;
    public static Preferences preferences;
    public static float sfxVolume = 1.0f;
    public static boolean invertMouseY = false;
    public static float mouseSensitivity = 1.0f; // defaults
    public static float fov = 90f; // default FOV
    private final PostInit postInit;
    public static Skin skin;
    public static Skin skin2;

    public abstract static class PostInit {
        public abstract void run(Main main);
    }

    TextureAtlas skinAtlas;

    public Main(PostInit postInit) {
        super();
        this.postInit = postInit;
    }

    private static float uiScale = 1.0f;
    public static ScreenViewport viewport;
    public static ScreenViewport createViewport() {
        viewport = new ScreenViewport();
        viewport.setUnitsPerPixel(1.0f/uiScale);
        return viewport;
    }

    public static float getUiScale() {
        return uiScale;
    }

    public static void setUiScale(float uiScale) {
        if (viewport != null) {
            viewport.setUnitsPerPixel(1.0f/uiScale);
        }
        Main.uiScale = uiScale;
    }

    public static void loadPreferences() {
        preferences = Gdx.app.getPreferences("Preferences");
        Main.sfxVolume = preferences.getFloat("sfxVolume", Main.sfxVolume);
        Main.music.setVolume(preferences.getFloat("musicVolume", Main.music.getVolume()));
        Main.invertMouseY = preferences.getBoolean("invertMouseY", Main.invertMouseY);
        Main.mouseSensitivity = preferences.getFloat("mouseSensitivity", Main.mouseSensitivity);
        Main.fov = preferences.getFloat("fov", Main.fov);
    }

    @Override
    public void create () {
        skinAtlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
        skin = new Skin();
        skin.add("default-font", new BitmapFont(Gdx.files.internal("ui/mono.fnt")), BitmapFont.class);
        skin.addRegions(skinAtlas);
        skin.load(Gdx.files.internal("uiskin.json"));
        skin2 = new Skin(Gdx.files.internal("biological-attack/biological-attack-ui.json"));

        assets = new AssetManager2();
        music = new MusicManager();
        randomRoller = new RandomRoller();
        loadPreferences();
        if (Main.isMobile()) {
            uiScale = MathUtils.clamp(((Gdx.graphics.getDensity() / .5783681f) - 1.0f) / 2f + 1f, .5f, 2f);
        }
        // loading
        while (!assets.update());
        assets.done();

        if (postInit != null) {
            postInit.run(this);
        } else {
            this.setScreen(new MenuScreen(this));
        }
    }

    @Override
    public void render () {
        float delta = Gdx.graphics.getDeltaTime();
        music.update(delta);
        if (screen != null) screen.render(delta);
    }

    @Override
    public void dispose () {
        skin.dispose();
        skinAtlas.dispose();
        assets.dispose();
    }

    public static boolean isMobile() {
        return Gdx.app.getType().equals(Application.ApplicationType.Android) || Gdx.app.getType().equals(Application.ApplicationType.iOS);
    }

    public Screen getLevel(int level, float easiness) {
        switch (level) {
            case 1: return new GameScreen(this, new Level1Swamp(this, 1, easiness));
            case 2: return new GameScreen(this, new Level2Flying(this, 2, easiness));
            case 3: return new GameScreen(this, new LevelBossRush(this, 3, easiness));
            default: return new MenuScreen(this);
        }
    }

    public static String float2Decimals(float f) {
        int decimal = MathUtils.round((f - (int) f) * 100f);
        if (decimal < 10) {
            return (int) f + ".0" + decimal;
        } else {
            return String.valueOf((int)f) + '.' + decimal;
        }
    }
    public static String float1Decimal(float f) {
        int decimal = MathUtils.round((f - (int) f) * 10f);
        return String.valueOf((int)f) + '.' + decimal;
    }
}
