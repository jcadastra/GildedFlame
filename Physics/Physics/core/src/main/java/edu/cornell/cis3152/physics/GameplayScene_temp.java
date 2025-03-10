package edu.cornell.cis3152.physics;/*
 * Torch_playground.java
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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.cis3152.physics.level_player.FireController;
import edu.cornell.cis3152.physics.level_player.enemies.*;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import edu.cornell.cis3152.physics.level_player.player.*;
import edu.cornell.cis3152.physics.level_player.CollisionController;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.audio.SoundEffectManager;
import edu.cornell.gdiac.physics2.Obstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class GameplayScene_temp extends GameplayScene {
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
    protected Traci avatar;
    private Vector2 location;
    protected Torch torch;

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
    protected boolean queueAddTorch;

    /**
     * Active joint for avatar holding torch
     */
    private Joint activeTorchJoint;
    private List<Enemy> enemies;

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
    protected CollisionController contactListener;
    protected FireController fireController;

    /**
     * If torch is on the right of the avatar
     */
    private boolean torchOnRight;

    /**
     * The name to get the values from from the temp layouts
     */
    private String internalLevelName;

    /**
     * Creates and initialize a new instance of the platformer game
     * <p>
     * The game has default gravity and other settings
     */
    public GameplayScene_temp(AssetDirectory directory, String internalLevelName) {
        super(directory, "platform");
        this.internalLevelName = internalLevelName;
        contactListener = new CollisionController(directory);
        world.setContactListener(contactListener);
        sensorFixtures = new ObjectSet<Fixture>();

        fireController = new FireController();

        // Pull out sounds
        jumpSound = directory.getEntry("platform-jump", SoundEffect.class);
        fireSound = directory.getEntry("platform-pew", SoundEffect.class);
        plopSound = directory.getEntry("platform-plop", SoundEffect.class);
        volume = constants.getFloat("volume", 1.0f);
        populateLevel();
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        JsonValue values = constants.get("world");
        Vector2 gravity = new Vector2(0, values.getFloat("gravity"));

        if (activeTorchJoint != null) {
            world.destroyJoint(activeTorchJoint);
            activeTorchJoint = null;
        }
        if (activeLightJoint != null) {
            world.destroyJoint(activeLightJoint);
            activeLightJoint = null;
        }

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
        if (contactListener != null) {
            world.setContactListener(contactListener);
        }
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
        Fire fire = new Fire(units, new Vector2(10,10));
        addSprite(fire);

        // Create Torch
        torch = new Torch(units, constants.get("torch"));
        torch.setTexture(texture);
        addSprite(torch);
        l.getObstacle().setPosition(torch.getObstacle().getPosition());
        fire.getObstacle().setPosition(torch.getObstacle().getPosition());
        activeLightJoint = world.createJoint(torch.attachObj(l));
        activeLightJoint = world.createJoint(torch.attachObj(fire));

        // Create Totem
        texture = directory.getEntry("rocket-totem01", Texture.class);
        Totem totem = new Totem(0, units, constants.get("totem"), directory);
        totem.setTexture(texture);
        addSprite(totem);
        totem.createSensor();
//        enemies.add(totem);

        // Create Moth
        texture = directory.getEntry("rocket-moth01", Texture.class);
        Moth moth = new Moth(0, units, constants.get("moth"), directory);
        moth.setTexture(texture);
        addSprite(moth);
        moth.createSensor();
//        enemies.add(moth);


        texture = directory.getEntry( "shared-earth", Texture.class );
        platform = new Surface(new float[]{11.0f, 3.0f, 15.0f, 3.0f, 15.0f, 5.0f, 11.0f, 5.0f}, units, walls);
        platform.getObstacle().setName("floor");
        platform.setTexture(texture);
        addSprite(platform);

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
        supplementaryCollisionActions();
        for (Enemy e : enemies) {
            e.update();
        }
        torch.update();

        InputController input = InputController.getInstance();
        // Process actions in object model
        avatar.setMovement(input.getHorizontal() * avatar.getForce());
        avatar.setJumping(input.didPrimary());
        avatar.setShooting(input.didSecondary());

        // Add a bullet if we fire
        if (avatar.isShooting()) {
            createBullet();
        }

        if (input.getThrowing() && avatar.getHasTorch() && activeTorchJoint != null) {
            avatar.setHasTorch(false);
            world.destroyJoint(activeTorchJoint);
            activeTorchJoint = null;
            torch.applyThrowForce(avatar.isFacingRight() ? 1 : -1);
            torch.resetPickUp();
        }

        avatar.applyForce();
        if (avatar.isJumping()) {
            SoundEffectManager sounds = SoundEffectManager.getInstance();
//            sounds.play("jump", jumpSound, volume);
        }
        if ((queueAddTorch && activeTorchJoint == null) || (activeTorchJoint != null &&
            torchOnRight != avatar.isFacingRight())) {
            joinTorchtoAvatar();
        }
    }

    private void supplementaryCollisionActions() {
        Stack<Object[]> todos = contactListener.getCollisionFlags();
        while ( !todos.isEmpty() ) {
            Object[] todo_action = todos.pop();
            switch ((String) todo_action[0]) {
                case "removeBullet":
                    removeBullet((Bullet) todo_action[1]);
                    break;
                case "addTorch":
                    if (torch.canBePickedUp()) {
                        queueAddTorch = true;
                    }
                    break;
                case "traciGrounded":
                    sensorFixtures.add((Fixture) todo_action[1]);
                    break;
                case "traciAirborne":
                    sensorFixtures.remove((Fixture) todo_action[1]);
                    if (sensorFixtures.size == 0) {
                        avatar.setGrounded(false);
                    }
                    break;
                case "addFire":
                    Fire f = (Fire) todo_action[1];
                    f.getObstacle().setBodyType( BodyType.StaticBody );
                    addSprite(f);
                    System.out.println("added torch");
            }
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
