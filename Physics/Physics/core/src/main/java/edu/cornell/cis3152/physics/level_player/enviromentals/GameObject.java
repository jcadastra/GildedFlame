package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.math.Poly2;
import edu.cornell.gdiac.math.PolyTriangulator;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.PolygonObstacle;
import edu.cornell.gdiac.physics2.WheelObstacle;
import javax.swing.Box;

public class GameObject extends EnhancedObstacleSprite {

    /**
     * Creates a GameObject with a hardcoded shape, generic object that is enhanced
     *
     * @param x      The world x-position.
     * @param y      The world y-position.
     * @param units  The physics unit scale.
     */
    public GameObject(float x, float y, float width, float height, float units, Boolean centerInBottomLeft) {
        super();

        if (centerInBottomLeft) {
            x += width / 2;
            y += height / 2;
        }

        BoxObstacle temp = new BoxObstacle(x,y,width,height);
        obstacle = new ObstacleSprite(temp).getObstacle();
        obstacle.setDensity(0.5f);
        obstacle.setFriction(0.5f);
        obstacle.setRestitution(.1f);
        obstacle.setPhysicsUnits(units);
        obstacle.setFixedRotation(true);
        mesh.set(-(width * units)/2, -(height * units)/2, width * units, height * units);
    }
}
