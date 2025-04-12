package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.gdiac.physics2.WheelObstacle;

public class Coin extends EnhancedObstacleSprite {
    private boolean collected;

    /**
     * Creates a coin object that acts as a collectible sensor.
     *
     * @param x      X-position in world coordinates
     * @param y      Y-position in world coordinates
     * @param radius Radius of the coin
     * @param units  Physics units scaling factor
     */
    public Coin(float x, float y, float radius, float units) {
        super();
        collected = false;

        // Use WheelObstacle for circular shape
        obstacle = new WheelObstacle(radius);
        obstacle.setPosition(x, y);
        obstacle.setBodyType(BodyType.StaticBody); // Static since it doesn't move
        obstacle.setSensor(true); // No physical collision
        obstacle.setName("coin"); // Identifier for collisions
        obstacle.setPhysicsUnits(units);
        obstacle.setUserData(this); // Link obstacle to this sprite

        // Optional: Set restitution for bounciness if desired
        obstacle.setRestitution(0.3f);
    }

    /** Marks the coin as collected and queues it for removal */
    public void collect() {
        collected = true;
        obstacle.markRemoved(true);
    }

    public boolean isCollected() {
        return collected;
    }
}
