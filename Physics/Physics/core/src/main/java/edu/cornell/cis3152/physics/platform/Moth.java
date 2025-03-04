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
//        System.out.println("In light");
        setState(EnemyState.ANGRY);
    }

    @Override
    public void attack(){
//        System.out.println("Attack");
        setMoveSpeed(150);
        move();
        resetMoveSpeed();
        setState(EnemyState.OUT_OF_LIGHT);
    }

    @Override
    public void angry(){
//        System.out.println("Angry: " + getAttackTimer());
        if (getAttackTimer() == 0){
            setState(EnemyState.ATTACK);
        } else {
            stop();
//            System.out.println("Waiting to attack");
            decrementAttackTimer();
        }
    }

    @Override
    public void out_of_light(){
//        System.out.println("Out of light");
        move();
    }
}
