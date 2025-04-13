package edu.cornell.cis3152.physics.level_player.player;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.EnhancedObstacleSprite;
import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.physics2.*;

public class Torch extends EnhancedObstacleSprite {

    /** The light that the torch emits */
    private Lighting internal_light;
    private Lighting lightJoint;
    private Lighting fireJoint;

    /** Json file to avoid magic numbers */
    private final JsonValue data;

    /** width of torch */
    private final float width;

    /** height of torch */
    private final float height;
    public float getHeight() {
        return height;
    }

    /** timer to avoid too early double pickup of torch (can pickup when 0*/
    private int pickUpTimer;
    //small vector used get hte intial velocity of the torch if thrown
    private Vector2 intialThrowVelocity = new Vector2();

    public Vector2 getIntialThrowVelocity() {
        return intialThrowVelocity;
    }

    public boolean canBePickedUp() {return pickUpTimer == 0;}
    public void resetPickUp() {pickUpTimer = data.getInt("pickupTimer");}

    /**
     * Torch constructor, takes in the physics units and the json source
     * inits a torch that can be thrown and has a light attached to it
     *
     * @param units physics units the world is defined in
     * @param data json file location
     */
    public Torch(float units, JsonValue data) {
        this.data = data;
        JsonValue debugInfo = data.get("debug");

        float x = data.get("pos").getFloat(0);
        float y = data.get("pos").getFloat(1);
        float s = data.getFloat( "size" );
        float size = s*units;

        // The capsule is smaller than the image
        // "inner" is the fraction of the original size for the capsule
        width = s*data.get("dimensions").getFloat(0);
        height = s*data.get("dimensions").getFloat(1);

        obstacle = new CapsuleObstacle(x,y,width, height);
//        ((CapsuleObstacle)obstacle).setTolerance( debugInfo.getFloat("tolerance", 0.5f) );

        obstacle.setDensity( data.getFloat( "density", 0 ) );
        System.out.println("-->"+obstacle.getDensity());
        obstacle.setFriction( data.getFloat( "friction", 0 ) );
        obstacle.setRestitution( data.getFloat( "restitution", 0 ) );
//        obstacle.setFixedRotation(true);
        obstacle.setPhysicsUnits( units );
        obstacle.setUserData( this );
        obstacle.setName("torch");
        obstacle.setBodyType(BodyType.DynamicBody);

        debug = ParserUtils.parseColor( debugInfo.get("avatar"),  Color.WHITE);

        // Create a rectangular mesh for Traci. This is the same as for door,
        // since Traci is a rectangular image. But note that the capsule is
        // actually smaller than the image, making a tighter hitbox. You can
        // see this when you enable debug mode.
        mesh.set(-size/2.0f,-size/2.0f,size,size);
    }

    /**
     * Applies the force to the torch upon thrown
     *
     * This method should be called after the force attribute is set.
     */
    public void applyThrowForce(int direc) {
        if (!obstacle.isActive()) {
            return;
        }
        obstacle.setLinearVelocity(Vector2.Zero);
        Body body = obstacle.getBody();
        Vector2 appliedForce = getThrowForce(direc);
        body.applyLinearImpulse(appliedForce,obstacle.getPosition(),true);
        body.applyAngularImpulse(data.getFloat("angular_force") * direc,true);
    }

    public Vector2 getThrowForce(int direc) {
        return new Vector2(data.get( "tossForce").getFloat(0) * direc, data.get( "tossForce").getFloat(1));
    }

    /**
     * In this case, only used to tick down the timer on the torck
     */
    public void update() {
        if (pickUpTimer != 0) {
            pickUpTimer--;
        }
        if (intialThrowVelocity.equals(Vector2.Zero)) {
            obstacle.getBody().applyLinearImpulse(getThrowForce(1),obstacle.getPosition(),true);
            intialThrowVelocity = obstacle.getLinearVelocity();
            obstacle.getBody().setLinearVelocity(Vector2.Zero);
        }
    }

    public JointDef attachObj(ObstacleSprite o) {
        WeldJointDef jointDef = new WeldJointDef();
        Vector2 anchor = new Vector2(obstacle.getX(), obstacle.getY());
        jointDef.initialize(obstacle.getBody(), o.getObstacle().getBody(), anchor);
        jointDef.collideConnected = false;
        return jointDef;
    }
}
