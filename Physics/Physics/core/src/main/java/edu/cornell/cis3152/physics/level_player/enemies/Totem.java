package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;

public class Totem extends Enemy {

    boolean isGrounded;

    public Totem(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position);
        setJustCollided(false);
    }


    // reacting to light
    @Override
    public void in_light(){
//        System.out.println("INLIGHT");
        Texture texture = directory.getEntry("platform-totem02", Texture.class);
        setTexture(texture);
        stop();
    }

    @Override
    public void out_of_light(){
//        System.out.println("OUTOFLIGHT");
        Texture texture = directory.getEntry("platform-totem01", Texture.class);
        setTexture(texture);
        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
        move();
    }

    @Override
    public void cd() {
        System.out.println("cd");
        Texture texture = directory.getEntry("platform-totem02", Texture.class);
        setTexture(texture);
        if (getFreezeTimer() == 0) {
            System.out.println("RAHH");
            out_of_light();
        } else {
            decrementFreezeTimer();
        }
    }


}
