package edu.cornell.cis3152.physics.platform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.graphics.SpriteBatch;

public class Totem extends Enemy {


    public Totem(int id, float units, JsonValue value) {
        super(id, units, value);
    }

    // reacting to light
    @Override
    public void in_light(){
        stop();
    }

    @Override
    public void out_of_light(){
        obstacle.setFriction(0);
        if (getFreezeTimer() == 0){
            move();
        } else {
            decrementFreezeTimer();
        }
    }

}
