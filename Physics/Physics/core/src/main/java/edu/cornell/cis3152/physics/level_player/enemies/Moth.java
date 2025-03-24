package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
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
        Texture texture = directory.getEntry("platform-mothINLIGHT", Texture.class);
        setTexture(texture);
        if (rr != null) {
//            System.out.println("in light: " + rr.targetObject);
            if (rr.targetObject instanceof Torch && ((Torch) rr.targetObject).canBePickedUp()) {
                setState(EnemyState.TRANCE);
            } else if (rr.targetObject instanceof Traci) {
                setState(EnemyState.CD);
            }
        } else {
//            System.out.println("in light: NULL target");
        }

    }

    @Override
    public void cd() {
//        System.out.println("cd : " + getAttackTimer());
        Texture texture = directory.getEntry("platform-mothCD", Texture.class);
        setTexture(texture);
        if (getAttackTimer() == 0) {
            setState(EnemyState.ATTACK);
        } else {
            stop();
            decrementAttackTimer();
        }
    }

    @Override
    public void attack() {
        Texture texture = directory.getEntry("platform-mothATTACK", Texture.class);
        setTexture(texture);
        if (getAttackAnimationTimer() == 0) {
//            System.out.print("attack");
            obstacle.getBody().setType(BodyDef.BodyType.DynamicBody);
            obstacle.setBullet(true);
            if (isFacingRight()) {
                obstacle.getBody().applyForceToCenter(new Vector2(15000, 0), true);
            } else {
                obstacle.getBody().applyForceToCenter(new Vector2(-15000, 0), true);
            }

            obstacle.setBullet(false);
            resetAttackTimer();
            resetAttackAnimationTimer();
            setState(EnemyState.OUT_OF_LIGHT);
        } else {
            stop();
            decrementAttackAnimationTimer();
        }
    }

    // just follows the torch around.
    @Override
    public void angry() {
        if (rr != null){
//            System.out.println("angry: " + rr.targetObject);
        } else {
            resetAttackTimer();
            resetAttackAnimationTimer();
            setState(EnemyState.OUT_OF_LIGHT);
//            System.out.println("angry");
        }
        setSpeed(3.0f);
        Texture texture = directory.getEntry("platform-mothANGRY", Texture.class);
        setTexture(texture);
        move();

    }

    @Override
    public void out_of_light() {
//        System.out.println("out of light");
        Texture texture = directory.getEntry("platform-mothOUTOFLIGHT", Texture.class);
        setTexture(texture);
        setSpeed(2.0f);
        if (!(rr == null) && (!Float.isNaN(rr.targetDistance))) {
            if (rr.targetDistance < DETECTION_DISTANCE && rr.targetObject instanceof Lighting) {
                setState(EnemyState.ANGRY);
            } else {
                setState(EnemyState.OUT_OF_LIGHT);
            }
        }
        move();
    }

    @Override
    public void trance() {

        Texture texture = directory.getEntry("platform-mothTRANCE", Texture.class);
        setTexture(texture);
//        System.out.println("trance");
        setSpeed(1f);
        move();
    }

    @Override
    public void smother() {

        // maybe have to put some function here to exactly pinpoint right on top of the torch
//        System.out.println("smother: " + getSmotherTimer());
        if (getSmotherTimer() == 0){
            System.out.println("Game Over");
        } else{
            if (rr == null){
//                System.out.println("ERROR SMOTHER RR IS NULL");
//                stop();
            } else {
                if (rr.targetObject instanceof Torch){
                    Vector2 torchPos = ((Torch) rr.targetObject).getObstacle().getPosition();
//                    System.out.print("Moth loc: " + getObstacle().getX() + " | Torch location: " + torchPos.x);
                    if (Math.abs(getObstacle().getX()-torchPos.x) < 0.5){
//                        System.out.println("HERE");
                        stop();
                    } else {
                        move_to(((Torch) rr.targetObject).getObstacle().getPosition());
                    }
                } else {
                    System.out.println(rr.targetObject);
                }
            }
            decrementSmotherTimer();
        }

    }

    @Override
    public void dazed(){
        Texture texture = directory.getEntry("platform-mothDAZED", Texture.class);
        setTexture(texture);
//        System.out.println("dazed: "+ getDazedTimer());
        if (getDazedTimer() == 0){
            setState(EnemyState.OUT_OF_LIGHT);
        } else {
            stop();
            decrementDazedTimer();
        }
    }


}

