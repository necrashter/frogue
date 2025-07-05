package io.github.necrashter.natural_revenge.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import io.github.necrashter.natural_revenge.GameScreen;
import io.github.necrashter.natural_revenge.Main;
import io.github.necrashter.natural_revenge.MenuScreen;
import io.github.necrashter.natural_revenge.world.levels.Level1Swamp;
import io.github.necrashter.natural_revenge.world.player.EnumeratingRoller;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        Main.PostInit postInit = new Main.PostInit() {
            @Override
            public void run(Main main) {
                if (args.length == 0) {
                    main.setScreen(new MenuScreen(main));
                } else if (args[0].equals("debug")) {
                    Main.debugMode = true;
                    main.setScreen(new GameScreen(main, new Level1Swamp(main, 1, 1)));
                } else if (args[0].equals("all-weapons")) {
                    String filename = args.length > 1 ? args[1] : "all-weapons.csv";
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                        EnumeratingRoller.appendAllWeaponsCSV(writer);
                    } catch (IOException e) {
                        System.err.println("Error writing to file: " + e.getMessage());
                    }
                    System.exit(0);
                } else {
                    System.err.println("Unknown CLI arguments.");
                    System.exit(1);
                }
            }
        };
        new Lwjgl3Application(new Main(postInit), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("NaturalRevenge");
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(true);
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        configuration.setWindowedMode(1280, 720);
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
