package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.ScreenListener;
import com.badlogic.gdx.audio.Sound;

public class FailureScene implements Screen {
    private Stage stage;
    private Skin skin;
    private Texture bgTexture;
    private boolean visible = false;
    private boolean startClicked = false;
    private ScreenListener listener;
    private InputController inputController = InputController.getInstance();
    private boolean prevButtonA = false;
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

        stage.addActor(retryButton);
        stage.addActor(mainMenuButton);
        stage.addActor(chambersButton);

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

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        if(!animationFinished){
            frameTimer += delta;
            if(frameTimer >= FRAME_DURATION) {
                frameTimer = 0f;
                currentFrameIndex++;
                if(currentFrameIndex >= TOTAL_FRAMES) {
                    currentFrameIndex = TOTAL_FRAMES - 1;
                    animationFinished = true;
                }
            }
        }
        int srcX = currentFrameIndex*FRAME_WIDTH;
        int srcY = 0;
        stage.getBatch().begin();
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
