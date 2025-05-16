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
    private enum State { FADE_IN, HOLD, FADE_OUT, DONE }
    private State state = State.FADE_IN;

    private final Screen oldScreen;
    private final Screen nextScreen;
    private final GDXRoot game;
    private final Stage stage;
    private boolean fadein = false;
    private boolean fadeout = false;

    private final Image black;
    private final Label label;
    private boolean message = true;

    private float timer = 0f;

    // durations
    private static final float FADE_DURATION = .5f;
    private static final float FADE_OUTDURATION = .4f;
    private static final float HOLD_DURATION = 1.5f;

    public TransitionScreen(Screen oldScreen, Screen nextScreen, GDXRoot game, String text, Boolean fadein, Boolean fadeout) {
        this.oldScreen  = oldScreen;
        this.nextScreen = nextScreen;
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.fadein = fadein;
        this.fadeout = fadeout;

        // create 1×1 black texture
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.BLACK); pm.fill();
        Texture blackTex = new Texture(pm);
        pm.dispose();

        // full-screen black image
        black = new Image(blackTex);
        black.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        black.getColor().a = 0f;
        stage.addActor(black);

        // may use picture instead
        if (text == null) {
            message = false;
        }
        BitmapFont font = new BitmapFont();
        Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
        label = new Label(text, style);
        label.setFontScale(2f);
        label.setPosition(
            (Gdx.graphics.getWidth() - label.getWidth()) / 2f,
            (Gdx.graphics.getHeight() - label.getHeight()) / 2f
        );
        label.getColor().a = 0f;
        stage.addActor(label);
    }

    @Override
    public void render(float delta) {
        delta = 1/60f;
        if (state == State.FADE_IN && !fadein) {
            state = State.HOLD;
        }
        if (state == State.HOLD && !message) {
            state = State.FADE_OUT;
        }
        if (state == State.FADE_OUT && !fadeout) {
            state = State.DONE;
        }
        timer += delta;
        switch (state) {
            case FADE_IN:
                oldScreen.render(delta);
                if (timer == delta) {
                    black.addAction(Actions.fadeIn(FADE_DURATION));
                    label.addAction(Actions.fadeIn(FADE_DURATION));
                }
                if (timer >= FADE_DURATION) {
                    state = State.HOLD;
                    timer = 0f;
                }
                break;

            case HOLD:
                if (timer >= HOLD_DURATION) {
                    state = State.FADE_OUT;
                    timer = 0f;
                }
                break;

            case FADE_OUT:
                nextScreen.render(delta);
                if (timer == delta) {
                    black.addAction(Actions.fadeOut(FADE_OUTDURATION));
                    label.addAction(Actions.fadeOut(FADE_OUTDURATION));
                }
                if (timer >= FADE_OUTDURATION) {
                    state = State.DONE;
                }
                break;

            case DONE:
                if (oldScreen instanceof MainMenuScreen || oldScreen instanceof FailureScene || oldScreen instanceof SuccessScene) {
                    oldScreen.dispose();
                }
                dispose();
                game.setScreen(nextScreen);
                return;
        }

        Gdx.gl.glEnable(GL20.GL_BLEND);
        stage.act(delta);
        stage.draw();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void resize(int w, int h) { stage.getViewport().update(w,h,true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
    @Override
    public void dispose() {
        stage.dispose();
    }
}
