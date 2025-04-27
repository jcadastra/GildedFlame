/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game. It is the "static main"
 * of LibGDX. In this lab we once again return to using Game (instead of
 * ApplicationAdapter), as scene management is so much easier. Once again, take
 * note of the use of ScreenListener to allow scene switching.
 *
 * Based on the original PhysicsDemo Lab by Don Holden, 2007
 *
 * Author:  Walker M. White
 * Version: 2/8/2025
 */
package edu.cornell.cis3152.physics;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.assets.*;
import edu.cornell.gdiac.graphics.*;
import java.util.ArrayList;
//import edu.cornell.cis3152.physics.ragdoll.*;

/**
 * Root class for a LibGDX.
 *
 * This class is technically not the ROOT CLASS. Each platform has another class
 * above this (e.g. PC games use DesktopLauncher) which serves as the true root.
 * However, those classes are unique to each platform, while this class is the
 * same across all plaforms. In addition, this functions as the root class all
 * intents and purposes, and you would draw it as a root class in an
 * architecture specification.
 */
public class GDXRoot extends Game implements ScreenListener {
    /**
     * AssetManager to load game assets (textures, sounds, etc.)
     */
    AssetDirectory directory;
    /**
     * The spritebatch to draw the screen (VIEW CLASS)
     */
    private SpriteBatch batch;
    /**
     * Scene for the asset loading screen (CONTROLLER CLASS)
     */
    private LoadingScene loading;
    /**
     * Player mode for the the game proper (CONTROLLER CLASS)
     */
    private int current;
    /**
     * List of all WorldControllers
     */
    private GameplayScene[] controllers;

    private String[] levels;

    private GameplayScene currentScene;

    private SoundEngine soundEngine;

    /**
     * Creates a new game from the configuration settings.
     * <p>
     * This method configures the asset manager, but does not load any assets
     * or assign any screen.
     */
    public GDXRoot() {
    }

    /**
     * Called when the Application is first created.
     * <p>
     * This is method immediately loads assets for the loading screen, and
     * prepares the asynchronous loader for all other assets.
     */
    public void create() {
        batch = new SpriteBatch();
        soundEngine = new SoundEngine();

        // Create the loading scene
        loading = new LoadingScene("assets.json", batch, 1);
        loading.setScreenListener(this);
        setScreen(loading);
    }

    /**
     * Called when the Application is destroyed.
     * <p>
     * This is preceded by a call to pause().
     */
    public void dispose() {
        // Call dispose on our children
        setScreen(null);
        if (loading != null) {
            loading.dispose();
            loading = null;
        }
        if (controllers != null) {
            for (int ii = 0; ii < controllers.length; ii++) {
                controllers[ii].dispose();
            }
            controllers = null;
        }

        batch.dispose();
        soundEngine.dispose();
        batch = null;

        // Unload all of the resources
        if (directory != null) {
            directory.unloadAssets();
            directory.dispose();
            directory = null;
        }
        super.dispose();
    }

    /**
     * Called when the Application is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never
     * happen before a call to create().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        if (loading != null) {
            loading.resize(width, height);
        }
        if (controllers != null) {
            for (int ii = 0; ii < controllers.length; ii++) {
                controllers[ii].resize(width, height);
            }
        }
    }

    /**
     * Responds to a request from a child scene.
     * <p>
     * Typically this is used to have a scene exit its player mode. The value
     * exitCode can be also used to implement menu options.
     *
     * @param screen   The screen requesting to exit
     * @param exitCode The state of the screen upon exit
     */
    public void exitScreen(Screen screen, int exitCode) {


        // Handle exit from the loading screen.
        if (screen == loading) {
            directory = loading.getAssets();
            loading.dispose();
            loading = null;

            MainMenuScreen mainMenu = new MainMenuScreen();
            mainMenu.setScreenListener(this);
            setScreen(mainMenu);
            soundEngine.stopMusicLoop();
            soundEngine.registerMusic("menu_music", directory.getEntry("menu_music", Music.class));
            ArrayList<String> temp = new ArrayList<>();
            temp.add("menu_music");
            soundEngine.startMusicLoop(temp);
            return;
        }
        // Handle exit from the main menu.
        else if (screen instanceof MainMenuScreen) {
            LevelSelectScene levelSelect = new LevelSelectScene();
            levelSelect.setScreenListener(this);
            setScreen(levelSelect);
            return;
        }
        // Handle exit from the level selection screen.
        else if (screen instanceof LevelSelectScene) {
            int selectedLevel = ((LevelSelectScene) screen).getSelectedLevel();
            soundEngine.stopMusicLoop();
            // Register sound effects.
            soundEngine.registerSoundEffect("jump", directory.getEntry("platform-jump", SoundEffect.class));
            soundEngine.registerSoundEffect("pew", directory.getEntry("platform-pew", SoundEffect.class));
            soundEngine.registerSoundEffect("plop", directory.getEntry("platform-plop", SoundEffect.class));
            soundEngine.registerSoundEffect("dirtFootStep", directory.getEntry("dirtFootStep", SoundEffect.class));
            soundEngine.registerSoundEffect("torchThrow", directory.getEntry("torchThrow", SoundEffect.class));

            // Register music and start a loop.
            //soundEngine.registerMusic("eerie1", directory.getEntry("eerie", Music.class));
            //soundEngine.registerMusic("eerieCriminal", directory.getEntry("eerieCriminal", Music.class));
            //soundEngine.registerMusic("tenseSoundscape", directory.getEntry("tenseSoundscape", Music.class));
            soundEngine.registerMusic("in_game", directory.getEntry("in_game_music", Music.class));
            ArrayList<String> temp = new ArrayList<>();
            //temp.add("eerieCriminal");
            //temp.add("eerie1");
            temp.add("in_game");
            soundEngine.startMusicLoop(temp);

            // Initialize the gameplay scene and level data.
            currentScene = new GameplayScene(directory, soundEngine, "platform");
            levels = new String[]{"example_level", "example_level2", "test3", "example_level", "example_level"};
            currentScene.loadLevel(levels[selectedLevel - 1], "rope_test");
            currentScene.setScreenListener(this);
            currentScene.setSpriteBatch(batch);
            current = selectedLevel - 1;
            setScreen(currentScene);

            return;
        }

        // Handle exit codes from any GameplayScene.
        if (screen instanceof GameplayScene) {
            if (exitCode == GameplayScene.EXIT_NEXT) {
                currentScene.clearLevel();
                current = (current + 1) % levels.length;
                currentScene.loadLevel(levels[current], "rope_test");
                currentScene.reset();
                setScreen(currentScene);
            } else if (exitCode == GameplayScene.EXIT_PREV) {
                currentScene.clearLevel();
                current = (current - 1 + levels.length) % levels.length;
                currentScene.loadLevel(levels[current], "rope_test");
                currentScene.reset();
                setScreen(currentScene);
            } else if (exitCode == GameplayScene.EXIT_QUIT) {
                LevelSelectScene levelSelect = new LevelSelectScene();
                levelSelect.setScreenListener(this);
                setScreen(levelSelect);
                soundEngine.stopMusicLoop();
                soundEngine.registerMusic("menu_music", directory.getEntry("menu_music", Music.class));
                ArrayList<String> temp = new ArrayList<>();
                temp.add("menu_music");
                soundEngine.startMusicLoop(temp);
                return;
            }
            // Handle exit from the main menu.
            else if (screen instanceof MainMenuScreen) {
                LevelSelectScene levelSelect = new LevelSelectScene();
                levelSelect.setScreenListener(this);
                setScreen(levelSelect);
                return;
            }
            // Handle exit from the level selection screen.
            else if (screen instanceof LevelSelectScene) {
                int selectedLevel = ((LevelSelectScene) screen).getSelectedLevel();
                soundEngine.stopMusicLoop();
                // Register sound effects.
                soundEngine.registerSoundEffect("jump", directory.getEntry("platform-jump", SoundEffect.class));
                soundEngine.registerSoundEffect("pew", directory.getEntry("platform-pew", SoundEffect.class));
                soundEngine.registerSoundEffect("plop", directory.getEntry("platform-plop", SoundEffect.class));
                soundEngine.registerSoundEffect("dirtFootStep", directory.getEntry("dirtFootStep", SoundEffect.class));
                soundEngine.registerSoundEffect("torchThrow", directory.getEntry("torchThrow", SoundEffect.class));

                soundEngine.registerMusic("in_game", directory.getEntry("in_game_music", Music.class));
                ArrayList<String> temp = new ArrayList<>();
                temp.add("in_game");
                soundEngine.startMusicLoop(temp);

                // Initialize the gameplay scene and level data.
                currentScene = new GameplayScene(directory, soundEngine, "platform");
                levels = new String[]{"level1", "level7", "level9"};
                currentScene.loadLevel(levels[selectedLevel - 1], "rope_test");
                currentScene.setScreenListener(this);
                currentScene.setSpriteBatch(batch);
                current = selectedLevel - 1;
                setScreen(currentScene);

                return;
            }

            // Handle exit codes from any GameplayScene.
            if (screen instanceof GameplayScene) {
                if (exitCode == GameplayScene.EXIT_NEXT) {
                    currentScene.clearLevel();
                    current = (current + 1) % levels.length;
                    currentScene.loadLevel(levels[current], "rope_test");
                    currentScene.reset();
                    setScreen(currentScene);
                } else if (exitCode == GameplayScene.EXIT_PREV) {
                    currentScene.clearLevel();
                    current = (current - 1 + levels.length) % levels.length;
                    currentScene.loadLevel(levels[current], "rope_test");
                    currentScene.reset();
                    setScreen(currentScene);
                } else if (exitCode == GameplayScene.EXIT_QUIT) {
                    LevelSelectScene levelSelect = new LevelSelectScene();
                    levelSelect.setScreenListener(this);
                    setScreen(levelSelect);
                    soundEngine.stopMusicLoop();
                    soundEngine.registerMusic("menu_music", directory.getEntry("menu_music", Music.class));
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add("menu_music");
                    soundEngine.startMusicLoop(temp);
                    return;
                }
            }
        }
    }
}
