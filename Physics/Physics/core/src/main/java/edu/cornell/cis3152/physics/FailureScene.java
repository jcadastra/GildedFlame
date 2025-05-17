package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;
import com.badlogic.gdx.audio.Sound;
import edu.cornell.gdiac.util.XBoxController;

public class FailureScene implements Screen {
    private Stage stage;
    private Skin skin;
    private Texture bgTexture;
    private boolean visible = false;
    private boolean startClicked = false;
    private ScreenListener listener;
    private InputController inputController = InputController.getInstance();
    protected SoundEngine soundEngine;
    /** The asset directory for retrieving textures, atlases */
    protected AssetDirectory directory;
    private Texture animationTexture;
    private int currentFrameIndex = 0;
    private float frameTimer = 0f;
    private int FRAME_WIDTH = 500;
    private int FRAME_HEIGHT = 500;
    private int TOTAL_FRAMES = 17;
    private static final float FRAME_DURATION = 0.1f;
    private boolean animationFinished = false;
    private int death_code;
    private Array<Entry> entries = new Array<>();
    private boolean prevButtonA = false;
    private float joystickCooldown = 0f;
    private int currentIndex = 0;
    private int scrollIndex = 0 ;

    private class Entry {
        private ImageButton button;
        private TextureRegionDrawable defaultDrawable;
        private TextureRegionDrawable selectedDrawable;

        public Entry(ImageButton button, TextureRegionDrawable defaultDrawable, TextureRegionDrawable selectedDrawable) {
            this.button = button;
            this.defaultDrawable = defaultDrawable;
            this.selectedDrawable = selectedDrawable;
        }
    }


    public FailureScene(AssetDirectory directory, SoundEngine soundEngine, int death_code) {
        this.directory = directory;
        this.soundEngine = soundEngine;
        this.death_code = death_code;
        stage = new Stage(new ScreenViewport());
        skin = new Skin();
        createBasicUI();
    }

    private void createBasicUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        Texture retryText = directory.getEntry("retryButt", Texture.class);
        Texture retryClickText = directory.getEntry("retryButtClick", Texture.class);
        TextureRegionDrawable retryButtUp = new TextureRegionDrawable(new TextureRegion(retryText));
        TextureRegionDrawable retryButtOver = new TextureRegionDrawable(new TextureRegion(retryClickText));

        ImageButton.ImageButtonStyle retryButt = new ImageButton.ImageButtonStyle();
        retryButt.up = retryButtUp;
        retryButt.over = retryButtOver;

        Texture mainMenuText = directory.getEntry("mainMenuButt", Texture.class);
        Texture mainMenuClickText = directory.getEntry("mainMenuButtClick", Texture.class);
        TextureRegionDrawable mainMenuButtUp = new TextureRegionDrawable(new TextureRegion(mainMenuText));
        TextureRegionDrawable mainMenuButtOver = new TextureRegionDrawable(new TextureRegion(mainMenuClickText));

        ImageButton.ImageButtonStyle mainMenuButt = new ImageButton.ImageButtonStyle();
        mainMenuButt.up = mainMenuButtUp;
        mainMenuButt.over = mainMenuButtOver;

        Texture chambersText = directory.getEntry("chambersButt", Texture.class);
        Texture chambersClickText = directory.getEntry("chambersButtClick", Texture.class);
        TextureRegionDrawable chambersButtUp = new TextureRegionDrawable(new TextureRegion(chambersText));
        TextureRegionDrawable chambersButtOver = new TextureRegionDrawable(new TextureRegion(chambersClickText));

        ImageButton.ImageButtonStyle chambersButt = new ImageButton.ImageButtonStyle();
        chambersButt.up = chambersButtUp;
        chambersButt.over = chambersButtOver;

        ImageButton mainMenuButton = new ImageButton(mainMenuButt);
        ImageButton retryButton = new ImageButton(retryButt);
        ImageButton chambersButton = new ImageButton(chambersButt);

        retryButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        retryButton.setPosition(
            screenWidth * 0.52f,
            screenHeight * 0.5f
        );

        mainMenuButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        mainMenuButton.setPosition(
            screenWidth * 0.52f,
            screenHeight * 0.3f
        );

        chambersButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        chambersButton.setPosition(
            screenWidth * 0.52f,
            screenHeight * 0.4f
        );

        retryButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                System.out.println("ImageButton pressed");
                soundEngine.playSoundEffect("clickSound");
                startClicked = true;
                if (listener != null) {
                    listener.exitScreen(FailureScene.this, 4);
                }
                return true;
            }
        });
        mainMenuButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                soundEngine.playSoundEffect("clickSound");
                System.out.println("ImageButton pressed");
                startClicked = true;
                if (listener != null) {
                    listener.exitScreen(FailureScene.this, 5);
                }
                return true;
            }
        });
        chambersButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                soundEngine.playSoundEffect("clickSound");
                System.out.println("ImageButton pressed");
                startClicked = true;
                if (listener != null) {
                    listener.exitScreen(FailureScene.this, 0);
                }
                return true;
            }
        });

        Entry retry = new Entry(retryButton,retryButtUp,retryButtOver);
        entries.add(retry);
        stage.addActor(retryButton);
        Entry chambers = new Entry(chambersButton,chambersButtUp,chambersButtOver);
        stage.addActor(chambersButton);
        entries.add(chambers);
        Entry mainMenu = new Entry(mainMenuButton,mainMenuButtUp,mainMenuButtOver);
        stage.addActor(mainMenuButton);
        entries.add(mainMenu);

        Texture topTexture = directory.getEntry("failed_text", Texture.class);
        Image topImage = new Image(topTexture);

        topImage.setSize(screenWidth * 0.66f, screenHeight * 0.1f);

        topImage.setPosition(
            screenWidth * 0.17f,
            screenHeight * 0.78f
        );

        // Add the image actor to the stage
        stage.addActor(topImage);

        if (death_code == 0) {
            FRAME_WIDTH = 1000;
            FRAME_HEIGHT = 1000;
            TOTAL_FRAMES = 33;
            animationTexture = directory.getEntry("torchOffANIMATION", Texture.class);
        } else if (death_code == 1) {
            FRAME_WIDTH = 500;
            FRAME_HEIGHT = 823;
            TOTAL_FRAMES = 6;
            animationTexture = directory.getEntry("maskFallANIMATION", Texture.class);
        } else if (death_code == 2) {
            FRAME_WIDTH = 500;
            FRAME_HEIGHT = 500;
            TOTAL_FRAMES = 17;
            animationTexture = directory.getEntry("maskCrumbleANIMATION", Texture.class);
        }

    }


    private void controllerSelect() {
        if (inputController.isUsingController()) {
            XBoxController xbox = inputController.xbox;
            boolean start = xbox.getA();
            Entry currentEntry = entries.get(currentIndex);
            ImageButton.ImageButtonStyle style = currentEntry.button.getStyle();
            style.up = currentEntry.selectedDrawable;
            currentEntry.button.setStyle(style);
            if (start && !prevButtonA) {
                int selectionIndex = scrollIndex + currentIndex;
                if (selectionIndex >= 0 && selectionIndex < entries.size) {
                    // Simulate touch down
                    InputEvent downEvent = new InputEvent();
                    downEvent.setType(InputEvent.Type.touchDown);
                    downEvent.setStage(stage);
                    downEvent.setTarget(currentEntry.button);
                    downEvent.setButton(0);
                    currentEntry.button.fire(downEvent);

                    // Simulate touch up
                    InputEvent upEvent = new InputEvent();
                    upEvent.setType(InputEvent.Type.touchUp);
                    upEvent.setStage(stage);
                    upEvent.setTarget(currentEntry.button);
                    upEvent.setButton(0);
                    currentEntry.button.fire(upEvent);

                }
            }
            prevButtonA = start;

            boolean moved = false;
            float vertical = xbox.getLeftY();
            System.out.println("joystick cool down"+joystickCooldown);
            if (joystickCooldown <= 0) {
                if (vertical < -0.4f && !moved) {
//                    particleEngine.dispose();
                    ImageButton.ImageButtonStyle s = currentEntry.button.getStyle();
                    s.up = currentEntry.defaultDrawable;
                    currentEntry.button.setStyle(s);
                    //currentEntry.button.getImage().setDrawable(currentEntry.defaultDrawable);
                    currentIndex = Math.max(currentIndex - 1, 0);  // Prevent going negative
                    moved = true;
                    joystickCooldown = 0.25f;
                } else if (vertical > 0.4f && !moved) {
                    ImageButton.ImageButtonStyle s = currentEntry.button.getStyle();
                    s.up = currentEntry.defaultDrawable;
                    currentEntry.button.setStyle(s);
                    currentIndex = Math.min(currentIndex + 1, entries.size-1);  // Prevent going off right
                    moved = true;
                    joystickCooldown = 0.25f;
                }
                if (Math.abs(vertical) <= 0.01f) {
                    moved = false;
                }
            }
            joystickCooldown -= Gdx.graphics.getDeltaTime();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        controllerSelect();
        stage.act(delta);
        stage.draw();
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        if(!animationFinished){
            frameTimer += delta;
            if(frameTimer >= FRAME_DURATION) {
                frameTimer = 0f;
                currentFrameIndex++;
                if(currentFrameIndex >= TOTAL_FRAMES && death_code != 1) {
                    currentFrameIndex = TOTAL_FRAMES - 1;
                    animationFinished = true;
                }
                else if(currentFrameIndex >= TOTAL_FRAMES) {
                    currentFrameIndex = 0;
                }
            }
        }
        int srcX = currentFrameIndex*FRAME_WIDTH;
        int srcY = 0;
        if (death_code == 0) {
            int framesPerRow = 5;
            int row = currentFrameIndex / framesPerRow;
            int col = currentFrameIndex % framesPerRow;

            srcX = col * FRAME_WIDTH;
            srcY = row * FRAME_HEIGHT;
        }

        stage.getBatch().begin();
        if (death_code == 1) {
            stage.getBatch().draw(
                    animationTexture,
                    screenWidth * 0.25f,
                    screenHeight * 0.2f,
                    screenWidth * 0.204f,
                    screenHeight * 0.6f,
                    srcX, srcY,
                    FRAME_WIDTH, FRAME_HEIGHT,
                    false, false
            );
        }
        else if (death_code == 0) {
            stage.getBatch().draw(
                    animationTexture,
                    screenWidth * 0.2f,
                    screenHeight * 0.2f,
                    screenWidth * 0.4f,
                    screenHeight * 0.5f,
                    srcX, srcY,
                    FRAME_WIDTH, FRAME_HEIGHT,
                    false, false
            );
        }
        else {
            stage.getBatch().draw(
                    animationTexture,
                    screenWidth * 0.1f,
                    screenHeight * 0.2f,
                    screenWidth * 0.4f,
                    screenHeight * 0.5f,
                    srcX, srcY,
                    FRAME_WIDTH, FRAME_HEIGHT,
                    false, false
            );
        }
        stage.getBatch().end();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
}
