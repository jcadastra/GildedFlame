package edu.cornell.cis3152.physics.platform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Totem extends Enemy {
    public Totem(int id, Vector2 pos, Body body) {
        super(id, pos, body);
    }

    // reacting to light
    @Override
    public void in_light(){
        // turns solid
    }



}
