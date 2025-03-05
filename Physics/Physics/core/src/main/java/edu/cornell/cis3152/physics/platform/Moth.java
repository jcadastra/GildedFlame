package edu.cornell.cis3152.physics.platform;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;

public class Moth extends Enemy {

    private int MOVE_SPEED;
    public Moth(int id, float units, JsonValue value, AssetDirectory directory) {
        super(id, units, value, directory);
        MOVE_SPEED = 4;
    }

    @Override
    public void in_light(){
        Texture texture = directory.getEntry("rocket-moth03", Texture.class);
        setTexture(texture);
//        System.out.println("In light");
        setState(EnemyState.ANGRY);
    }

    @Override
    public void attack(){
        if (getAttackAnimationTimer() != 0) {
            obstacle.setBullet(true);
            if (isFacingRight()){
                obstacle.getBody().applyForceToCenter(new Vector2(-500, 0), true);
            } else {
                obstacle.getBody().applyForceToCenter(new Vector2(500, 0), true);
            }
            obstacle.setBullet(false);
            decrementAttackAnimationTimer();
        } else {
            stop();
            setState(EnemyState.ANGRY);
            resetAttackTimer();
            resetAttackAnimationTimer();
        }
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
        Texture texture = directory.getEntry("rocket-moth01", Texture.class);
        setTexture(texture);
//        System.out.println("Out of light");
        move(MOVE_SPEED);
    }
}
