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
        super(new BoxObstacle(
            centerInBottomLeft ? x + width / 2f : x,
            centerInBottomLeft ? y + height / 2f : y,
            width,
            height
        ));

        getObstacle().setDensity(0.5f);
        getObstacle().setFriction(0.5f);
        getObstacle().setRestitution(0.1f);
        getObstacle().setPhysicsUnits(units);
        getObstacle().setFixedRotation(true);
        mesh.set(
            -(width  * units)/2f,
            -(height * units)/2f,
            width  * units,
            height * units
        );
    }

    public GameObject(float x, float y, float width, float height, float units, Boolean centerInBottomLeft, int tileId) {
        super();

        String name;
        if (tileId - 5 <= 0 || tileId == 7 || tileId == 12 || tileId == 13 ||
            tileId == 14 || tileId == 15 || tileId == 29 || tileId == 31) {
            name = "platform";
        } else {
            name = "wall";
        }

        if (centerInBottomLeft) {
            x += width / 2;
            y += height / 2;
        }

        float physHeight = height * (name.equals("platform") ? 0.85f : 1f);
        float physCentreY = y + (physHeight - height) / 2f;

        int direction = -1;
        float widthFactor = 1f;
        //shrink to 80%, centered
        if (tileId == 0 || tileId == 12) {
//            widthFactor = .8f;
        }
        // shrink to 90%, right justified
        else if (tileId == 1 || tileId == 4 || tileId == 13) {
//            widthFactor = .9f;
        }
        // shrink to 90%, left justified
        else if (tileId == 3 || tileId == 5 || tileId == 15) {
//            widthFactor = .9f;
//            direction = 1;
        }
        float physCentreX = x - (direction* (width - width * widthFactor)) / 2f;

        BoxObstacle temp = new BoxObstacle(physCentreX,physCentreY,width * widthFactor,physHeight);

        obstacle = new ObstacleSprite(temp).getObstacle();
        obstacle.setDensity(0.5f);
        obstacle.setFriction(0.5f);
        obstacle.setRestitution(.1f);
        obstacle.setPhysicsUnits(units);
        obstacle.setFixedRotation(true);
        obstacle.setName(name);
        //if platform ie walk on shrink height by .15 to walk on better
        mesh.set(-(width * units)/2, -(height * (name.equals("platform") ? .85f : 1) * units)/2, width * widthFactor * units, (height ) * units);
    }
}
