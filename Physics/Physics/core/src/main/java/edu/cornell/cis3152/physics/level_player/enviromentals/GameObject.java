package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.math.Poly2;
import edu.cornell.gdiac.math.PolyTriangulator;
import edu.cornell.gdiac.physics2.PolygonObstacle;
import edu.cornell.gdiac.physics2.WheelObstacle;

public class GameObject extends EnhancedObstacleSprite {

    /**
     * Creates a GameObject with a hardcoded shape.
     *
     * The hardcoded shape is defined by the vertices:
     *  (0,0), (3,0), (3,1), (1,1), (1,5)
     *
     * The centroid is computed for internal bookkeeping.
     * The physics obstacle and mesh are created based on these vertices.
     *
     * @param x      The world x-position.
     * @param y      The world y-position.
     * @param units  The physics unit scale.
     */
    public GameObject(float[] points, float x, float y, float units) {
        super();

        Poly2 poly = new Poly2();
        PolyTriangulator triangulator = new PolyTriangulator();
        triangulator.set(points);
        triangulator.calculate();
        triangulator.getPolygon(poly);

        obstacle = new PolygonObstacle(points, x, y);
        obstacle.setPosition(x, y);
        obstacle.setDensity(0.5f);
        obstacle.setFriction(0.5f);
        obstacle.setRestitution(.1f);
        obstacle.setPhysicsUnits(units);
        obstacle.setUserData(this);
        obstacle.setFixedRotation(true);

        poly.scl(units);
        float tile = 1.0f;
        mesh.set(poly, tile, tile);
    }

}
