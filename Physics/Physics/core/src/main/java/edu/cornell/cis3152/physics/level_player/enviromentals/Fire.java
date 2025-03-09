package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.Vector;

public class Fire extends ObstacleSprite {

    private Vector2 position;
    private Vector2 radius;
    private Polygon polygon;

    public Polygon getPolygon() {
        return polygon;
    }

    /**
     * Boolean value if the fire spreads to other sources or stays constant
     */
    private boolean spreads;

    /**
     * General fire usuage, can either spread or not depending on needs
     * If spreads, then also destructive
     *
     * fire must be attached to something else at all times
     */
    public Fire(Vector2 point) {
        super();
//        this.radius = data.getFloat("radius");
        float radius = 1f;
        position = point;
        obstacle = new WheelObstacle(point.x, point.y, radius);
        obstacle.setUserData( this );
        obstacle.setSensor(true);
        obstacle.setName("fire");
        obstacle.setBodyType( BodyType.StaticBody );
        mesh.set( -radius, -radius, 2 * radius, 2 * radius );
        debug = Color.RED;
    }

    public void dispose() {

    }
}

/**
 * TODO:
 *
 * Given a fire, needs to burn
 * so a singular fire needs to have a light source and partcile generator
 *
 * one or spreading?
 * one i think
 * given a polygon, randomly place points within it by triangulation
 *
 * need fire controller for spreading
 *      can have dictionary from body to n distribution
 * fire to generate things
 *
 * for each block on fire not all lit
 *      get point closet to next fire on blcok, light it with material based lighting timer
 *          add contact hangler that if fire git burnable block but is not on fire then add to fire
 *      if all points then set off internal material lighitng timer
 * upon expiration of internal block lighting timer, destroy
 */
