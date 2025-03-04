package edu.cornell.cis3152.physics.platform;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.graphics.SpriteBatch;

public class Totem extends Enemy {


    public Totem(int id, float units, JsonValue value) {
        super(id, units, value);
        Body body = obstacle.getBody();
        /*Fixture fixture = body.getFixtureList().get(0);
        fixture.getFilterData().categoryBits = TOTEM;
        fixture.getFilterData().maskBits = WALL | TORCH | MOTH;*/
    }

    // reacting to light
    @Override
    public void in_light(){
        decrementFreezeTimer();
        if (getFreezeTimer() != 0){
            stop();
            System.out.println("Frozen: " +  getFreezeTimer());
        } else {
            System.out.println("Unfrozen");
            setState(EnemyState.OUT_OF_LIGHT);
        }

//        getFixture().setSensor(false); // yes collisions
    }

    @Override
    public void out_of_light(){
        move();
        //        getFixture().setSensor(true); // no collisions
    }

    /*public void updateMaskbits() {
        Fixture fixture = obstacle.getBody().getFixtureList().get(0);

        if (state == EnemyState.OUT_OF_LIGHT) {
            fixture.getFilterData().maskBits = WALL | TORCH | MOTH;
        } else {
            fixture.getFilterData().maskBits = PLAYER | WALL | TORCH | MOTH;
        }
    }*/

    @Override
    public void update() {
        super.update();
        //updateMaskbits();
    }

    public void create_Fixture() {
        if (obstacle.getBody() != null) {
            FixtureDef fd = new FixtureDef();
            fd.filter.categoryBits = TOTEM;
            fd.filter.maskBits = WALL | TORCH | MOTH;
            PolygonShape fixShape = new PolygonShape();
            fixShape.setAsBox(width, height, new Vector2(getX(), getY()), 0.0f);
            fd.shape = fixShape;
            obstacle.getBody().createFixture(fd);
        } else {
            System.out.println("Error: Body is null in createFixture()");
        }
    }
}

