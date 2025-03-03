package edu.cornell.cis3152.physics.platform;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.graphics.SpriteBatch;

public class Moth extends Enemy {


    public Moth(int id, float units, JsonValue value) {
        super(id, units, value);
    }

    @Override
    public void in_light(){
        // turns angry - maybe just represent this with a different sprite color
//        spriteColor = Color.RED;
        draw(batch); // change sprite color
    }

    @Override
    public void attracted(){
        move_to(AIController.playerPosition);
    }

    @Override
    public void out_of_light(){
        move();
        //        getFixture().setSensor(true); // no collisions
    }
}
