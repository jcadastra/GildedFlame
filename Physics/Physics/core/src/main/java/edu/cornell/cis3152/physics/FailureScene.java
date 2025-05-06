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
    private Sound clickSound;
    private InputController inputController = InputController.getInstance();
    private boolean prevButtonA = false;
    /** The asset directory for retrieving textures, atlases */
    protected AssetDirectory directory;

    public FailureScene(AssetDirectory directory) {
        this.directory = directory;
        stage = new Stage(new ScreenViewport());
        skin = new Skin();
        clickSound = Gdx.audio.newSound(Gdx.files.internal("soundEffects/clickSound.mp3"));
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
            screenWidth * 0.40f,
            screenHeight * 0.4f
        );

        mainMenuButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        mainMenuButton.setPosition(
            screenWidth * 0.40f,
            screenHeight * 0.2f
        );

        chambersButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        chambersButton.setPosition(
            screenWidth * 0.40f,
            screenHeight * 0.3f
        );

        retryButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                System.out.println("ImageButton pressed");
                clickSound.play();
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
                clickSound.play();
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
                clickSound.play();
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
            screenHeight * 0.65f
        );

        // Add the image actor to the stage
        stage.addActor(topImage);

        Texture treasureTexture = directory.getEntry("torchOff", Texture.class);
        Image treasureImage = new Image(treasureTexture);

        treasureImage.setSize(screenWidth * 0.08f, screenHeight * 0.1f);

        treasureImage.setPosition(
            screenWidth * 0.46f,
            screenHeight * 0.5f
        );

        // Add the image actor to the stage
        stage.addActor(treasureImage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
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
