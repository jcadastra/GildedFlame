package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Actor;
import edu.cornell.cis3152.physics.level_player.FireController;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.gdiac.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Batch;
import java.util.ArrayList;
import java.util.List;

/**
 * ParticleEngine handles particle effects for fires and UI buttons.
 */
public class ParticleEngine implements Screen {

    private List<ParticleEffect> fireEffects = new ArrayList<>();
    private ParticleEffect uiEffect;
    private float physicsUnits;

    /**
     * Constructor for environmental fires.
     * @param fire The fire instance to attach particles to.
     * @param physicsUnits Scaling factor from game units to pixels.
     */
    public ParticleEngine(Fire fire, float physicsUnits) {
        this.physicsUnits = physicsUnits;
        uiEffect = null;
        ParticleEffect effect = createEffect();
        float x = fire.getObstacle().getX() * physicsUnits;
        float y = fire.getObstacle().getY() * physicsUnits;
        effect.setPosition(x, y);
        effect.scaleEffect(1 / 5f);
        fireEffects.add(effect);
    }

    /**
     * Constructor for UI elements (e.g., buttons).
     * @param actor Any Actor (TextButton, ImageButton, etc.) to attach particles to.
     */
    public ParticleEngine(Actor actor) {
        this.physicsUnits = 1f;
        uiEffect = createEffect();
        float x = actor.getX() + actor.getWidth() / 2f;
        float y = actor.getY() + actor.getHeight() / 2f;
        uiEffect.setPosition(x, y);
        uiEffect.getEmitters().first().getAngle().setHigh(0);
        uiEffect.scaleEffect(1 / 2f);
    }

    /**
     * Spawns or updates particle effects for lit fires.
     * Effects are stored and drawn in draw methods.
     * @param fireController Controller providing lit fires.
     */
    public void newFires(FireController fireController) {
        fireEffects.clear();
        for (Fire fire : fireController.getLitFires()) {
            ParticleEffect effect = createEffect();
            float x = fire.getObstacle().getX() * physicsUnits;
            float y = fire.getObstacle().getY() * physicsUnits;
            effect.setPosition(x, y);
            effect.scaleEffect(1 / 5f);
            fireEffects.add(effect);
        }
    }

    /**
     * Helper to load and start a new ParticleEffect.
     */
    private ParticleEffect createEffect() {
        ParticleEffect effect = new ParticleEffect();
        effect.load(
            Gdx.files.internal("platform/flame/particle.p"),
            Gdx.files.internal("platform/flame/")
        );
        effect.start();
        return effect;
    }

    /**
     * Draw all fire effects.
     */
    public void draw(SpriteBatch batch) {
        float delta = Gdx.graphics.getDeltaTime();
        for (ParticleEffect effect : fireEffects) {
            effect.update(delta);
            effect.draw(batch, delta);
        }
    }

    /**
     * Draw a specific fire's effect.
     */
    public void draw(SpriteBatch batch, Fire fire) {
        float delta = Gdx.graphics.getDeltaTime();
        for (ParticleEffect effect : fireEffects) {
            // assume one-to-one mapping in same order
            effect.update(delta);
            effect.draw(batch, delta);
        }
    }

    /**
     * Draw the UI effect attached to an actor.
     */
    public void draw(Batch batch, Actor actor) {
        if (uiEffect == null) return;
        float x = actor.getX() + actor.getWidth() / 2f;
        float y = actor.getY() + actor.getHeight() / 2f;
        uiEffect.setPosition(x, y);
        float delta = Gdx.graphics.getDeltaTime();
        uiEffect.update(delta);
        uiEffect.draw(batch, delta);
    }

    /**
     * Dispose all effects.
     */
    public void unSelect() {
        if (uiEffect != null) {
            uiEffect.dispose();
            uiEffect = null;
        }
        for (ParticleEffect effect : fireEffects) {
            effect.dispose();
        }
        fireEffects.clear();
    }

    // Screen interface methods (no-op)
    @Override public void show() {}
    @Override public void render(float delta) {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { unSelect(); }
}
