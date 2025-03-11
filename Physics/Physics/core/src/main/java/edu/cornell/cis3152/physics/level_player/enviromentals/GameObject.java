package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.gdiac.math.Poly2;
import edu.cornell.gdiac.math.PolyTriangulator;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.PolygonObstacle;

public class GameObject extends ObstacleSprite {
    private boolean flammable;
    private Vector2 internalPosition;

    public boolean getFlammable() {
        return flammable;
    }

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

        // Compute the centroid (average of vertices)
        float xSum = 0, ySum = 0;
        int count = points.length / 2;
        for (int i = 0; i < points.length; i += 2) {
            xSum += points[i];
            ySum += points[i + 1];
        }
        internalPosition = new Vector2(xSum / count, ySum / count);

        // Construct a Poly2 object using a triangulator
        Poly2 poly = new Poly2();
        PolyTriangulator triangulator = new PolyTriangulator();
        triangulator.set(points);
        triangulator.calculate();
        triangulator.getPolygon(poly);

        // Create the polygon obstacle from the same hardcoded vertices.
        // The obstacle is positioned at (x,y) in world space.
        obstacle = new PolygonObstacle(points, x, y);
        flammable = true;
        obstacle.setPosition(x, y);
        obstacle.setDensity(0.5f);
        obstacle.setFriction(0.5f);
        obstacle.setRestitution(.1f);
        obstacle.setPhysicsUnits(units);
        obstacle.setUserData(this);
        obstacle.setFixedRotation(true);

        // Create the mesh from the Poly2.
        // Scale the polygon by the physics units so that drawing is adjusted.
        poly.scl(units);
        // Hardcoded tiling factor; adjust as needed.
        float tile = 1.0f;
        mesh.set(poly, tile, tile);
//        mesh.
    }

    public Vector2 getInternalPosition() {
        return internalPosition;
    }
}
