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

//    private String[] levels = new String[]{"move_intro","jump_intro","throw_intro","climb_intro","advanced_movement","box_intro","totem_intro","moth_intro","moth_medium","moth_medium_2", "rune_intro", "rune_medium", "rain_intro", "rain_medium", "rune_hard", "new_1", "new_2"};
    private String[] levels = new String[]{"level1","level2","level3","level4","advanced_movement","new_2","harvey_test2","moth_intro","moth_medium","moth_medium_2", "advanced_movement", "rune_medium", "rain_intro", "rain_medium", "rune_hard"};
    private GameplayScene currentScene;
    private LevelSelectScene levelSelectScene;

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
            soundEngine.populateSoundEngine(directory);

            swapCreateMainMenuScene();
            return;
        }
        // Handle exit from the main menu.
        else if (screen instanceof MainMenuScreen) {
            if (exitCode == -1) {
                SavedDataHandler handler = new SavedDataHandler();
                String lastCompleted = handler.getDataVal("lastLevel");
                int nextLevel = 1;
                try {
                    if (lastCompleted != null) {
                        nextLevel = Integer.parseInt(lastCompleted) + 1;
                    }
                } catch (NumberFormatException e) {
                    Gdx.app.error("GDXRoot", "Invalid saved level: " + lastCompleted);
                }
                current = Math.min(nextLevel - 1, levels.length - 1);
                createSetGamePlayScene(current);
            } else if (exitCode == 1) {
                // Fallback: go to level select
                swapCreateLevelSelect();
            } else {
                directory.dispose();
                Gdx.app.exit();
            }
            return;
        }

        // Handle exit from the level selection screen.
        else if (screen instanceof LevelSelectScene) {
            if (exitCode == 0) {
                swapCreateMainMenuScene();
                return;
            }
            int selectedLevel = ((LevelSelectScene) screen).getSelectedLevel();
            createSetGamePlayScene(selectedLevel-1);
            return;
        }

        else if (screen instanceof SuccessScene) {
            if (exitCode == GameplayScene.EXIT_NEXT) {
                current = (current + 1) % levels.length;
            } else if (exitCode == GameplayScene.EXIT_QUIT) {
                swapCreateLevelSelect();
                return;
            }
            createSetGamePlayScene(current);
            return;
        }
        else if (screen instanceof FailureScene) {
            if (exitCode == GameplayScene.EXIT_MAINMENU) {
                swapCreateMainMenuScene();
                return;
            } else if (exitCode == GameplayScene.EXIT_REPLAY) {
                createSetGamePlayScene(current);
                return;
            } else if (exitCode == GameplayScene.EXIT_QUIT) {
                swapCreateLevelSelect();
                return;
            }
        }

        // Handle exit codes from any GameplayScene.
        if (screen instanceof GameplayScene) {
            if (exitCode == GameplayScene.EXIT_NEXT) {
                swapGamePlayScene(1);
            } else if (exitCode == GameplayScene.EXIT_PREV) {
                swapGamePlayScene(-1);
            } else if (exitCode == GameplayScene.EXIT_QUIT) {
                swapCreateLevelSelect();
            } else if (exitCode == GameplayScene.EXIT_SUCCESS) {
                createSwapSuccess();
            } else if (exitCode == GameplayScene.EXIT_FAILURE) {
                createSwapFailure();
            }
            return;
        }
    }

    private void swapCreateMainMenuScene() {
        MainMenuScreen mainMenu = new MainMenuScreen(directory, soundEngine);
        mainMenu.setScreenListener(this);
        setScreen(mainMenu);
        ArrayList<String> temp = new ArrayList<>();
        temp.add("menu_music");
        soundEngine.startMusicLoop(temp);
    }

    public void swapCreateLevelSelect() {
        if (levelSelectScene == null) {
            levelSelectScene = new LevelSelectScene();
            levelSelectScene.setScreenListener(this);
        }
        setScreen(levelSelectScene);
        ArrayList<String> temp = new ArrayList<>();
        temp.add("menu_music");
        soundEngine.startMusicLoop(temp);
    }

    private void swapGamePlayScene(int delta) {
        createSetGamePlayScene(current +delta);
    }

    public void setGamePlaySceneLevel(int level) {
        ArrayList<String> temp = new ArrayList<>();
        temp.add("in_game");
        soundEngine.startMusicLoop(temp);
        currentScene.clearLevel();
        current = (level) % levels.length;
        System.out.println();
        currentScene.levelName = levels[current];
        setScreen(currentScene);
        System.out.println("done loading level " + levels[current]);
    }

    private void createSetGamePlayScene(int level) {
        if (currentScene == null) {
            currentScene = new GameplayScene(directory, soundEngine, "platform");
            currentScene.setScreenListener(this);
            currentScene.setSpriteBatch(batch);
        }
        current = level;
        setGamePlaySceneLevel(level);
    }

    private void createSwapSuccess() {
        SuccessScene success = new SuccessScene(directory, soundEngine);
        success.setScreenListener(this);
        success.setCurrentLevel(current+1);
        setScreen(success);
        soundEngine.successSound();
    }

    private void createSwapFailure() {
        FailureScene failure = new FailureScene(directory, soundEngine);
        failure.setScreenListener(this);
        setScreen(failure);
        soundEngine.failureSound();
    }
}
