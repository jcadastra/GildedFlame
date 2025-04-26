package edu.cornell.cis3152.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import edu.cornell.cis3152.physics.level_player.FireController;
import edu.cornell.cis3152.physics.level_player.enviromentals.Button;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.gdiac.graphics.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;


public class ParticleEngine implements Screen {

    private TextureAtlas particleAtlas;
    private ParticleEffect effect = new ParticleEffect();
    private float physicsUnits;

    private ParticleEffect rainEffect = new ParticleEffect();

    // splash pool
    Pool<ParticleEffect> splashPool = new Pool<ParticleEffect>(5,20) {
        @Override
        protected ParticleEffect newObject() {
            ParticleEffect effect = new ParticleEffect();
            effect.load(Gdx.files.internal("platform/particles/splash.p"), Gdx.files.internal("platform/particles/"));
            return effect;
        }
    };

    private Array<ParticleEffect> splashEffects;


    /* Handles the torch fire.
    * TODO: modify code structure to allow environmental lights*/

    public ParticleEngine (Fire fire, float physicsUnits){
        this.physicsUnits = physicsUnits;
        effect.load(Gdx.files.internal("platform/particles/flame.p"),Gdx.files.internal("platform/particles/"));
        //effect.set
        effect.start();

        //Setting the position of the ParticleEffect
        effect.setPosition(fire.getObstacle().getX()*physicsUnits, fire.getObstacle().getY()*physicsUnits);
        effect.scaleEffect(1/5f * physicsUnits/32);
        //System.out.println("fire-pos"+fire.getObstacle().getX()+","+fire.getObstacle().getY());

    }

    public ParticleEngine(){}

    public void rainEffect(Rectangle bounds){
        rainEffect.load(Gdx.files.internal("platform/particles/rain.p"),Gdx.files.internal("platform/particles/"));
        rainEffect.start();
        rainEffect.scaleEffect(0.5f);
        rainEffect.setPosition(0,bounds.height );
    }

    /*Particle effect for text effects*/
    public ParticleEngine (TextButton button){
        effect.load(Gdx.files.internal("platform/particles/flame.p"),Gdx.files.internal("platform/particles/"));
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
            effect.load(Gdx.files.internal("platform/particles/flame.p"),Gdx.files.internal("platform/particles/"));
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

    public void splashEffects(float x,float y,float width,float height){
        float h = 2f;
        int k = (int) width/(int)h;
        splashEffects = new Array<>(k);
        for (int i = 0; i<k; i++){
            ParticleEffect splash = new ParticleEffect();
            splash.load(Gdx.files.internal("platform/particles/splash.p"), Gdx.files.internal("platform/particles/"));;
            // Set the splash position
            float splashX = (x + (i * h))*32;  // Spread the splashes across the width
            float splashY = y*32;            // Position on the Y-axis (adjust if needed)
            splash.setPosition(splashX, splashY);
            splash.start();
            splash.scaleEffect(10f);
            splashEffects.add(splash);
        }

    }

    public void drawSplash(Batch batch, float x,float y,float width,float height){
        if (splashEffects!=null){
            float h = 1f;
            int k = splashEffects.size;
            System.out.println(splashEffects.size);
            //splashEffects = new Array<>(k);
            for (int i = 0; i<k; i++){
                ParticleEffect splash = splashEffects.get(i);
                float splashX = (x + (i * h))*32;  // Spread the splashes across the width
                float splashY = y*32;            // Position on the Y-axis (adjust if needed)
                splash.setPosition(splashX, splashY);
                System.out.println("Splash at: " + splashX + ", " + splashY);
                splash.update(Gdx.graphics.getDeltaTime());
                splash.draw(batch);
            // If the splash effect is complete
//            if (splash.isComplete()) {
//                splashPool.free(splash); // Return the splash effect to the pool
//            }
        }
        //splashEffects.clear();
    }}
    public void drawRain (SpriteBatch batch,Rectangle bounds){
        rainEffect.setPosition(32,bounds.height*32 );
        rainEffect.update(Gdx.graphics.getDeltaTime());
        rainEffect.draw(batch);
        //drawSplash(batch,Gdx.graphics.getDeltaTime());
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
