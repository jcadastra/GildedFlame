package edu.cornell.cis3152.physics.platform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Totem extends Enemy {
    private Fixture fixture;
    public Totem(int id, Vector2 pos, Body body, float width, float height, SpriteBatch batch) {
        super(id, pos, body, width, height, batch);
        this.fixture = body.getFixtureList().first();
    }

    // reacting to light
    @Override
    public void in_light(){
        stop();
        fixture.setSensor(false); // yes collisions
    }

    @Override
    public void out_of_light(){
        fixture.setSensor(true); // no collisions
    }



}
