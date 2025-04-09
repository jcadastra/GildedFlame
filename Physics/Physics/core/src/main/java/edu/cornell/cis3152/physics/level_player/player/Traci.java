/*
 * Traci.java
 *
 * This is the class for Traci Nathans-Kelly cartoon avatar. WHile it is also
 * an ObstacleSprite, this class is much more than an organizational tool. This
 * class has all sorts of logic, like the whether Traci can jump or whether
 * Traci can fire a bullet.
 *
 * You SHOULD NOT need to modify this file. However, you may learn valuable
 * lessons for the rest of the lab by looking at it.
 *
 * Based on the original PhysicsDemo Lab by Don Holden, 2007
 *
 * Author:  Walker M. White
 * Version: 2/8/2025
 */
package edu.cornell.cis3152.physics.level_player.player;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Predicate;
import edu.cornell.cis3152.physics.level_player.enemies.Enemy;
import edu.cornell.cis3152.physics.level_player.enviromentals.EnhancedObstacleSprite;
import edu.cornell.cis3152.physics.level_player.enviromentals.Ladder;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.Texture2D;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.physics2.*;
import edu.cornell.gdiac.util.RandomGenerator;
import java.nio.file.StandardOpenOption;
import java.rmi.MarshalException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Traci's avatar for the platform game.
 *
 * An ObstacleSprite is a sprite (specifically a textured mesh) that is
 * connected to a obstacle. It is designed to be the same size as the
 * physics object, and it tracks the physics object, matching its position
 * and angle at all times.
 *
 * Note that unlike a traditional ObstacleSprite, this attaches some additional
 * information to the obstacle. In particular, we add a sensor fixture. This
 * sensor is used to prevent double-jumping. However, we only have one mesh,
 * the mesh for Traci. The sensor is invisible and only shows up in debug mode.
 * While we could have made the fixture a separate obstacle, we want it to be a
 * simple fixture so that we can attach it to the obstacle WITHOUT using joints.
 */
public class Traci extends ObstacleSprite {
    public enum GroundState {
        GROUNDED,
        AIRBORNE,
        CLIMBING;
    }
    private GroundState groundState;
    private Set<EnhancedObstacleSprite> bodyTouchedClimbables;
    public static final int PLAYER = 0x00000001;
    public static final int WALL = 0x00000002;
    public static final int TORCH = 0x00000004;
    public static final int TOTEM = 0x00000008;
    public static final int MOTH = 0x00000010;
    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;
    /** The width of Traci's avatar */
    private float width;
    /** The height of Traci's avatar */
    private float height;

    /** The factor to multiply by the input */
    private float force;
    /** The amount to slow the character down */
    private float damping;
    /** The maximum character speed */
    private float maxspeed;
    /** The impulse for the character jump */
    private float jump_force;
    /** Cooldown (in animation frames) for jumping */
    private int jumpLimit;
    /** Cooldown (in animation frames) for shooting */
    private int shotLimit;

    /** The current horizontal movement of the character */
    private Vector2   movement;
    /** Which direction is the character facing */
    private boolean faceRight;
    /** How long until we can jump again */
    private int jumpCooldown;
    /** Whether we are actively jumping */
    private boolean isJumping;
    /** How long until we can shoot again */
    private int shootCooldown;
    /** Whether we are actively shooting */
    private boolean isShooting;

    /** Whether the player has torch in hand */
    private boolean hasTorch;
    /** The outline of the sensor obstacle */
    private Path2 sensorOutline;
    /** The debug color for the sensor */
    private Color sensorColor;
    /** The name of the sensor fixture */
    private String sensorName;

    private float x;
    private float y;

    private final float units;

    public float getUnits() {
        return units;
    }


    public static final short CATEGORY_AVATAR = 0x0002;  // 00000010
    public static final short CATEGORY_ENVIRONMENT = 0x0004;  // 00000100
    public static final short CATEGORY_LIGHT = 0x0008;  // 00001000

    /** Cache for internal force calculations */
    private final Vector2 forceCache = new Vector2();
    /** Cache for the affine flip */
    private final Affine2 flipCache = new Affine2();

    protected static AssetDirectory directory;
    private static Texture animationTextureIdleTorch;
    private static Texture animationTextureIdleNoTorch;
    private static Texture animationTextureMovementTorch;
    private static Texture animationTextureMovementNoTorch;
    public static final int TOTAL_FRAMES = 6;

    public static final int IDLE_FRAME_HEIGHT = 550;
    public static final int IDLE_FRAME_WIDTH = 300;


    public static final int MOVEMENT_FRAME_HEIGHT = 550;
    public static final int MOVEMENT_FRAME_WIDTH = 350;

    private static final int FRAME_DURATION = 12;

    private int cdFrameCount = 0;
    private int frameIndex = 0;

    private void resetFrames() {
        cdFrameCount = 0;
        frameIndex = 0;
    }

    /**
     * Returns the left/right movement of this character.
     *
     * This is the result of input times force.
     *
     * @return the left/right movement of this character.
     */
    public Vector2 getMovement() {
        return movement;
    }

    /**
     * Sets the left/right movement of this character.
     *
     * This is the result of input times force.
     *
     * @param value the left/right movement of this character.
     */
    public void setMovement(Vector2 value) {
        movement = value;
        // Change facing if appropriate
        if (movement.x < 0) {
            faceRight = false;
        } else if (movement.x > 0) {
            faceRight = true;
        }
    }

    /**
     * Returns true if Traci is actively firing.
     *
     * @return true if Traci is actively firing.
     */
    public boolean isShooting() {
        return isShooting && shootCooldown <= 0;
    }

    /**
     * Sets whether Traci is actively firing.
     *
     * @param value whether Traci is actively firing.
     */
    public void setShooting(boolean value) {
        isShooting = value;
    }

    /**
     * Returns true if Traci is actively jumping.
     *
     * @return true if Traci is actively jumping.
     */
    public boolean isJumping() {
        return isJumping && !groundState.equals(GroundState.AIRBORNE) && jumpCooldown <= 0;
    }

    /**
     * Sets whether Traci is actively jumping.
     *
     * @param value whether Traci is actively jumping.
     */
    public void setJumping(boolean value) {
        isJumping = value;
    }
    /**
     * Returns true if Traci has torch.
     */
    public boolean getHasTorch() {
        return hasTorch;
    }

    public Vector2 getLocation() {
        return new Vector2(getObstacle().getX(), getObstacle().getY());
    }

    /**
     * Sets whether Traci has torch.
     */
    public void setHasTorch(boolean value) {
        hasTorch = value;
    }

    /**
     * Returns true if Traci is on the ground.
     *
     * @return true if Traci is on the ground.
     */
    public GroundState getGroundedState() {
        return groundState;
    }

    /**
     * Sets whether Traci is on the ground.
     */
    public void setGroundedState(GroundState state) {
        groundState = state;
    }

    /**
     * Returns how much force to apply to get Traci moving
     *
     * Multiply this by the input to get the movement value.
     *
     * @return how much force to apply to get Traci moving
     */
    public float getForce() {
        return force;
    }

    /**
     * Returns how hard the brakes are applied to stop Traci moving
     *
     * @return how hard the brakes are applied to stop Traci moving
     */
    public float getDamping() {
        return damping;
    }

    /**
     * Returns the upper limit on Traci's left-right movement.
     *
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on Traci's left-right movement.
     */
    public float getMaxSpeed() {
        return maxspeed;
    }

    /**
     * Returns the name of the ground sensor
     *
     * This is used by the ContactListener. Because we do not associate the
     * sensor with its own obstacle,
     *
     * @return the name of the ground sensor
     */
    public String getSensorName() {
        return sensorName;
    }

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }
    public Float getWidth() {
        return width;
    }
    public Float getHeight() {
        return height;
    }

    /**
     * Creates a new Traci avatar with the given physics data
     *
     * The physics units are used to size the mesh relative to the physics
     * body. All other attributes are defined by the JSON file. Because of
     * transparency around the image file, the physics object will be slightly
     * thinner than the mesh in order to give a tighter hitbox.
     *
     * @param units     The physics units
     * @param data      The physics constants for Traci
     */
//    public Traci(float units, JsonValue data, AssetDirectory directory) {
    public Traci(AssetDirectory directory, float units, JsonValue data) {
        Traci.directory = directory;
        this.data = data;
        this.units = units;
        JsonValue debugInfo = data.get("debug");

        x = data.get("pos").getFloat(0);
        y = data.get("pos").getFloat(1);
        float s = data.getFloat( "size" );
        float size = s*units;

        animationTextureIdleTorch = directory.getEntry("platform-playerIDLETORCH", Texture.class);
        animationTextureIdleNoTorch = directory.getEntry("platform-playerIDLENOTORCH", Texture.class);
        animationTextureMovementTorch = directory.getEntry("platform-playerMOVEMENTTORCH", Texture.class);
        animationTextureMovementNoTorch = directory.getEntry("platform-playerMOVEMENTNOTORCH", Texture.class);

        // The capsule is smaller than the image
        // "inner" is the fraction of the original size for the capsule
        width = s*data.get("inner").getFloat(0);
        height = s*data.get("inner").getFloat(1);
        obstacle = new CapsuleObstacle(x, y, width, height);
        ((CapsuleObstacle)obstacle).setTolerance( debugInfo.getFloat("tolerance", 0.5f) );

        obstacle.setDensity( data.getFloat( "density", 0 ) );
        obstacle.setFriction( data.getFloat( "friction", 0 ) );
        obstacle.setRestitution( data.getFloat( "restitution", 0 ) );
        obstacle.setFixedRotation(true);
        obstacle.setPhysicsUnits( units );
        obstacle.setUserData( this );
        obstacle.setName("traci");

        debug = ParserUtils.parseColor( debugInfo.get("avatar"),  Color.WHITE);
        sensorColor = ParserUtils.parseColor( debugInfo.get("sensor"),  Color.WHITE);

        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        force = data.getFloat("force", 0);
        jump_force = data.getFloat( "jump_force", 0 );
        jumpLimit = data.getInt( "jump_cool", 0 );
        shotLimit = data.getInt( "shot_cool", 0 );

        // Gameplay attributes
        groundState = GroundState.AIRBORNE;
        isShooting = false;
        isJumping = false;
        faceRight = true;
        hasTorch = false;

        shootCooldown = 0;
        jumpCooldown = 0;

        // Create a rectangular mesh for Traci. This is the same as for door,
        // since Traci is a rectangular image. But note that the capsule is
        // actually smaller than the image, making a tighter hitbox. You can
        // see this when you enable debug mode.
        mesh.set(-size/2.0f,-size/2.0f,size,size);

        //fixture filter for lights
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.filter.categoryBits = CATEGORY_AVATAR; // Object's category
        fixtureDef.filter.maskBits = CATEGORY_ENVIRONMENT; // Which lights affect it

        this.bodyTouchedClimbables = new HashSet<>();
    }

    public void create_Fixture() {
        if (obstacle.getBody() != null) {
            FixtureDef fd = new FixtureDef();
            fd.filter.categoryBits = PLAYER;
            fd.filter.maskBits = WALL | TORCH | MOTH;
            PolygonShape fixShape = new PolygonShape();
            fixShape.setAsBox(width, height, new Vector2(0, 0), 0.0f);
            fd.shape = fixShape;
            obstacle.getBody().createFixture(fd);
        } else {
            System.out.println("Error: Body is null in createFixture()");
        }
    }

    public void updateCollisionState() {
        Fixture fixture = obstacle.getBody().getFixtureList().get(0);
        if ((fixture.getFilterData().maskBits & TOTEM) != 0) {
            fixture.getFilterData().maskBits &= ~TOTEM; // remove
        } else {
            fixture.getFilterData().maskBits |= TOTEM;  // add
        }
    }

    /**
     * Creates the sensor for Traci.
     *
     * We only allow the Traci to jump when she's on the ground. Double jumping
     * is not allowed.
     *
     * To determine whether Traci is on the ground we create a thin sensor under
     * her feet, which reports collisions with the world but has no collision
     * response. This sensor is just a FIXTURE, it is not an obstacle. We will
     * talk about the different between these later.
     *
     * Note this method is not part of the constructor. It can only be called
     * once the physics obstacle has been activated.
     */
    public void createSensor() {
        Vector2 sensorCenter = new Vector2(0, -height / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;

        JsonValue sensorjv = data.get("sensor");
        float w = sensorjv.getFloat("shrink",0)*width/2.0f;
        float h = sensorjv.getFloat("height",0);
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(w, h, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Body body = obstacle.getBody();
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorName = "traci_sensor";
        sensorFixture.setUserData(sensorName);

        // Finally, we need a debug outline
        float u = obstacle.getPhysicsUnits();
        PathFactory factory = new PathFactory();
        sensorOutline = new Path2();
        factory.makeRect( (sensorCenter.x-w/2)*u,(sensorCenter.y-h/2)*u, w*u, h*u,  sensorOutline);
    }


    /**
     * Applies the force to the body of Traci
     *
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!obstacle.isActive()) {
            return;
        }
        Vector2 pos = obstacle.getPosition();
        float vx = obstacle.getVX();
        float vy = obstacle.getVY();
        Body body = obstacle.getBody();

        if (groundState.equals(GroundState.CLIMBING)) {
            int count = bodyTouchedClimbables.size();
            Vector2 maxVel = new Vector2();
            Vector2 avgVel = new Vector2();
            float avgAngle = 0;
//            System.out.println("pre loop" + obstacle.getLinearVelocity());

            /*Avg pos used and calculated as a way to prevent player from hopping of the top of a rope accidentally*/
            Vector2 avgPos = new Vector2();
//            Vector2 avgPos = Vector2.Zero;
            for (EnhancedObstacleSprite eos : bodyTouchedClimbables) {
                Vector2 eosVel = eos.getObstacle().getLinearVelocity();
                if (eosVel.len2() > maxVel.len2()) {
                    maxVel.set(eosVel);
                }
                avgVel.add(eosVel);
                avgAngle += eos.getObstacle().getAngle();
                avgPos.add(eos.getObstacle().getPosition().cpy());
            }
            avgVel.scl(1f/count);
            avgPos.scl(1f/count);
            avgAngle /= (count);
            obstacle.setAngle((float) ((Math.PI/2)-Math.abs(avgAngle)));
//            System.out.println("angle -> " + avgAngle);
//            System.out.println(count);

            Vector2 ropeDir = new Vector2((float)Math.cos(avgAngle), (float)Math.sin(avgAngle));
            Vector2 perpDir = new Vector2(-ropeDir.y, ropeDir.x);

            java.util.function.Predicate<EnhancedObstacleSprite> testLadder = (item) -> item instanceof Ladder;
            int topProtector = 1;
            if (!bodyTouchedClimbables.stream().allMatch(testLadder) && avgPos.y < (pos.y - height/4) && getMovement().y > 0) {
                topProtector= 0;
            }
            if (avgPos.y > pos.y) {
                maxVel.scl(Math.signum(avgPos.x - pos.x) , Math.signum(avgPos.y - pos.x));
            }
            if (topProtector == 0) {
                pos.y = avgPos.y;
            }

            Vector2 playerMovement = new Vector2(ropeDir).scl(-getMovement().y * (1f/7 * topProtector))
                .add(new Vector2(perpDir).scl(getMovement().x * (1f/10)));

            if (playerMovement.len() == 0 && bodyTouchedClimbables.size() > 1) {
                playerMovement = avgPos.cpy().sub(getObstacle().getPosition());
            }
            obstacle.setLinearVelocity(avgVel.add(playerMovement));

            if (isJumping()) {
                removeClimbingPhysics();
                obstacle.setLinearVelocity(Vector2.Zero);
                forceCache.set(0, jump_force);
                body.applyLinearImpulse(forceCache,pos,true);
            }
        } else {
            // TYPICAL MOVEMENT LOGIC
//            getObstacle().getBody().setGravityScale(1);

            if (getMovement().x == 0f) {
                forceCache.set(-getDamping()*vx,0);
                body.applyForce(forceCache,pos,true);
            }

            if (Math.abs(vx) >= getMaxSpeed()) {
                obstacle.setVX(Math.signum(vx)*getMaxSpeed());
            } else {
                forceCache.set(getMovement().x,0);
                body.applyForce(forceCache,pos,true);
            }

            if (isJumping()) {
                forceCache.set(0, jump_force);
                body.applyLinearImpulse(forceCache,pos,true);
            }
        }
    }

    public void applyClimbingPhysics() {
        getObstacle().getBody().setGravityScale(0);
        getObstacle().setFixedRotation(false);
        applyWeightToClimbable(bodyTouchedClimbables);
    }
    public void removeClimbingPhysics() {
        getObstacle().setAngle(0);
        getObstacle().setFixedRotation(true);
        removeWeightToClimbable(bodyTouchedClimbables);
        getObstacle().getBody().setGravityScale(1);
    }

    public JointDef attachTorchToAvatar(Torch t) {
        WeldJointDef jointDef = new WeldJointDef();
        Vector2 anchor = new Vector2(obstacle.getX(), obstacle.getY());
        jointDef.initialize(obstacle.getBody(), t.getObstacle().getBody(), anchor);
        jointDef.collideConnected = false;
        return jointDef;
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt    Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {
        // Apply cooldowns
        if (isJumping()) {
            jumpCooldown = jumpLimit;
        } else {
            jumpCooldown = Math.max(0, jumpCooldown - 1);
        }

        if (isShooting()) {
            shootCooldown = shotLimit;
        } else {
            shootCooldown = Math.max(0, shootCooldown - 1);
        }

        cdFrameCount++;
        frameIndex = (cdFrameCount / FRAME_DURATION) % TOTAL_FRAMES;
        if (cdFrameCount >= FRAME_DURATION * TOTAL_FRAMES) {
            cdFrameCount = 0;
        }

        super.update(dt);
    }

    /**
     * Draws the physics object.
     *
     * This method is overridden from ObstacleSprite. We need to flip the
     * texture back-and-forth depending on her facing. We do that by creating
     * a reflection affine transform.
     *
     * @param batch The sprite batch to draw to
     */
    @Override
    public void draw(SpriteBatch batch) {
        float drawX = obstacle.getX() - getWidth() / 2f;
        float drawY = obstacle.getY() - getHeight() / 2f;



        Texture animationTexture;
        if (getMovement() != null) {
            if (!getMovement().epsilonEquals(0,0)){
                int srcIndex = frameIndex * MOVEMENT_FRAME_WIDTH;
                if (hasTorch){
                    animationTexture = animationTextureMovementTorch;
                } else {
                    animationTexture = animationTextureMovementNoTorch;
                }
                batch.draw(animationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits()*1.5f, srcIndex, 0, MOVEMENT_FRAME_WIDTH, MOVEMENT_FRAME_HEIGHT, !isFacingRight(), false);
            } else {
                int srcIndex = frameIndex * IDLE_FRAME_WIDTH;
                if (hasTorch){
                    animationTexture = animationTextureIdleTorch;
                } else {
                    animationTexture = animationTextureIdleNoTorch;
                }
                batch.draw(animationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits()*1.5f, srcIndex, 0, IDLE_FRAME_WIDTH, IDLE_FRAME_HEIGHT, !isFacingRight(), false);

            }
        }
    }

    /**
     * Draws the outline of the physics object.
     *
     * This method is overridden from ObstacleSprite. By default, that method
     * only draws the outline of the main physics obstacle. We also want to
     * draw the outline of the sensor, and in a different color. Since it
     * is not an obstacle, we have to draw that by hand.
     *
     * @param batch The sprite batch to draw to
     */
    @Override
    public void drawDebug(SpriteBatch batch) {
        super.drawDebug( batch );

        if (sensorOutline != null) {
            batch.setTexture( Texture2D.getBlank() );
            batch.setColor( sensorColor );

            Vector2 p = obstacle.getPosition();
            float a = obstacle.getAngle();
            float u = obstacle.getPhysicsUnits();

            // transform is an inherited cache variable
            transform.idt();
            transform.preRotate( (float) (a * 180.0f / Math.PI) );
            transform.preTranslate( p.x * u, p.y * u );

            //
            batch.outline( sensorOutline, transform );
        }
    }

    public void registerClimbable(EnhancedObstacleSprite obj) {
        if (!bodyTouchedClimbables.contains(obj) && groundState.equals(GroundState.CLIMBING) ) {
            applyWeightToClimbable(new HashSet<>(List.of(obj)));
        }
        bodyTouchedClimbables.add(obj);
    }
    public void removeClimbable(EnhancedObstacleSprite obj) {
        if (bodyTouchedClimbables.contains(obj) && groundState.equals(GroundState.CLIMBING)) {
            removeWeightToClimbable(new HashSet<>(List.of(obj)));
        }
        bodyTouchedClimbables.remove(obj);
    }
    private void applyWeightToClimbable(Set<EnhancedObstacleSprite> set) {
//        System.out.println("adding weight to # objects -> " + set.size());
        for (EnhancedObstacleSprite obj : set) {
//            System.out.println("appyly weight");
            Fixture fixture = obj.getObstacle().getBody().getFixtureList().first();
            //TODO: fix with updated masses later
            float currentDensity = fixture.getDensity();
            float adjustment = (getObstacle().getMass() * .05f);
            fixture.setDensity(currentDensity + adjustment);
            obj.getObstacle().getBody().resetMassData();
        }
    }
    private void removeWeightToClimbable(Set<EnhancedObstacleSprite> set) {
//        System.out.println("removing weight to # objects -> " + set.size());
        for (EnhancedObstacleSprite obj : set) {
            if (obj.getObstacle().isRemoved()) {
                continue;
            }
//            System.out.println("remove weight");
            Fixture fixture = obj.getObstacle().getBody().getFixtureList().first();
            float currentDensity = fixture.getDensity();
            float adjustment = (getObstacle().getMass() * .05f);
            fixture.setDensity(currentDensity - adjustment);
            obj.getObstacle().getBody().resetMassData();
//            System.out.println(obj + " post -> " +obj.getObstacle().getDensity());
//            System.out.println(obj +" post -> " +obj.getObstacle().getMass());
        }
    }
    public Set<EnhancedObstacleSprite> getBodyTouchedClimbables() {
        return bodyTouchedClimbables;
    }
}

