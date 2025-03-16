package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.GameplayScene;
import edu.cornell.cis3152.physics.level_player.enviromentals.Light;
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
        System.out.println("in light");
        if (rr.targetObject instanceof Torch && ((Torch) rr.targetObject).canBePickedUp()) {
            setState(EnemyState.CREEP);
        } else if (rr.targetObject instanceof Traci) {
            setState(EnemyState.CD);
        } else {
            System.out.println("ERROR");
        }
    }

    @Override
    public void cd() {
        System.out.println("cd : " + rr.targetObject);
        if (getAttackTimer() == 0) {
            setState(EnemyState.ATTACK);
        } else {
            stop();
            decrementAttackTimer();
        }
    }

    @Override
    public void attack() {
        System.out.println("attack: " + getAttackAnimationTimer());
        if (getAttackAnimationTimer() != 0) {
            obstacle.setBullet(true);
            if (isFacingRight()) {
                obstacle.getBody().applyForceToCenter(new Vector2(-500, 0), true);
            } else {
                obstacle.getBody().applyForceToCenter(new Vector2(500, 0), true);
            }
            obstacle.setBullet(false);
            decrementAttackAnimationTimer();
        } else {
            stop();
            resetAttackTimer();
            resetAttackAnimationTimer();
            setState(EnemyState.IN_LIGHT);
        }
    }

    // just follows the torch around.
    @Override
    public void angry() {
        System.out.println("angry: " + rr.targetObject);
        Texture texture = directory.getEntry("platform-moth02", Texture.class);
        setTexture(texture);
    }

    @Override
    public void out_of_light() {
        System.out.println("out_of_light");
        Texture texture = directory.getEntry("platform-moth01", Texture.class);
        setTexture(texture);
        if (!(rr == null) && (!Float.isNaN(rr.targetDistance))) {
            System.out.println("TARGET: " + rr.targetObject.toString());
            if (rr.targetDistance < DETECTION_DISTANCE && rr.targetObject instanceof Light) {
                setState(EnemyState.ANGRY);
            } else {
                move();
            }
        }
    }

    @Override
    public void creep() {
        System.out.println("creep: " + rr.targetObject);
        setSpeed(2);
//        move();

    }


}

