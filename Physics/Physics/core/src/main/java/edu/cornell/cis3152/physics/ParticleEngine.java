package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import edu.cornell.cis3152.physics.level_player.FireController;
import edu.cornell.cis3152.physics.level_player.enviromentals.Button;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.gdiac.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Batch;

import java.awt.*;

public class ParticleEngine implements Screen {

    private TextureAtlas particleAtlas;
    private ParticleEffect effect = new ParticleEffect();
    private float physicsUnits;


    /* Handles the torch fire.
    * TODO: modify code structure to allow environmental lights*/
    public ParticleEngine (Fire fire, float physicsUnits){
        this.physicsUnits = physicsUnits;
        effect.load(Gdx.files.internal("platform/flame/particle.p"),Gdx.files.internal("platform/flame/"));
        //effect.set
        effect.start();

        //Setting the position of the ParticleEffect
        effect.setPosition(fire.getObstacle().getX()*physicsUnits, fire.getObstacle().getY()*physicsUnits);
        effect.scaleEffect(1/5f);
        //System.out.println("fire-pos"+fire.getObstacle().getX()+","+fire.getObstacle().getY());

    }

    /*Particle effect for text effects*/
    public ParticleEngine (TextButton button){
        effect.load(Gdx.files.internal("platform/flame/particle.p"),Gdx.files.internal("platform/flame/"));
        //effect.set
        effect.start();
        //Setting the position of the ParticleEffect
        float offesetX = button.getLabel().getX()+button.getLabel().getWidth()/2f;
        float offesetY = button.getLabel().getY()+button.getLabel().getHeight()/2f;
        effect.setPosition(button.getX()+offesetX,button.getY()+offesetY);
        effect.getEmitters().first().getAngle().setHigh(0);
        effect.scaleEffect(1/2f);
        effect.start();
    }

    public void unSelect(TextButton button){
        effect.dispose();
    }

    /*Handles particle effects for new fires (dynamic)*/
    public void newFires(FireController fireController){
        //particleAtlas = new TextureAtlas();
        for (Fire fire: fireController.getLitFires()){
        effect.load(Gdx.files.internal("platform/flame/particle.p"),Gdx.files.internal("platform/flame/"));
        //effect.set
        effect.start();
        //Setting the position of the ParticleEffect
            effect.setPosition(1000,100);
        effect.setPosition(fire.getObstacle().getX()*physicsUnits, fire.getObstacle().getY()*physicsUnits);
        //System.out.println("fire-pos"+fire.getObstacle().getX()*32+","+fire.getObstacle().getY()*32);

        }
        //effect.scaleEffect(1/2f);
        //System.out.println();

    }

    public void draw(SpriteBatch batch){
        effect.draw(batch,Gdx.graphics.getDeltaTime());
    }

    /*
    Draw method for fire
    TODO:update this to use the firecontroller and hide the loop, add torch to firecontroller
    * */
    public void draw(SpriteBatch batch,Fire fire){
        //Updating and Drawing the particle effect
        //Delta being the time to progress the particle effect by, usually you pass in Gdx.graphics.getDeltaTime();
        // Update the particle effect's position to follow the fire's position
        effect.setPosition(fire.getObstacle().getX()*physicsUnits, fire.getObstacle().getY()*physicsUnits);
        effect.update(Gdx.graphics.getDeltaTime());
        effect.draw(batch,Gdx.graphics.getDeltaTime());
    }

    /*
    Draw method for text effects
    TODO:update this to use the firecontroller and hide the loop, add torch to firecontroller
    * */
    public void draw(Batch batch,TextButton button){
        //Updating and Drawing the particle effect
        //Delta being the time to progress the particle effect by, usually you pass in Gdx.graphics.getDeltaTime();
        // Update the particle effect's position to follow the fire's position
        //effect.setPosition(label.getX()*32,label.getY()*32);
        effect.update(Gdx.graphics.getDeltaTime());
        effect.draw(batch,Gdx.graphics.getDeltaTime());
    }



    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
