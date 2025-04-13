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


public class MainMenuScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private Texture bgTexture;
    private boolean startClicked = false;
    private ScreenListener listener;
    private Sound clickSound;

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    public MainMenuScreen() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin();
        clickSound = Gdx.audio.newSound(Gdx.files.internal("soundEffects/clickSound.mp3"));

        bgTexture = new Texture(Gdx.files.internal("loading/menuScreen.png"));
        Image bgImage = new Image(bgTexture);
        bgImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.addActor(bgImage);

        createBasicUI();
    }

    public boolean getStartClicked(){
        return startClicked;
    }

    private void createBasicUI() {
        BitmapFont font = new BitmapFont();

        Pixmap pixmap = new Pixmap(200, 60, Pixmap.Format.RGB888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        Drawable drawable = new TextureRegionDrawable(texture);

        Texture newGameText = new Texture(Gdx.files.internal("ui/newGameButt.png"));

        TextureRegionDrawable buttonUp = new TextureRegionDrawable(new TextureRegion(newGameText));


        ImageButton.ImageButtonStyle newGame = new ImageButton.ImageButtonStyle();
        newGame.up = buttonUp;

        Texture contText = new Texture(Gdx.files.internal("ui/contbutt.png"));
        TextureRegionDrawable contButtUp = new TextureRegionDrawable(new TextureRegion(contText));

        ImageButton.ImageButtonStyle contButt = new ImageButton.ImageButtonStyle();
        contButt.up = contButtUp;

        Texture settingsText = new Texture(Gdx.files.internal("ui/settingsbutt.png"));
        TextureRegionDrawable settingsTextUp = new TextureRegionDrawable(new TextureRegion(settingsText));

        ImageButton.ImageButtonStyle settingsButt = new ImageButton.ImageButtonStyle();
        settingsButt.up = settingsTextUp;





        ImageButton imageButton = new ImageButton(newGame);
        ImageButton contButt1 = new ImageButton(contButt);
        ImageButton settingsButt1 = new ImageButton(settingsButt);

        imageButton.setSize(200, 60);
        imageButton.setPosition(
            (Gdx.graphics.getWidth() / 2f - 200 / 2f) + 325,
            Gdx.graphics.getHeight() / 2f - 30
        );
        contButt1.setSize(200, 60);
        contButt1.setPosition(
            (Gdx.graphics.getWidth() / 2f - 200 / 2f) + 325,
            Gdx.graphics.getHeight() / 2f - 30 - 90
        );

        settingsButt1.setSize(200, 60);
        settingsButt1.setPosition(
            (Gdx.graphics.getWidth() / 2f - 200 / 2f) + 325,
            Gdx.graphics.getHeight() / 2f - 30 - 180
        );



// Add your input listener
        imageButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                System.out.println("ImageButton pressed");
                clickSound.play();
                startClicked = true;
                if (listener != null) {
                    listener.exitScreen(MainMenuScreen.this, 1);
                }
                return true;
            }
        });
        contButt1.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                clickSound.play();
                System.out.println("ImageButton pressed");
                startClicked = true;
                if (listener != null) {
                    listener.exitScreen(MainMenuScreen.this, 1);
                }
                return true;
            }
        });
        settingsButt1.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                clickSound.play();
                System.out.println("ImageButton pressed");
                startClicked = true;
                if (listener != null) {
                    listener.exitScreen(MainMenuScreen.this, 1);
                }
                return true;
            }
        });



// Add the ImageButton to your stage
        stage.addActor(imageButton);
        stage.addActor(contButt1);
        stage.addActor(settingsButt1);


        Texture topRightTexture = new Texture(Gdx.files.internal("ui/textbutt.png"));
        Image topRightImage = new Image(topRightTexture);

        topRightImage.setSize(300, 150);

        topRightImage.setPosition(
            Gdx.graphics.getWidth() - topRightImage.getWidth()-30,
            Gdx.graphics.getHeight() - topRightImage.getHeight()-80
        );

        // Add the image actor to the stage.
        stage.addActor(topRightImage);

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
        bgTexture.dispose();
    }
}
