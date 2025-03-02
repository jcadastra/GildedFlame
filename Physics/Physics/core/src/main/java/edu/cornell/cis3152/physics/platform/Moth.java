package edu.cornell.cis3152.physics.platform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Moth extends Enemy {

    public Moth(int id, Vector2 pos, Body body) {
        super(id, pos, body);
    }

    // reacting to light
    @Override
    public void react(){

    }


    @Override
    public void attracted(){
//        move_to(AIController.lightPosition)
    }
}
