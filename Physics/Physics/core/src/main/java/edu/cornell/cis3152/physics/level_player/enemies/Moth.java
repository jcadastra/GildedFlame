package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.Light;
import edu.cornell.gdiac.assets.AssetDirectory;

public class Moth extends Enemy {
    float DETECTION_DISTANCE = 5;

    RaycastResult rr;

    public Moth(int id, float units, JsonValue value, AssetDirectory directory, Vector2 position) {
        super(id, units, value, directory, position);
    }

    @Override
    public void update() {
    }


    @Override
    public void in_light() {

    }

    @Override
    public void attack() {
        Texture texture = directory.getEntry("rocket-moth03", Texture.class);
        setTexture(texture);
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
            setState(EnemyState.ANGRY);
            resetAttackTimer();
            resetAttackAnimationTimer();
        }
    }

    @Override
    public RaycastResult raycast() {
        Vector2 start = obstacle.getBody().getPosition();
        Vector2 direction;
        if (isFacingRight()) {
            direction = new Vector2(-1, 0);
        } else {
            direction = new Vector2(1, 0);
        }
        float maxDistance = 5f;
        Vector2 end = start.cpy().add(direction.scl(maxDistance));
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture.getBody().getUserData() instanceof Light) {
                    System.out.println("Light detected");
                }
                return fraction;
            }
        };
        World world = obstacle.getBody().getWorld();
        world.rayCast(callback, start, end);
        return null;
    }

    // just follows the torch around.
    @Override
    public void angry() {
        

        if (getAttackTimer() == 0) {
            setState(EnemyState.ATTACK);
        } else {
            stop();
            decrementAttackTimer();
        }
    }

    @Override
    public void out_of_light() {
        Texture texture = directory.getEntry("rocket-moth01", Texture.class);
        setTexture(texture);
        if (rr.targetDistance < DETECTION_DISTANCE && rr.targetObject instanceof Light){
            angry();
        } else {
            move();
        }
    }
}
