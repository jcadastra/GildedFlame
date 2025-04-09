package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
    boolean startClicked = false;
    private ScreenListener listener;
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }



    public MainMenuScreen() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin();
        createBasicUI();
    }
    public boolean getStartClicked(){
        return startClicked;
    }

    private void createBasicUI() {
        // Create a basic font
        BitmapFont font = new BitmapFont();  // Default font

        // Create a simple white texture and turn it into a drawable
        Pixmap pixmap = new Pixmap(200, 60, Pixmap.Format.RGB888);
        pixmap.setColor(1, 1, 1, 1);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose(); // We can dispose pixmap after texture is created
        Drawable drawable = new TextureRegionDrawable(texture);

        // Create a TextButtonStyle and add resources to the skin
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = drawable;
        textButtonStyle.font = font;
        skin.add("default", textButtonStyle);

        TextButton playButton = new TextButton("Choose Level", skin);
        playButton.setSize(200, 60);
        playButton.setPosition(
            Gdx.graphics.getWidth()/2 - 100,
            Gdx.graphics.getHeight()/2 - 30
        );

        playButton.addListener(new com.badlogic.gdx.scenes.scene2d.InputListener() {
            @Override
            public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event,
                                     float x, float y, int pointer, int button) {
                // Handle button press
                System.out.println("Button pressed");
                startClicked = true;
                System.out.println(startClicked);
                if (listener != null) {
                    listener.exitScreen(MainMenuScreen.this, 1); // You choose any exit code
                }
                return true;


            }
        });

        stage.addActor(playButton);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.8f, 0.2f, 0.2f, 1);
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
}
