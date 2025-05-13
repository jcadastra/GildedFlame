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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.EnhancedObstacleSprite;
import edu.cornell.cis3152.physics.level_player.enviromentals.Ladder;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.Texture2D;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.physics2.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Traci's avatar for the platform game.
 * <p>
 * An ObstacleSprite is a sprite (specifically a textured mesh) that is
 * connected to a obstacle. It is designed to be the same size as the
 * physics object, and it tracks the physics object, matching its position
 * and angle at all times.
 * <p>
 * Note that unlike a traditional ObstacleSprite, this attaches some additional
 * information to the obstacle. In particular, we add a sensor fixture. This
 * sensor is used to prevent double-jumping. However, we only have one mesh,
 * the mesh for Traci. The sensor is invisible and only shows up in debug mode.
 * While we could have made the fixture a separate obstacle, we want it to be a
 * simple fixture so that we can attach it to the obstacle WITHOUT using joints.
 */
public class Avatar extends ObstacleSprite {
    public static final int PLAYER = 0x00000001;
    public static final int WALL = 0x00000002;
    public static final int TORCH = 0x00000004;
    public static final int TOTEM = 0x00000008;
    public static final int MOTH = 0x00000010;
    public static final short CATEGORY_AVATAR = 0x0002;  // 00000010
    public static final short CATEGORY_ENVIRONMENT = 0x0004;  // 00000100
    public static final short CATEGORY_LIGHT = 0x0008;  // 00001000
    public static final int TOTAL_FRAMES = 6;
    public static final int TOTAL_JUMP_UP_FRAMES = 4;
    public static final int TOTAL_JUMP_FALL_FRAMES = 2;
    public static final int TOTAL_JUMP_LAND_FRAMES = 2;
    public static final int FRAME_HEIGHT = 550;
    public static final int FRAME_WIDTH = 350;

    public static final int MOVEMENT_FRAME_DURATION = 16;
    public static final int THROW_TOTAL_FRAMES = 3;
    public static final int THROW_FRAME_DURATION = 6;
    public static final int CLIMB_FRAME_DURATION = 18;
    private static final int JUMP_FRAME_DURATION = 7;
    private static final int JUMP_FRAME_LAND_DURATION = 6;
    private static final int DEATH_TOTAL_FRAMES = 9;
    private static final int DEATH_FRAME_DURATION = 16;
    private static final int FRAME_DURATION = 12;
    private static final float FADE_SPEED = 0.05f;
    public static int throwCount = 0;
    protected static AssetDirectory directory;
    private static Texture animationTextureIdleTorch;
    private static Texture animationTextureIdleNoTorch;
    private static Texture animationTextureMovementTorch;
    private static Texture animationTextureMovementNoTorch;
    private static Texture animationTextureThrow;
    private static Texture animationTextureJumpNoTorchFall;
    private static Texture animationTextureJumpNoTorchLand;
    private static Texture animationTextureJumpNoTorchUp;
    private static Texture animationTextureJumpTorchFall;
    private static Texture animationTextureJumpTorchLand;
    private static Texture animationTextureJumpTorchUp;
    private static Texture animationTextureJumpThrowFall;
    private static Texture animationTextureJumpThrowLand;
    private static Texture animationTextureJumpThrowUp;
    private static Texture animationTextureClimbUp;
    private static Texture animationTextureClimbDown;
    private static Texture animationTextureDeath;
    /**
     * Whether the player has torch in hand
     */
    private static boolean hasTorch;
    /**
     * The initializing data (to avoid magic numbers)
     */
    private final JsonValue data;
    private final float units;
    /**
     * Cache for internal force calculations
     */
    private final Vector2 forceCache = new Vector2();
    /**
     * Cache for the affine flip
     */
    private final Affine2 flipCache = new Affine2();
    private final Set<EnhancedObstacleSprite> bodyTouchedClimbables;
    /**
     * The width of Traci's avatar
     */
    private final float width;
    /**
     * The height of Traci's avatar
     */
    private final float height;
    /**
     * The factor to multiply by the input
     */
    private final float force;
    /**
     * The amount to slow the character down
     */
    private final float damping;
    /**
     * The maximum character speed
     */
    private final float maxspeed;
    /**
     * The impulse for the character jump
     */
    private final float jump_force;
    /**
     * Cooldown (in animation frames) for jumping
     */
    private final int jumpLimit;
    /**
     * Cooldown (in animation frames) for shooting
     */
    private final int shotLimit;
    /**
     * The debug color for the sensor
     */
    private final Color sensorColor;
    private final float x;
    private final float y;
    private int fallTimer = 10;
    private GroundState groundState;

    /**
     * isFalling compares previous position to current position
     * -1 is currently falling, 0 is standing still, and 1 is jumping.
     */
    private int isFalling = 0;
    private int prevFalling = 0;
    /**
     * The current horizontal movement of the character
     */
    private Vector2 movement;
    /**
     * Which direction is the character facing
     */
    private boolean faceRight;
    /**
     * How long until we can jump again
     */
    private int jumpCooldown;
    /**
     * Whether we are actively jumping
     */
    private boolean isJumping;
    /**
     * How long until we can shoot again
     */
    private int shootCooldown;
    /**
     * Whether we are actively shooting
     */
    private boolean isShooting;
    private boolean doOnce;
    /**
     * The outline of the sensor obstacle
     */
    private Path2 sensorOutline;
    /**
     * The name of the sensor fixture
     */
    private String sensorName;
    private int cdFrameCount = 0;
    private int frameIndex = 0;
    private Vector2 prevPosition;
    private final boolean startSwitch = true;
    private int throwFrameIndex = 0;
    private boolean throwSwitch = false;
    private int jumpFrameCount = 0;
    private boolean justLanded = false;
    private boolean reachedApex = false;
    private boolean hadTorch;
    private float deathOpacity = 0f;
    private int jumpDeadTimer = 0;
    public void setJumpDeadTimer() {jumpDeadTimer = 20;}
    public boolean jumpDeadTimerAvaliable() {return jumpDeadTimer <= 0;}

    /**
     * Creates a new Traci avatar with the given physics data
     * <p>
     * The physics units are used to size the mesh relative to the physics
     * body. All other attributes are defined by the JSON file. Because of
     * transparency around the image file, the physics object will be slightly
     * thinner than the mesh in order to give a tighter hitbox.
     *
     * @param units The physics units
     * @param data  The physics constants for Traci
     */
//    public Traci(float units, JsonValue data, AssetDirectory directory) {
    public Avatar(AssetDirectory directory, float units, JsonValue data) {
        Avatar.directory = directory;
        this.data = data;
        this.units = units;
        JsonValue debugInfo = data.get("debug");

        x = data.get("pos").getFloat(0);
        y = data.get("pos").getFloat(1);
        float s = data.getFloat("size");
        float size = s * units;

        animationTextureIdleTorch = directory.getEntry("platform-playerIDLETORCH", Texture.class);
        animationTextureIdleNoTorch = directory.getEntry("platform-playerIDLENOTORCH", Texture.class);
        animationTextureMovementTorch = directory.getEntry("platform-playerMOVEMENTTORCH", Texture.class);
        animationTextureMovementNoTorch = directory.getEntry("platform-playerMOVEMENTNOTORCH", Texture.class);
        animationTextureJumpNoTorchFall = directory.getEntry("platform-playerJUMPNOTORCHFALL", Texture.class);
        animationTextureJumpNoTorchLand = directory.getEntry("platform-playerJUMPNOTORCHLAND", Texture.class);
        animationTextureJumpNoTorchUp = directory.getEntry("platform-playerJUMPNOTORCHUP", Texture.class);
        animationTextureJumpTorchFall = directory.getEntry("platform-playerJUMPTORCHFALL", Texture.class);
        animationTextureJumpTorchLand = directory.getEntry("platform-playerJUMPTORCHLAND", Texture.class);
        animationTextureJumpTorchUp = directory.getEntry("platform-playerJUMPTORCHUP", Texture.class);
        animationTextureJumpThrowFall = directory.getEntry("platform-playerJUMPTHROWFALL", Texture.class);
        animationTextureJumpThrowLand = directory.getEntry("platform-playerJUMPTHROWLAND", Texture.class);
        animationTextureJumpThrowUp = directory.getEntry("platform-playerJUMPTHROWUP", Texture.class);
        animationTextureThrow = directory.getEntry("platform-playerTHROW", Texture.class);
        animationTextureClimbUp = directory.getEntry("platform-playerCLIMBUP", Texture.class);
        animationTextureClimbDown = directory.getEntry("platform-playerCLIMBDOWN", Texture.class);
        animationTextureDeath = directory.getEntry("platform-playerDEATH", Texture.class);


        // The capsule is smaller than the image
        // "inner" is the fraction of the original size for the capsule
        width = s * data.get("inner").getFloat(0);
        height = s * data.get("inner").getFloat(1);
        obstacle = new CapsuleObstacle(x, y, width, height);
        ((CapsuleObstacle) obstacle).setTolerance(debugInfo.getFloat("tolerance", 0.5f));

        obstacle.setDensity(data.getFloat("density", 0));
        obstacle.setFriction(data.getFloat("friction", 0));
        obstacle.setRestitution(data.getFloat("restitution", 0));
        obstacle.setFixedRotation(true);
        obstacle.setPhysicsUnits(units);
        obstacle.setUserData(this);
        obstacle.setName("traci");

        debug = ParserUtils.parseColor(debugInfo.get("avatar"), Color.WHITE);
        sensorColor = ParserUtils.parseColor(debugInfo.get("sensor"), Color.WHITE);

        maxspeed = data.getFloat("maxspeed", 0);
        damping = data.getFloat("damping", 0);
        force = data.getFloat("force", 0);
        jump_force = data.getFloat("jump_force", 0);
        jumpLimit = data.getInt("jump_cool", 0);
        shotLimit = data.getInt("shot_cool", 0);

        // Gameplay attributes
        groundState = GroundState.GROUNDED;
        isShooting = false;
        isJumping = false;
        faceRight = true;
        hasTorch = false;
        hadTorch = false;
        doOnce = true;

        shootCooldown = 0;
        jumpCooldown = 0;

        // Create a rectangular mesh for Traci. This is the same as for door,
        // since Traci is a rectangular image. But note that the capsule is
        // actually smaller than the image, making a tighter hitbox. You can
        // see this when you enable debug mode.
        mesh.set(-size / 2.0f, -size / 2.0f, size, size);

        //fixture filter for lights
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.filter.categoryBits = CATEGORY_AVATAR; // Object's category
        fixtureDef.filter.maskBits = CATEGORY_ENVIRONMENT; // Which lights affect it

        this.bodyTouchedClimbables = new HashSet<>();
    }

    /**
     * Returns true if Traci has torch.
     */
    public static boolean getHasTorch() {
        return hasTorch;
    }

    /**
     * Sets whether Traci has torch.
     */
    public void setHasTorch(boolean value) {
        hasTorch = value;
    }

    public int getFallTimer() {
        return fallTimer;
    }

    public void decrementFallTimer() {
        fallTimer--;
    }

    public void resetFallTimer() {
        fallTimer = 10;
    }

    public void startFallTimer() {
        if (getFallTimer() == 0) {
            setGroundedState(GroundState.AIRBORNE);
            resetFallTimer();
        } else {
            decrementFallTimer();
        }

    }

    public Vector2 getPrevPosition() {
        return prevPosition;
    }

    public int getIsFalling() {
        return isFalling;
    }

    public void setIsFalling(int val) {
        isFalling = val;
    }

    public float getUnits() {
        return units;
    }

    private void resetFrames() {
        cdFrameCount = 0;
        frameIndex = 0;
        jumpFrameCount = 0;
        reachedApex = false;
    }

    private void resetJumpFrames() {
        jumpFrameCount = 0;
        reachedApex = false;
    }

    /**
     * Returns the left/right movement of this character.
     * <p>
     * This is the result of input times force.
     *
     * @return the left/right movement of this character.
     */
    public Vector2 getMovement() {
        return movement;
    }

    /**
     * Sets the left/right movement of this character.
     * <p>
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
     * Returns true if Traci had torch.
     */
    public boolean getHadTorch() {
        return hadTorch;
    }

    /**
     * Sets whether Traci has torch.
     */
    public void setHadTorch(boolean value) {
        hadTorch = value;
    }

    public Vector2 getLocation() {
        return new Vector2(getObstacle().getX(), getObstacle().getY());
    }

    public Vector2 getVelocity() {
        return new Vector2(getObstacle().getLinearVelocity());
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
        if (!isDead()) {
            groundState = state;
        }
    }

    public void becomeSensor() {
        for (Fixture fixture : obstacle.getBody().getFixtureList()) {
            fixture.setSensor(true);
        }
    }


    /**
     * Returns how much force to apply to get Traci moving
     * <p>
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
     * <p>
     * This does NOT apply to vertical movement.
     *
     * @return the upper limit on Traci's left-right movement.
     */
    public float getMaxSpeed() {
        return maxspeed;
    }

    /**
     * Returns the name of the ground sensor
     * <p>
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

    public boolean isDead() {
        return (groundState == GroundState.DEAD);
    }

    public void die() {
        if (groundState != GroundState.DEAD) {
            groundState = GroundState.DEAD;
            cdFrameCount = 0;
            frameIndex = 0;
        }
    }

    /**
     * Creates the sensor for Traci.
     * <p>
     * We only allow the Traci to jump when she's on the ground. Double jumping
     * is not allowed.
     * <p>
     * To determine whether Traci is on the ground we create a thin sensor under
     * her feet, which reports collisions with the world but has no collision
     * response. This sensor is just a FIXTURE, it is not an obstacle. We will
     * talk about the different between these later.
     * <p>
     * Note this method is not part of the constructor. It can only be called
     * once the physics obstacle has been activated.
     */
    public void createSensor() {
        Vector2 sensorCenter = new Vector2(0, -height / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density", 0);
        sensorDef.isSensor = true;

        JsonValue sensorjv = data.get("sensor");
        float w = sensorjv.getFloat("shrink", 0) * width / 2.0f;
        float h = sensorjv.getFloat("height", 0);
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(w, h, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Body body = obstacle.getBody();
        Fixture sensorFixture = body.createFixture(sensorDef);
        sensorName = "traci_sensor";
        sensorFixture.setUserData(sensorName);

        // Finally, we need a debug outline
        float u = obstacle.getPhysicsUnits();
        PathFactory factory = new PathFactory();
        sensorOutline = new Path2();
        factory.makeRect((sensorCenter.x - w / 2) * u, (sensorCenter.y - h / 2) * u, w * u, h * u, sensorOutline);
    }

    /**
     * Applies the force to the body of Traci
     * <p>
     * This method should be called after the force attribute is set.
     */
    public void applyForce() {
        if (!obstacle.isActive()) {
            return;
        }
        Vector2 pos = obstacle.getPosition();
        Body body = obstacle.getBody();

        if (groundState.equals(GroundState.CLIMBING)) {
            int count = bodyTouchedClimbables.size();
            Vector2 maxVel = new Vector2();
            Vector2 avgVel = new Vector2();
            float avgAngle = 0;

            /*Avg pos used and calculated as a way to prevent player from hopping of the top of a rope accidentally*/
//            Vector2 avgPos = Vector2.Zero;
            for (EnhancedObstacleSprite eos : bodyTouchedClimbables) {
                Vector2 eosVel = eos.getObstacle().getLinearVelocity();
                if (eosVel.len2() > maxVel.len2()) {
                    maxVel.set(eosVel);
                }
                avgVel.add(eosVel);
                avgAngle += eos.getObstacle().getAngle();
            }
            avgVel.scl(1f / count);
            avgAngle /= (count);
            Vector2 closestPos = bodyTouchedClimbables.stream().min(
                    Comparator.comparingDouble(x -> x.getObstacle().getPosition().dst(obstacle.getPosition())))
                .map(x -> x.getObstacle().getPosition()).orElse(obstacle.getPosition());
            obstacle.setAngle((float) ((Math.PI / 2) - Math.abs(avgAngle)));

            Vector2 ropeDir = new Vector2((float) Math.cos(avgAngle), (float) Math.sin(avgAngle));
            Vector2 perpDir = new Vector2(-ropeDir.y, ropeDir.x);

            Predicate<EnhancedObstacleSprite> testLadder = (item) -> item instanceof Ladder;
            int topProtector = 1;
            if (!bodyTouchedClimbables.stream().allMatch(testLadder) && closestPos.y < (pos.y - height / 4) && getMovement().y > 0) {
                topProtector = 0;
            }
            if (closestPos.y > pos.y) {
                maxVel.scl(Math.signum(closestPos.x - pos.x), Math.signum(closestPos.y - pos.x));
            }
            if (topProtector == 0) {
                pos.y = closestPos.y;
            }

            Vector2 playerMovement = new Vector2(ropeDir).scl(-getMovement().y * (1f / 7 * topProtector)).add(new Vector2(perpDir).scl(getMovement().x * (1f / 10)));

            if (playerMovement.len() == 0 && bodyTouchedClimbables.size() > 1) {
                playerMovement = closestPos.cpy().sub(getObstacle().getPosition());
            }
            obstacle.setLinearVelocity(avgVel.add(playerMovement));

            if (isJumping()) {
                removeClimbingPhysics();
                obstacle.setLinearVelocity(Vector2.Zero);
                forceCache.set(0, jump_force);
                body.applyLinearImpulse(forceCache, pos, true);
            }
        } else {
            float desiredX = movement.x;
            float vx = body.getLinearVelocity().x;

            if (desiredX != 0 && vx * desiredX < 0) {
                obstacle.setVX(0);
                vx = 0;
            }
            if (desiredX == 0f) {
                forceCache.set(-damping * vx, 0);
                body.applyForce(forceCache, pos, true);

            } else if (Math.abs(vx) >= maxspeed) {
                obstacle.setVX(Math.signum(desiredX) * maxspeed);

            } else {
                forceCache.set(desiredX, 0);
                body.applyForce(forceCache, pos, true);
            }

            if (isJumping()) {
                forceCache.set(0, jump_force);
                body.applyLinearImpulse(forceCache, pos, true);
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
        jointDef.initialize(obstacle.getBody(), t.getObstacle().getBody(), t.getObstacle().getPosition());
        jointDef.collideConnected = false;
        return jointDef;
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     * <p>
     * We use this method to reset cooldowns.
     *
     * @param dt Number of seconds since last animation frame
     */
    @Override
    public void update(float dt) {
        Vector2 currentPosition = getLocation();
        prevFalling = getIsFalling();
        if (jumpDeadTimer > 0) {
            jumpDeadTimer--;
        }

        if (groundState == GroundState.DEAD) {
            cdFrameCount++;
            frameIndex = Math.min((cdFrameCount / DEATH_FRAME_DURATION), DEATH_TOTAL_FRAMES - 1);
            deathOpacity += FADE_SPEED;
            if (deathOpacity > 1f) {
                deathOpacity = 1f;
            }
            super.update(dt);
            return;
        }

        if (prevPosition != null) {
            float deltaY = getLocation().y - prevPosition.y;
            final float EPSILON = 0.01f;

            if (deltaY < -EPSILON) {
                setIsFalling(-1);
            } else if (deltaY > EPSILON) {
                setIsFalling(1);
            } else {
                setIsFalling(0);
            }
        }

        if (justLanded && doOnce) {
            resetJumpFrames();
            doOnce = false;
        }

        if (!hasTorch && hadTorch) {
            if (groundState == GroundState.GROUNDED) {
                if (throwSwitch) {
                    throwSwitch = false;
                    setHadTorch(getHasTorch());
                }
            } else {
                setHadTorch(false);
                throwSwitch = false;
            }


        } else if (!hadTorch && hasTorch) {
            setHadTorch(getHasTorch());
        }

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
        changeState();

        super.update(dt);
        prevPosition = currentPosition;
    }

    public void changeState() {
        switch (getGroundedState()) {
            case GROUNDED:
                if (prevFalling == 0 && getIsFalling() == -1) { // if was landed before and now falling
                    setGroundedState(GroundState.AIRBORNE);
                }
                break;
            case AIRBORNE:
                if (prevFalling == -1 && (getIsFalling() == 0 || getIsFalling() == 1)) { // if it was falling before, and now is not (landed)
                    setGroundedState(GroundState.GROUNDED);
                    } else if (prevFalling == 0 && (Math.abs(getVelocity().y) < 0.01f)) {
                        setGroundedState(GroundState.GROUNDED);
                    }
                break;
            case DEAD:
            case CLIMBING:
            default: // do nothing for all of these
        }
    }

    /**
     * Draws the physics object.
     * <p>
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
        int srcIndex;
        if (groundState == GroundState.DEAD) {
            cdFrameCount++;
            frameIndex = Math.min((cdFrameCount / DEATH_FRAME_DURATION), DEATH_TOTAL_FRAMES - 1);
            int srcX = frameIndex * FRAME_WIDTH;
            batch.draw(animationTextureDeath, (obstacle.getX() - width / 2f) * units, (obstacle.getY() - height / 2f) * units, units, units * 1.5f, srcX, 0, FRAME_WIDTH, FRAME_HEIGHT, !faceRight, false);
            batch.setColor(0f, 0f, 0f, deathOpacity);
            batch.draw(Texture2D.getBlank(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.setColor(1, 1, 1, 1);
        } else if (groundState == GroundState.CLIMBING) {
            if (movement.y > 0) {
                cdFrameCount++;
                frameIndex = (cdFrameCount / CLIMB_FRAME_DURATION) % TOTAL_FRAMES;
                srcIndex = frameIndex * FRAME_WIDTH;
                animationTexture = animationTextureClimbUp;
            } else if (movement.y < 0) {
                cdFrameCount++;
                frameIndex = (cdFrameCount / CLIMB_FRAME_DURATION) % TOTAL_FRAMES;
                srcIndex = frameIndex * FRAME_WIDTH;
                animationTexture = animationTextureClimbDown;
            } else {
                frameIndex = 0;
                srcIndex = 0;
                animationTexture = animationTextureClimbUp;
            }
            batch.draw(animationTexture, drawX * units, drawY * units, units, units * 1.5f, srcIndex, 0, FRAME_WIDTH, FRAME_HEIGHT, !faceRight, false);

        } else if (getGroundedState() == GroundState.AIRBORNE) {
            switch (isFalling) {
                case (-1):
                    if (reachedApex) {
                        resetJumpFrames();
                    }
                    jumpFrameCount++;
                    frameIndex = (jumpFrameCount / JUMP_FRAME_DURATION) % TOTAL_JUMP_FALL_FRAMES;
                    srcIndex = frameIndex * FRAME_WIDTH;
                    animationTexture = hasTorch ? animationTextureJumpTorchFall : animationTextureJumpNoTorchFall;
                    justLanded = true;
                    doOnce = true;
                    batch.draw(animationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits() * 1.5f, srcIndex, 0, FRAME_WIDTH, FRAME_HEIGHT, !isFacingRight(), false);
                    break;
                case (1):
                    jumpFrameCount++;
                    frameIndex = Math.min((jumpFrameCount / JUMP_FRAME_DURATION), TOTAL_JUMP_UP_FRAMES - 1);
                    if (frameIndex == TOTAL_JUMP_UP_FRAMES - 1) {
                        reachedApex = true;
                    }
                    srcIndex = frameIndex * FRAME_WIDTH;
                    animationTexture = hasTorch ? animationTextureJumpTorchUp : animationTextureJumpNoTorchUp;
                    batch.draw(animationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits() * 1.5f, srcIndex, 0, FRAME_WIDTH, FRAME_HEIGHT, !isFacingRight(), false);
                    break;
                case (0):
                    if (prevFalling != -1) {
                        frameIndex = TOTAL_JUMP_UP_FRAMES - 1;
                        srcIndex = frameIndex * FRAME_WIDTH;
                        animationTexture = hasTorch ? animationTextureJumpTorchUp : animationTextureJumpNoTorchUp;
                        batch.draw(animationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits() * 1.5f, srcIndex, 0, FRAME_WIDTH, FRAME_HEIGHT, !isFacingRight(), false);
                    }
            }
        } else if (groundState == GroundState.GROUNDED && getMovement() != null && Math.abs(getMovement().x) > 0.001f) {
            cdFrameCount++;
            frameIndex = (cdFrameCount / MOVEMENT_FRAME_DURATION) % TOTAL_FRAMES;
            srcIndex = frameIndex * FRAME_WIDTH;
            animationTexture = hasTorch ? animationTextureMovementTorch : animationTextureMovementNoTorch;
            batch.draw(animationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits() * 1.5f, srcIndex, 0, FRAME_WIDTH, FRAME_HEIGHT, !isFacingRight(), false);
        } else if (getHadTorch() && !getHasTorch() && groundState == GroundState.GROUNDED) {
            throwFrameIndex = throwCount / THROW_FRAME_DURATION;
            throwCount++;
            if (throwFrameIndex <= THROW_TOTAL_FRAMES) {
                if (throwFrameIndex == THROW_TOTAL_FRAMES) {
                    throwFrameIndex = 0;
                    throwCount = 0;
                    throwSwitch = true;
                } else {
                    srcIndex = throwFrameIndex * FRAME_WIDTH;
                    animationTexture = animationTextureThrow;
                    batch.draw(animationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits() * 1.5f, srcIndex, 0, FRAME_WIDTH, FRAME_HEIGHT, !isFacingRight(), false);
                    throwFrameIndex++;
                }
            }

        } else {
            cdFrameCount++;
            frameIndex = (cdFrameCount / FRAME_DURATION) % TOTAL_FRAMES;
            srcIndex = frameIndex * FRAME_WIDTH;
            animationTexture = hasTorch ? animationTextureIdleTorch : animationTextureIdleNoTorch;
            batch.draw(animationTexture, drawX * getUnits(), drawY * getUnits(), getUnits(), getUnits() * 1.5f, srcIndex, 0, FRAME_WIDTH, FRAME_HEIGHT, !isFacingRight(), false);
        }
    }

    /**
     * Draws the outline of the physics object.
     * <p>
     * This method is overridden from ObstacleSprite. By default, that method
     * only draws the outline of the main physics obstacle. We also want to
     * draw the outline of the sensor, and in a different color. Since it
     * is not an obstacle, we have to draw that by hand.
     *
     * @param batch The sprite batch to draw to
     */
    @Override
    public void drawDebug(SpriteBatch batch) {
        super.drawDebug(batch);

        if (sensorOutline != null) {
            batch.setTexture(Texture2D.getBlank());
            batch.setColor(sensorColor);

            Vector2 p = obstacle.getPosition();
            float a = obstacle.getAngle();
            float u = obstacle.getPhysicsUnits();

            // transform is an inherited cache variable
            transform.idt();
            transform.preRotate((float) (a * 180.0f / Math.PI));
            transform.preTranslate(p.x * u, p.y * u);

            //
            batch.outline(sensorOutline, transform);
        }
    }

    public void registerClimbable(EnhancedObstacleSprite obj) {
        if (!bodyTouchedClimbables.contains(obj) && groundState.equals(GroundState.CLIMBING)) {
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
        for (EnhancedObstacleSprite obj : set) {
            if (    obj.getObstacle().isRemoved()) {
                continue;
            }
            Fixture fixture = obj.getObstacle().getBody().getFixtureList().first();
            //TODO: fix with updated masses later
            float currentDensity = fixture.getDensity();
            float adjustment = (getObstacle().getMass() * .05f);
            fixture.setDensity(currentDensity + adjustment);
            obj.getObstacle().getBody().resetMassData();
        }
    }

    private void removeWeightToClimbable(Set<EnhancedObstacleSprite> set) {
        for (EnhancedObstacleSprite obj : set) {
            if (obj.getObstacle().isRemoved()) {
                continue;
            }
            Fixture fixture = obj.getObstacle().getBody().getFixtureList().first();
            float currentDensity = fixture.getDensity();
            float adjustment = (getObstacle().getMass() * .05f);
            fixture.setDensity(currentDensity - adjustment);
            obj.getObstacle().getBody().resetMassData();
        }
    }

    public Set<EnhancedObstacleSprite> getBodyTouchedClimbables() {
        return bodyTouchedClimbables;
    }
    public void reset() {bodyTouchedClimbables.clear();}

    public enum GroundState {
        GROUNDED, AIRBORNE, CLIMBING, DEAD
    }
}

