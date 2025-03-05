package edu.cornell.cis3152.physics.platform;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;

public class Totem extends Enemy {

    private int MOVE_SPEED;
    public Totem(int id, float units, JsonValue value, AssetDirectory directory) {
        super(id, units, value, directory);
        MOVE_SPEED = 3;
    }

    // reacting to light
    @Override
    public void in_light(){
//        System.out.println("INLIGHT");
        Texture texture = directory.getEntry("rocket-totem03", Texture.class);
        setTexture(texture);
        stop();
    }

    @Override
    public void out_of_light(){
//        System.out.println("OUTOFLIGHT");
        Texture texture = directory.getEntry("rocket-totem01", Texture.class);
        setTexture(texture);
        if (getFreezeTimer() == 0){
//            System.out.println("HERE");

            move(MOVE_SPEED);
        } else {
            angry();
        }
    }

    @Override
    public void angry() {
//        System.out.println("ANGRY");
        Texture texture = directory.getEntry("rocket-totem02", Texture.class);
        setTexture(texture);
        decrementFreezeTimer();
    }

}
