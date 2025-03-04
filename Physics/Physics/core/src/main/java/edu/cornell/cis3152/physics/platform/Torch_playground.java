/*
 * PlatformScene.java
 *
 * This is the game scene (player mode) specific to the platforming mini-game.
 * You SHOULD NOT need to modify this file. However, you may learn valuable
 * lessons for the rest of the lab by looking at it.
 *
 * Based on the original PhysicsDemo Lab by Don Holden, 2007
 *
 * Author:  Walker M. White
 * Version: 2/8/2025
 */
package edu.cornell.cis3152.physics.platform;
import java.util.List;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.cis3152.physics.InputController;
import edu.cornell.cis3152.physics.PhysicsScene;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.audio.SoundEffectManager;
import edu.cornell.gdiac.physics2.Obstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;

import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import java.util.ArrayList;

/**
 * The game scene for the platformer game.
 * <p>
 * Look at the method {@link #populateLevel} for how we initialize the scene. Beyond that, a lot of
 * work is done in the method for the ContactListener interface. That is the method that is called
 * upon collisions, giving us a chance to define a response.
 */
public class Torch_playground extends PhysicsScene implements ContactListener {

    /**
     * Texture asset for character avatar
     */
    private TextureRegion avatarTexture;
    /**
     * Texture asset for the spinning barrier
     */
    private TextureRegion barrierTexture;
    /**
     * Texture asset for the bullet
     */
    private TextureRegion bulletTexture;
    /**
     * Texture asset for the bridge plank
     */
    private TextureRegion bridgeTexture;

    /**
     * The jump sound. We only want to play once.
     */
    private SoundEffect jumpSound;
    /**
     * The weapon fire sound. We only want to play once.
     */
    private SoundEffect fireSound;
    /**
     * The weapon pop sound. We only want to play once.
     */
    private SoundEffect plopSound;
    /**
     * The default sound volume
     */
    private float volume;

    /**
     * Reference to the character avatar
     */
    private Traci avatar;
    private Vector2 location;
    private Torch torch;

    private Totem totem;
    private Moth moth;
    private Body body;
    private int count;
    /** Reference to the goalDoor (for collision detection) */
    private Door goalDoor;

    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * Flag to add torch to avatar in update
     */
    private boolean queueAddTorch;

    /**
     * Active joint for avatar holding torch
     */
    private Joint activeTorchJoint;
    private List<Enemy> enemies;

    public AIController aiController;

    public List<Enemy> getEnemies(){
        return enemies;
    }

    public Traci getAvatar() { return avatar; }

    public Vector2 getPlayerLocation() {
        return avatar.getObstacle().getPosition();
    }
    /**
     * Active joint for torch holding light
     */
    private Joint activeLightJoint;

    /**
     * If torch is on the right of the avatar
     */
    private boolean torchOnRight;

    /**
     * Creates and initialize a new instance of the platformer game
     * <p>
     * The game has default gravity and other settings
     */
    public Torch_playground(AssetDirectory directory) {
        super(directory, "platform");
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();

        // Pull out sounds
        jumpSound = directory.getEntry("platform-jump", SoundEffect.class);
        fireSound = directory.getEntry("platform-pew", SoundEffect.class);
        plopSound = directory.getEntry("platform-plop", SoundEffect.class);
        volume = constants.getFloat("volume", 1.0f);
        populateLevel();

        aiController = new AIController(this);
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        JsonValue values = constants.get("world");
        Vector2 gravity = new Vector2(0, values.getFloat("gravity"));

        for (ObstacleSprite sprite : sprites) {
            Obstacle obj = sprite.getObstacle();
            sprite.getObstacle().deactivatePhysics(world);
        }
        sprites.clear();
        addQueue.clear();
        if (world != null) {
            world.dispose();
        }

        world = new World(gravity, false);
        world.setContactListener(this);
        setComplete(false);
        setFailure(false);
        populateLevel();
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        float units = height / bounds.height;

        // Create ground pieces
        Texture texture = directory.getEntry( "shared-earth", Texture.class );
        enemies = new ArrayList<>();

        Surface wall;
        String wname = "wall";
        JsonValue walls = constants.get("walls");
        JsonValue walljv = walls.get("positions");
        for (int ii = 0; ii < walljv.size; ii++) {
            wall = new Surface(walljv.get(ii).asFloatArray(), units, walls);
            wall.getObstacle().setName(wname + ii);
            wall.setTexture(texture);
            addSprite(wall);
        }

        Surface platform;
        String pname = "platform";
        JsonValue plats = constants.get("platforms");
        platform = new Surface(new float[]{1.0f, 0f, 60.0f, 0f, 60.0f, 1f, 1.0f, 1f}, units, walls);
        platform.getObstacle().setName("floor");
        platform.setTexture(texture);
        addSprite(platform);

        // Create Traci
        texture = directory.getEntry("platform-traci", Texture.class);
        avatar = new Traci(units, constants.get("traci"));
        avatar.setTexture(texture);
        addSprite(avatar);
        // Have to do after body is created
        avatar.createSensor();

        Light l = new Light(units, constants.get("light"));
        l.setTexture(texture);
        addSprite(l);
        l.createSensor();

        // Create Torch
        torch = new Torch(units, constants.get("torch"));
        torch.setTexture(texture);
        addSprite(torch);
        l.getObstacle().setPosition(torch.getObstacle().getPosition());
        activeLightJoint = world.createJoint(torch.attachLight(l));
//        System.out.println(l.getObstacle().getMass());
//        torch.createSensor();


        // Totem
        texture = directory.getEntry("rocket-crate01", Texture.class);
        totem = new Totem(0, units, constants.get("totem"));
        totem.setTexture(texture);
        addSprite(totem);
        totem.createSensor();
        enemies.add(totem);

        // Create Moth
//        texture = directory.getEntry("rocket-crate02", Texture.class);
//        moth = new Moth(0, units, constants.get("moth"));
//        moth.setTexture(texture);
//        addSprite(moth);
//        moth.createSensor();
//        enemies.add(moth);
    }

    /**
     * Returns whether to process the update loop
     * <p>
     * At the start of the update loop, we check if it is time to switch to a new game mode. If not,
     * the update proceeds normally.
     *
     * @param dt Number of seconds since last animation frame
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }

        if (!isFailure() && avatar.getObstacle().getY() < -1) {
            setFailure(true);
            return false;
        }

        return true;
    }

    /**
     * Advances the core gameplay loop of this world.
     * <p>
     * This method contains the specific update code for this mini-game. It does not handle
     * collisions, as those are managed by the parent class PhysicsScene. This method is called
     * after input is synced to the current frame, but before collisions are resolved. The very last
     * thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        torch.update();
        totem.update();
//        moth.update();
        InputController input = InputController.getInstance();

        // Process actions in object model
        avatar.setMovement(input.getHorizontal() * avatar.getForce());
        avatar.setJumping(input.didPrimary());
        avatar.setShooting(input.didSecondary());

        // Add a bullet if we fire
        if (avatar.isShooting()) {
            createBullet();
        }

        if (input.getThrowing() && avatar.getHasTorch()) {
            avatar.setHasTorch(false);
            world.destroyJoint(activeTorchJoint);
            activeTorchJoint = null;
            torch.applyThrowForce(avatar.isFacingRight() ? 1 : -1);
            torch.resetPickUp();
        }

        avatar.applyForce();
        if (avatar.isJumping()) {
            SoundEffectManager sounds = SoundEffectManager.getInstance();
            sounds.play("jump", jumpSound, volume);
        }
        if ((queueAddTorch && activeTorchJoint == null) || (activeTorchJoint != null &&
            torchOnRight != avatar.isFacingRight())) {
            joinTorchtoAvatar();
        }

    }

    /**
     * Generates torch joint and connects the avatar to the torch Also used to flip the torch round
     * if avatar rotates
     */
    private void joinTorchtoAvatar() {
        if (activeTorchJoint != null) {
            world.destroyJoint(activeTorchJoint);
            activeTorchJoint = null;
        }
        torch.getObstacle().setAngle(0);
        Vector2 offset = (new Vector2((avatar.isFacingRight() ? 1 : -1) * avatar.getWidth() / 2,
            avatar.getHeight() / 4));
        torch.getObstacle().setPosition(avatar.getObstacle().getPosition().add(offset));
        activeTorchJoint = world.createJoint(avatar.attachTorchToAvatar(torch));
        queueAddTorch = false;
        torchOnRight = avatar.isFacingRight();
        aiController.update();
    }

    /**
     * Adds a new bullet to the world and send it in the right direction.
     */
    private void createBullet() {
        float units = height / bounds.height;

        JsonValue bulletjv = constants.get("bullet");
        Obstacle traci = avatar.getObstacle();

        Texture texture = directory.getEntry("platform-bullet", Texture.class);
        Bullet bullet = new Bullet(units, bulletjv, traci.getPosition(), avatar.isFacingRight());
        bullet.setTexture(texture);
        addQueuedObject(bullet);

        SoundEffectManager sounds = SoundEffectManager.getInstance();
        sounds.play("fire", fireSound, volume);
    }

    /**
     * Removes a new bullet from the world.
     *
     * @param bullet the bullet to remove
     */
    public void removeBullet(ObstacleSprite bullet) {
        bullet.getObstacle().markRemoved(true);
        SoundEffectManager sounds = SoundEffectManager.getInstance();
        sounds.play("plop", plopSound, volume);
    }


    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when we first get a collision between two objects. We use this method
     * to test if it is the "right" kind of collision. In particular, we use it to test if we made
     * it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            ObstacleSprite bd1 = (ObstacleSprite) body1.getUserData();
            ObstacleSprite bd2 = (ObstacleSprite) body2.getUserData();

            // Test bullet collision with world
            if (bd1.getName().equals("bullet") && bd2 != avatar && !bd2.getName().equals("goal")) {
                removeBullet(bd1);
            }

            if (bd2.getName().equals("bullet") && bd1 != avatar && !bd1.getName().equals("goal")) {
                removeBullet(bd2);
            }

            // See if we have landed on the ground.
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
                avatar.setGrounded(true);
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            // Check for win condition
            if ((bd1 == avatar && bd2.getName().equals("goal")) ||
                (bd1.getName().equals("goal") && bd2 == avatar)) {
                setComplete(true);
            }

            if (bd1 == avatar && bd2 == torch && torch.canBePickedUp()) {
                avatar.setHasTorch(true);
                queueAddTorch = true;
            }

            if ((bd1 instanceof Enemy && bd2.getName().startsWith("wall")) ||
                (bd2 instanceof Enemy && bd1.getName().startsWith("wall"))) {
                Enemy enemy = (bd1 instanceof Enemy) ? (Enemy) bd1 : (Enemy) bd2;
                enemy.changeDirection();
            }

            if ((bd2 instanceof Totem && bd1 instanceof Moth) || (bd1 instanceof Totem && bd2 instanceof Moth)) {
                ((Enemy) bd2).changeDirection();
                ((Enemy) bd1).changeDirection();
            }

            if ((bd2 instanceof Light && bd1 instanceof Totem)) {
                Texture texture = directory.getEntry("rocket-totem04", Texture.class);
                totem.setTexture(texture);
                totem.setState(Enemy.EnemyState.IN_LIGHT);
                totem.resetFreeze();
            }

            if ((bd2 instanceof Light && bd1 instanceof Moth)){
                moth.setState(Enemy.EnemyState.ATTRACTED);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two objects cease to touch. The main use of this method is to
     * determine when the characer is NOT on the ground. This is how we prevent double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
            (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }

        if ((bd2 instanceof Light && bd1 instanceof Totem)) {
            Texture texture = directory.getEntry("rocket-totem03", Texture.class);
            totem.setTexture(texture);
            totem.setState(Enemy.EnemyState.OUT_OF_LIGHT);
        }
    }

    /**
     * Unused ContactListener method
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    /**
     * Unused ContactListener method
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * We need this method to stop all sounds when we pause. Pausing happens when we switch game
     * modes.
     */
    public void pause() {
        SoundEffectManager sounds = SoundEffectManager.getInstance();
        sounds.stop("plop");
        sounds.stop("fire");
        sounds.stop("jump");
    }
}
