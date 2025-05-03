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
import edu.cornell.gdiac.util.ScreenListener;
import com.badlogic.gdx.audio.Sound;

public class SuccessScene implements Screen {
    private Stage stage;
    private Skin skin;
    private Texture bgTexture;
    private boolean visible = false;
    private boolean startClicked = false;
    private ScreenListener listener;
    private Sound clickSound;
    private InputController inputController = InputController.getInstance();
    private boolean prevButtonA = false;

    public SuccessScene() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin();
        clickSound = Gdx.audio.newSound(Gdx.files.internal("soundEffects/clickSound.mp3"));
        createBasicUI();
    }

    private void createBasicUI() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        Texture contText = new Texture(Gdx.files.internal("ui/contbutt.png"));
        Texture contClickText = new Texture(Gdx.files.internal("ui/contButtClick.png"));
        TextureRegionDrawable contButtUp = new TextureRegionDrawable(new TextureRegion(contText));
        TextureRegionDrawable contButtOver = new TextureRegionDrawable(new TextureRegion(contClickText));

        ImageButton.ImageButtonStyle contButt = new ImageButton.ImageButtonStyle();
        contButt.up = contButtUp;
        contButt.over = contButtOver;

        Texture replayText = new Texture(Gdx.files.internal("ui/replayButt.png"));
        Texture replayClickText = new Texture(Gdx.files.internal("ui/replayButtClick.png"));
        TextureRegionDrawable replayButtUp = new TextureRegionDrawable(new TextureRegion(replayText));
        TextureRegionDrawable replayButtOver = new TextureRegionDrawable(new TextureRegion(replayClickText));

        ImageButton.ImageButtonStyle replayButt = new ImageButton.ImageButtonStyle();
        replayButt.up = replayButtUp;
        replayButt.over = replayButtOver;

        Texture chambersText = new Texture(Gdx.files.internal("ui/chambersButt.png"));
        Texture chambersClickText = new Texture(Gdx.files.internal("ui/chambersButtClick.png"));
        TextureRegionDrawable chambersButtUp = new TextureRegionDrawable(new TextureRegion(chambersText));
        TextureRegionDrawable chambersButtOver = new TextureRegionDrawable(new TextureRegion(chambersClickText));

        ImageButton.ImageButtonStyle chambersButt = new ImageButton.ImageButtonStyle();
        chambersButt.up = chambersButtUp;
        chambersButt.over = chambersButtOver;

        ImageButton replayButton = new ImageButton(replayButt);
        ImageButton contButton = new ImageButton(contButt);
        ImageButton chambersButton = new ImageButton(chambersButt);

        contButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        contButton.setPosition(
                screenWidth * 0.40f,
                screenHeight * 0.4f
        );

        replayButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        replayButton.setPosition(
                screenWidth * 0.40f,
                screenHeight * 0.3f
        );

        chambersButton.setSize(screenWidth * 0.2f, screenHeight * 0.06f);
        chambersButton.setPosition(
                screenWidth * 0.40f,
                screenHeight * 0.2f
        );

        contButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                System.out.println("ImageButton pressed");
                clickSound.play();
                startClicked = true;
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
                clickSound.play();
                System.out.println("ImageButton pressed");
                startClicked = true;
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
                clickSound.play();
                System.out.println("ImageButton pressed");
                startClicked = true;
                if (listener != null) {
                    listener.exitScreen(SuccessScene.this, 0);
                }
                return true;
            }
        });

        stage.addActor(contButton);
        stage.addActor(replayButton);
        stage.addActor(chambersButton);

        Texture topTexture = new Texture(Gdx.files.internal("ui/chamber_explored.png"));
        Image topImage = new Image(topTexture);

        topImage.setSize(screenWidth * 0.66f, screenHeight * 0.1f);

        topImage.setPosition(
                screenWidth * 0.17f,
                screenHeight * 0.65f
        );

        // Add the image actor to the stage
        stage.addActor(topImage);

        Texture treasureTexture = new Texture(Gdx.files.internal("ui/treasureGlow.gif"));
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
        Gdx.gl.glClearColor(1, 1, 1, 1);
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
