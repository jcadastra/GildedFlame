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
        decrementFreezeTimer();
        if (getFreezeTimer() != 0){
            stop();
//            System.out.println("Frozen: " +  getFreezeTimer());
        } else {
//            System.out.println("Unfrozen");
            setState(EnemyState.OUT_OF_LIGHT);
        }
        // getFixture().setSensor(false); // yes collisions
    }

    @Override
    public void out_of_light(){
        move();
        //        getFixture().setSensor(true); // no collisions
    }

}
