package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;
import edu.cornell.cis3152.physics.level_player.player.Torch;
import edu.cornell.cis3152.physics.level_player.player.Traci;
import edu.cornell.gdiac.assets.AssetDirectory;

public class Moth extends Enemy {
    float DETECTION_DISTANCE = 5;


    public Moth(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position);
        rr = null;
    }

    @Override
    public void in_light() {
        resetAttackTimer();
        if (rr != null){
//            System.out.println("in light: " + rr.targetObject);
            if (rr.targetObject instanceof Torch && ((Torch) rr.targetObject).canBePickedUp()) {
                setState(EnemyState.CREEP);
            } else if (rr.targetObject instanceof Traci) {
                setState(EnemyState.CD);
            } else {
                System.out.println("ERROR: " + rr.targetObject);
            }
        } else {
//            System.out.println("in light");
        }
    }

    @Override
    public void cd() {
//        System.out.println("cd : " + getAttackTimer());
        if (getAttackTimer() == 0) {
            setState(EnemyState.ATTACK);
        } else {
            stop();
            decrementAttackTimer();
        }
    }

    @Override
    public void attack() {
        if (getAttackAnimationTimer() == 0) {
//            System.out.print("attack");
            obstacle.getBody().setType(BodyDef.BodyType.DynamicBody);
            obstacle.setBullet(true);
            if (isFacingRight()) {
                obstacle.getBody().applyForceToCenter(new Vector2(10000, 0), true);
            } else {
                obstacle.getBody().applyForceToCenter(new Vector2(-10000, 0), true);
            }

            obstacle.setBullet(false);
            resetAttackTimer();
            resetAttackAnimationTimer();
            setState(EnemyState.IN_LIGHT);
        } else {
//            System.out.println("attack: " + getAttackAnimationTimer());
            stop();
            decrementAttackAnimationTimer();
        }
    }

    // just follows the torch around.
    @Override
    public void angry() {
//        if (rr != null){
//            System.out.println("angry: " + rr.targetObject);
//        } else {
//            System.out.println("angry");
//        }
        setSpeed(3.0f);
        Texture texture = directory.getEntry("platform-moth02", Texture.class);
        setTexture(texture);
        move();

    }

    @Override
    public void out_of_light() {
//        System.out.println("out_of_light");
        Texture texture = directory.getEntry("platform-moth01", Texture.class);
        setTexture(texture);
        setSpeed(2.0f);
        if (!(rr == null) && (!Float.isNaN(rr.targetDistance))) {
//            System.out.println("TARGET: " + rr.targetObject);
            if (rr.targetDistance < DETECTION_DISTANCE && rr.targetObject instanceof Lighting) {
                setState(EnemyState.ANGRY);
            }
//            else {
//                System.out.print("MOVE1 ");
//                move();change
//            }
        }
//        System.out.print("MOVE2 ");
        move();
    }

    @Override
    public void creep() {
//        if (rr != null){
//            System.out.println("creep: "+ rr.targetObject);
//        } else {
//            System.out.println("creep");
//        }
        setSpeed(0.5f);
        move();
    }


}

