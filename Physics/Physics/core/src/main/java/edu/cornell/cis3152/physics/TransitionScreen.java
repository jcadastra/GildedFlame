package edu.cornell.cis3152.physics;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class TransitionScreen implements Screen {
    private final Screen oldScreen;
    private final Stage stage;

    public TransitionScreen(Screen oldScreen, Screen nextScreen, GDXRoot game, String text) {
        this.oldScreen  = oldScreen;
        this.stage = new Stage(new ScreenViewport());

        Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture blackTex = new Texture(pixmap);
        pixmap.dispose();

        Image black = new Image(blackTex);
        black.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        black.getColor().a = 0f;
        stage.addActor(black);

        BitmapFont font = new BitmapFont();
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        Label label = new Label(text, style);
        label.setFontScale(2f);
        label.setPosition((Gdx.graphics.getWidth()  - label.getWidth() ) / 2f,(Gdx.graphics.getHeight() - label.getHeight()) / 2f);
        label.getColor().a = 0f;
        stage.addActor(label);

        black.addAction(Actions.sequence(
            Actions.fadeIn(1f),
            Actions.delay(1.5f),
            Actions.run(() -> {
                game.setScreen(nextScreen);
                dispose();
            })
        ));
        label.addAction(Actions.sequence(
            Actions.fadeIn(1f),
            Actions.delay(1.5f)
        ));
    }

    float total = 0;
    @Override
    public void render(float delta) {
        if (total < 1) {
            oldScreen.render(delta);
            total += delta;
        }
        Gdx.gl.glEnable(GL20.GL_BLEND);
        stage.act(delta);
        stage.draw();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override public void resize(int width, int height) {}
    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override
    public void dispose() {
        stage.dispose();
    }
}
