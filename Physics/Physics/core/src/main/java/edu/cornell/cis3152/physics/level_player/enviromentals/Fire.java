package edu.cornell.cis3152.physics.level_player.enviromentals;

import static java.lang.Float.NaN;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Joint;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.Vector;

/**
 * Geneeral burning fire that holds a flame, there is nothing more than the
 * sensor here, all logic is stored in the firecontroller, atm all fire can spread
 *
 * fire must be attached to something else at all times
 */
public class Fire extends ObstacleSprite {

    // raidus of the fire
    private Float radius;
    public int fireID;
    public float getRadius () {
        return radius;
    }

    public Boolean queryPointInside(Vector2 point) {
        return (obstacle.getPosition()).sub(point).len() <= radius;
    }

    // The joint the conencts the fire to the obstacle that it is burning
    private Joint fixtureJoint;
    public Joint getFixtureJoint() {return fixtureJoint;}
    public void setFixtureJoint(Joint j) {fixtureJoint = j;}

    public Fire(Float units, Vector2 point) {
        super();
//        this.radius = data.getFloat("radius");
        radius = .8f;
        obstacle = new WheelObstacle(point.x, point.y, radius);
        obstacle.setPosition(point);
        obstacle.setUserData( this );
        obstacle.setSensor(true);
        obstacle.setDensity(0.00000001f);
        obstacle.setMass(0.0000001f);
        obstacle.setInertia(0.0000001f);
        obstacle.setPhysicsUnits(units);
        obstacle.setName("fire");
//        obstacle.setBodyType( BodyType.DynamicBody );
        mesh.set( -radius, -radius, 2 * radius, 2 * radius );
        debug = Color.RED;
        obstacle.setGravityScale(0.001f);
    }

    // cleans up the fire
    public void dispose() {
        mesh.clear();
        obstacle.markRemoved(true);
    }

    public void setID(int fireID) {
        this.fireID = fireID;
    }
}
