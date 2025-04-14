package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.gdiac.util.ScreenListener;

public class MainMenuScreen implements Screen {
    private Stage stage;
    private Skin skin;
    private Texture bgTexture;
    private boolean startClicked = false;
    private ScreenListener listener;

    /*Checker for controller support*/
    private boolean prevButtonA = false;
    private TextButton playButton;

    /*Gets the controller*/
    private InputController inputController = InputController.getInstance();

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    public MainMenuScreen() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin();

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

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = drawable;
        textButtonStyle.font = font;
        skin.add("default", textButtonStyle);

        // Create the "Choose Level" button.
        textButtonStyle.fontColor = Color.DARK_GRAY;

        playButton = new TextButton("Choose Level", skin);
        playButton.setSize(200, 60);
        playButton.setPosition(
                (Gdx.graphics.getWidth() / 2f - 200 / 2f) + 325,
                Gdx.graphics.getHeight() / 2 - 30
        );
        if (inputController.isUsingController()){// show controller select
            playButton.setColor(Color.GRAY);
        }

        // for mouse and keyboard
        playButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                System.out.println("Button pressed");
                startClicked = true;
                System.out.println(startClicked);
                if (listener != null) {
                    // Use an exit code of 1 (this code can be customized as needed)
                    listener.exitScreen(MainMenuScreen.this, 1);
                }
                return true;
            }
        });

        stage.addActor(playButton);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // begin controller listening
        if (inputController.isUsingController()){
        boolean start= inputController.xbox.getA();
        if(start && !prevButtonA){
            InputEvent downEvent = new InputEvent();
            downEvent.setType(InputEvent.Type.touchDown);
            downEvent.setStage(stage);
            downEvent.setTarget(playButton);
            downEvent.setButton(0);
            playButton.fire(downEvent);

            InputEvent upEvent = new InputEvent();
            upEvent.setType(InputEvent.Type.touchUp);
            upEvent.setStage(stage);
            upEvent.setTarget(playButton);
            upEvent.setButton(0);
            playButton.fire(upEvent);
        }
        prevButtonA =start;}
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
