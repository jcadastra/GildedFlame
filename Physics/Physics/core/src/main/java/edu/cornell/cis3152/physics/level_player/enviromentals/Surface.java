/*
 * Surface.java
 *
 * This class is a ObstacleSprite referencing either a wall or a platform. All
 * it does is override the constructor. We do this for organizational purposes.
 * Otherwise we have to put a lot of initialization code in the scene, and that
 * just makes the scene too long and unreadable.
 *
 * Note that we have similar classes in the other scenes (rocket and ragdoll).
 * We do this because we want to keep each mini-game self-contained.
 *
 * Based on the original PhysicsDemo Lab by Don Holden, 2007
 *
 * Author:  Walker M. White
 * Version: 2/8/2025
 */
 package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.math.Poly2;
import edu.cornell.gdiac.math.PolyTriangulator;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.PolygonObstacle;
import java.util.Arrays;

/**
 * A class representing a tiled surface (wall or platform)
 *
 * An ObstacleSprite is a sprite (specifically a textured mesh) that is
 * connected to a obstacle. It is designed to be the same size as the
 * physics object, and it tracks the physics object, matching its position
 * and angle at all times.
 *
 * This class demonstrates WHY we use meshes, even though we did not use them
 * in earlier labs. For a surface, we do not want to draw a simple rectangular
 * image. This time we want to tile a texture on a polygonal shape. Creating
 * such tiles is something the designers had to do in Lab 2. The nice thing
 * about ObstacleSprite, is that you can have a mesh with the exact same shape
 * as the physics body (adjusted for physics units), and then apply a texture
 * to that shape.
 */
public class Surface extends ObstacleSprite {

    private Vector2 internal_position;

    /**
     * Creates a surface from the given set of points and physics units
     *
     * The points are in box2d space, not drawing space. They will be scaled
     * by the physics units when draw. The points define the outline of the
     * shape. To work correctly, the points must be specified in counterclockwise
     * order, and the line segments may not cross.
     *
     * @param points    The outline of the shape as x,y pairs
     * @param units     The physics units
     * @param data      The physics constants for this rope bridge
     */
    public Surface(float[] points, float units, String name, JsonValue settings) {
        super();

        float tile = units;

        // Save original for visuals
        float[] visualPoints = Arrays.copyOf(points, points.length);

        // Modify for physics
        if (name.equals("platform") || name.equals("floor")) {
            points[1] -= 0.15f;
            points[7] -= 0.15f;
        }

        // Build triangulated mesh using original visual points
        Poly2 poly = new Poly2();
        PolyTriangulator triangulator = new PolyTriangulator();
        triangulator.set(visualPoints);
        triangulator.calculate();
        triangulator.getPolygon(poly);

        float x_avg = 0;
        float y_avg = 0;
        for (int i = 0; i < points.length; i++) {
            if (i % 2 == 0) {
                x_avg += points[i];
            } else {
                y_avg += points[i];
            }
        }
        internal_position = new Vector2(x_avg / (points.length / 2f), y_avg / (points.length / 2f));

        // Use modified points for the physics object
        obstacle = new PolygonObstacle(points);
        obstacle.setBodyType(BodyDef.BodyType.StaticBody);
        obstacle.setDensity(settings.getFloat("density", 0));
        obstacle.setFriction(settings.getFloat("friction", 0));
        obstacle.setRestitution(settings.getFloat("restitution", 0));
        obstacle.setPhysicsUnits(units);
        obstacle.setUserData(this);
        obstacle.setName(name);

        debug = ParserUtils.parseColor(settings.get("debug"), Color.YELLOW);

        // Scale visual mesh properly (without overcompensation)
        Poly2 visualPoly = new Poly2();
        PolyTriangulator visTri = new PolyTriangulator();
        visTri.set(visualPoints);
        visTri.calculate();
        visTri.getPolygon(visualPoly);
        visualPoly.scl(units);
        mesh.set(visualPoly, tile, tile);
    }

    public Vector2 temp_delect_position_remove() {
        return internal_position;
    }
}
