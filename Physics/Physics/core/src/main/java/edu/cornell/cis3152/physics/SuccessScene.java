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

public class SuccessScene implements Screen {
    private Stage stage;
    private Skin skin;
    private Texture bgTexture;
    private boolean visible = false;
    private boolean startClicked = false;
    private ScreenListener listener;
    private InputController inputController = InputController.getInstance();
    private boolean prevButtonA = false;
    private float joystickCooldown = 0f;
    private int currentLevel;
    protected SoundEngine soundEngine;
    /** The asset directory for retrieving textures, atlases */
    protected AssetDirectory directory;
    private Texture animationTexture;
    private int currentFrameIndex = 0;
    private float frameTimer = 0f;
    private int FRAME_WIDTH = 350;
    private int FRAME_HEIGHT = 250;
    private int TOTAL_FRAMES = 3;
    private static final float FRAME_DURATION = 0.1f;

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

    private Array<Entry> entries = new Array<>();
    private int currentIndex = 0;
    private int scrollIndex = 0;


    public SuccessScene(AssetDirectory directory, SoundEngine soundEngine) {
        this.directory = directory;
        stage = new Stage(new ScreenViewport());
        skin = new Skin();
        this.soundEngine = soundEngine;
        createBasicUI();
    }

    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }


    private void createBasicUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        Texture contText = directory.getEntry("contButt", Texture.class);
        Texture contClickText = directory.getEntry("contButtClick", Texture.class);
        TextureRegionDrawable contButtUp = new TextureRegionDrawable(new TextureRegion(contText));
        TextureRegionDrawable contButtOver = new TextureRegionDrawable(new TextureRegion(contClickText));

        ImageButton.ImageButtonStyle contButt = new ImageButton.ImageButtonStyle();
        contButt.up = contButtUp;
        contButt.over = contButtOver;

        Texture replayText = directory.getEntry("replayButt", Texture.class);
        Texture replayClickText = directory.getEntry("replayButtClick", Texture.class);
        TextureRegionDrawable replayButtUp = new TextureRegionDrawable(new TextureRegion(replayText));
        TextureRegionDrawable replayButtOver = new TextureRegionDrawable(new TextureRegion(replayClickText));

        ImageButton.ImageButtonStyle replayButt = new ImageButton.ImageButtonStyle();
        replayButt.up = replayButtUp;
        replayButt.over = replayButtOver;

        Texture chambersText = directory.getEntry("chambersButt", Texture.class);
        Texture chambersClickText = directory.getEntry("chambersButtClick", Texture.class);
        TextureRegionDrawable chambersButtUp = new TextureRegionDrawable(new TextureRegion(chambersText));
        TextureRegionDrawable chambersButtOver = new TextureRegionDrawable(new TextureRegion(chambersClickText));

        ImageButton.ImageButtonStyle chambersButt = new ImageButton.ImageButtonStyle();
        chambersButt.up = chambersButtUp;
        chambersButt.over = chambersButtOver;


        ImageButton contButton = new ImageButton(contButt);
        Entry cont = new Entry(contButton,contButtUp,contButtOver);
        entries.add(cont);
        ImageButton replayButton = new ImageButton(replayButt);;
        Entry repl = new Entry(replayButton,replayButtUp,replayButtOver);
        entries.add(repl);
        ImageButton chambersButton = new ImageButton(chambersButt);
        Entry chamb = new Entry(chambersButton,chambersButtUp,chambersButtOver);
        entries.add(chamb);

        contButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        contButton.setPosition(
                screenWidth * 0.52f,
                screenHeight * 0.5f
        );

        replayButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        replayButton.setPosition(
                screenWidth * 0.52f,
                screenHeight * 0.4f
        );

        chambersButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        chambersButton.setPosition(
                screenWidth * 0.52f,
                screenHeight * 0.3f
        );

        contButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                System.out.println("ImageButton pressed");
                soundEngine.playSoundEffect("clickSound");
                startClicked = true;
                SavedDataHandler handler = new SavedDataHandler();
                String levelKey = "level" + currentLevel;
                handler.setDataVal(levelKey, "true");
                handler.setDataVal("lastLevel", String.valueOf(currentLevel));
                System.out.println("level saved: " + String.valueOf(currentLevel));
                if (listener != null) {
                    listener.exitScreen(SuccessScene.this, 1);
                }
                return true;
            }
        });
        replayButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                soundEngine.playSoundEffect("clickSound");
                System.out.println("ImageButton pressed");
                startClicked = true;
                SavedDataHandler handler = new SavedDataHandler();
                String levelKey = "level" + currentLevel;
                handler.setDataVal(levelKey, "true");
                handler.setDataVal("lastLevel", String.valueOf(currentLevel));
                System.out.println("level saved: " + String.valueOf(currentLevel));
                if (listener != null) {
                    listener.exitScreen(SuccessScene.this, 4);
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
                SavedDataHandler handler = new SavedDataHandler();
                String levelKey = "level" + currentLevel;
                handler.setDataVal(levelKey, "true");
                handler.setDataVal("lastLevel", String.valueOf(currentLevel));
                System.out.println("level saved: " + String.valueOf(currentLevel));
                if (listener != null) {
                    listener.exitScreen(SuccessScene.this, 0);
                }
                return true;
            }
        });

        stage.addActor(contButton);
        stage.addActor(replayButton);
        stage.addActor(chambersButton);

        Texture topTexture = directory.getEntry("chamber_explored", Texture.class);
        Image topImage = new Image(topTexture);

        topImage.setSize(screenWidth * 0.66f, screenHeight * 0.1f);

        topImage.setPosition(
                screenWidth * 0.17f,
                screenHeight * 0.78f
        );

        // Add the image actor to the stage
        stage.addActor(topImage);

        animationTexture = directory.getEntry("successANIMATION", Texture.class);
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

        frameTimer += delta;
        if(frameTimer >= FRAME_DURATION) {
            frameTimer = 0f;
            currentFrameIndex++;
            if(currentFrameIndex >= TOTAL_FRAMES) {
                currentFrameIndex = 0;
            }
        }

        int srcX = currentFrameIndex*FRAME_WIDTH;
        int srcY = 0;
        stage.getBatch().begin();
        stage.getBatch().draw(
                animationTexture,
                screenWidth * 0.2f,
                screenHeight * 0.29f,
                screenWidth * 0.24f,
                screenHeight * 0.3f,
                srcX, srcY,
                FRAME_WIDTH, FRAME_HEIGHT,
                false, false
        );
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
