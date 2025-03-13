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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.cis3152.physics.level_player.FireController;
import edu.cornell.cis3152.physics.level_player.enemies.*;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import edu.cornell.cis3152.physics.level_player.player.*;
import edu.cornell.cis3152.physics.level_player.CollisionController;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.audio.SoundEffectManager;
import edu.cornell.gdiac.graphics.TextAlign;
import edu.cornell.gdiac.graphics.TextLayout;
import edu.cornell.gdiac.physics2.Obstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.PooledList.Entry;
import edu.cornell.gdiac.util.ScreenListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;


public class GameplayScene_temp extends GameplayScene {
    /** Exit code for quitting the game */
    public static final int EXIT_QUIT = 0;
    /** Exit code for advancing to next level */
    public static final int EXIT_NEXT = 1;
    /** Exit code for jumping back to previous level */
    public static final int EXIT_PREV = 2;
    /** How many frames after winning/losing do we continue? */
    public static final int EXIT_COUNT = 180;
    private boolean queueFailure;

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
    private Joint activeFireJoint;
    protected CollisionController contactListener;
    protected FireController fireController;

    /** A layout for drawing a victory message */
    private TextLayout goodMessage;
    /** A layout for drawing a failure message */
    private TextLayout badMessage;
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
        fireController = new FireController();
        contactListener = new CollisionController(directory, fireController);
        world.setContactListener(contactListener);
        sensorFixtures = new ObjectSet<Fixture>();


        // Pull out sounds
        jumpSound = directory.getEntry("platform-jump", SoundEffect.class);
        fireSound = directory.getEntry("platform-pew", SoundEffect.class);
        plopSound = directory.getEntry("platform-plop", SoundEffect.class);
        volume = constants.getFloat("volume", 1.0f);


        displayFont = directory.getEntry( "shared-retro" , BitmapFont.class);
        goodMessage = new TextLayout();
        goodMessage.setFont( displayFont );
        goodMessage.setAlignment( TextAlign.middleCenter );
        goodMessage.setColor( Color.YELLOW );
        goodMessage.setText("VICTORY!");
        goodMessage.layout();

        badMessage = new TextLayout();
        badMessage.setFont( displayFont );
        badMessage.setAlignment( TextAlign.middleCenter );
        badMessage.setColor( Color.RED );
        badMessage.setText("FAILURE!");
        badMessage.layout();

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
        if (queueFailure) {
            queueFailure = false;
        }

        if (activeTorchJoint != null) {
            world.destroyJoint(activeTorchJoint);
            activeTorchJoint = null;
        }
        if (activeLightJoint != null) {
            world.destroyJoint(activeLightJoint);
            activeLightJoint = null;
        }
        if (activeFireJoint != null) {
            world.destroyJoint(activeFireJoint);
            activeFireJoint = null;
        }
        //TODO: imrpove above

        if (fireController != null) {
            for (Fire fire : fireController.getLitFires()) {
                Joint joint = fire.getFixtureJoint();
                if (joint != null) {
                    world.destroyJoint(fire.getFixtureJoint());
                }
            }
            fireController.resetStorage();
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
        platform = new Surface(new float[]{1.0f, 0f, 31f, 0f, 31f, 1f, 1.0f, 1f}, units, walls);
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
        activeFireJoint = world.createJoint(torch.attachObj(fire));
        // TODO: FIX THE BAOVE^^

        // Create Totem
        texture = directory.getEntry("rocket-totem01", Texture.class);
        Totem totem = new Totem(0, units, constants.get("totem"), directory);
        totem.setTexture(texture);
        addSprite(totem);
        totem.createSensor();
        enemies.add(totem);

        // Create Moth
        texture = directory.getEntry("rocket-moth01", Texture.class);
        Moth moth = new Moth(0, units, constants.get("moth"), directory);
        moth.setTexture(texture);
        addSprite(moth);
        moth.createSensor();
        enemies.add(moth);


        float[] temp = new float[]{
            0f, 0f,
            3f, 1f,
            6f, 0f,
            5f, 2f,
            7f, 4f,
            4f, 4f,
            6f, 6f,
            3f, 5f,
            1f, 7f,
            0f, 4f,
            -1f, 6f,
            -3f, 4f,
            -2f, 2f,
            -4f, 0f,
            -1f, 1f
        };

        texture = directory.getEntry( "rocket-crate0", Texture.class );
        Rope o = new Rope(new Vector2(100,600), new Vector2(150, 600),1, units);
        addSpriteGroup(o);
//        texture = directory.getEntry( "rocket-crate0", Texture.class );
//        GameObject o = new GameObject(temp, 11,9, units);
//        o.getObstacle().setBodyType(BodyType.DynamicBody);
//        o.getObstacle().setName("blocky1");
//        o.setMaterial(new ObstacleMaterial("iron", constants.get(1)));
//        o.setTexture(texture);
//        addSprite(o);

//        texture = directory.getEntry( "platform-rope", Texture.class );
//        RopeBridge bridge = new RopeBridge(units, constants.get("bridge"));
//        bridge.setTexture(texture);
//        addSpriteGroup(bridge);
//        o = new GameObject(12, 3,  units);
//        o.getObstacle().setName("blocky2");
//        o.getObstacle().setBodyType(BodyType.DynamicBody);
//        o.setTexture(texture);
//        addSprite(o);
//        o = new GameObject(9,9, units);
//        o.getObstacle().setBodyType(BodyType.DynamicBody);
//        o.getObstacle().setName("blocky3");
//        o.setTexture(texture);
//        addSprite(o);
//        o = new GameObject(16,9,  units);
//        o.getObstacle().setBodyType(BodyType.DynamicBody);
//        o.getObstacle().setName("blocky3");
//        o.setTexture(texture);
//        addSprite(o);

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
        if (activeFireJoint == null || queueFailure) {
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
//        System.out.println(Gdx.graphics.getFramesPerSecond());
        supplementaryCollisionActions();
        supplementaryFireActions();
        for (Enemy e : enemies) {
            e.update();
        }
        torch.update();
        fireController.update();

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
            joinTorchToAvatar();
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
                case "queueFailure":
                    queueFailure = true;
            }
        }
    }

    private void supplementaryFireActions() {
        Stack<Object[]> todos = fireController.getFireFlags();
        while (!todos.isEmpty()) {
            Object[] todo_action = todos.pop();
            switch ((String) todo_action[0]) {
                case "attachFire":
                    Fire fire = (Fire) todo_action[2];
                    addSprite(fire);
                    joinFireToObject((ObstacleSprite) todo_action[1], fire);
                    System.out.println("added fire to game at " + fire.getObstacle().getPosition());
                    break;
            }
        }
    }

    /**
     * Generates torch joint and connects the avatar to the torch Also used to flip the torch round
     * if avatar rotates
     */
    private void joinTorchToAvatar () {
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
     * Generates fire joint and connects the fire to the obstacle passed in
     */
    private void joinFireToObject (ObstacleSprite o, Fire f) {
        WeldJointDef jointDef = new WeldJointDef();
        jointDef.initialize(o.getObstacle().getBody(), f.getObstacle().getBody(), Vector2.Zero);
        jointDef.collideConnected = false;
        Joint joint = world.createJoint(jointDef);
        f.setFixtureJoint(joint);
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


    /**
     * Returns true if the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @return true if the level is completed.
     */
    public boolean isComplete( ) {
        return complete;
    }

    /**
     * Sets whether the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @param value whether the level is completed.
     */
    public void setComplete(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        complete = value;
    }

    /**
     * Returns true if the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * @return true if the level is failed.
     */
    public boolean isFailure( ) {
        return failed;
    }

    /**
     * Sets whether the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * @param value whether the level is failed.
     */
    public void setFailure(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        failed = value;
    }

    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            addSprite(addQueue.poll());
        }

        // Turn the physics engine crank.
        // NORMALLY we would use a fixed step, not dt
        // But that is harder and a topic of the advanced class
        world.step(dt,WORLD_VELOC,WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<ObstacleSprite>.Entry> iterator = sprites.entryIterator();
        while (iterator.hasNext()) {
            PooledList<ObstacleSprite>.Entry entry = iterator.next();
            ObstacleSprite sprite = entry.getValue();
            Obstacle obj = sprite.getObstacle();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }
    }

    /**
     * Draws the physics objects to the screen
     *
     * For simple worlds, this method is enough by itself. It will need to be
     * overriden if the world needs fancy backgrounds or the like.
     *
     * The method draws all objects in the order that they were added.
     *
     * @param dt    Number of seconds since last animation frame
     */
    public void draw(float dt) {
        // Clear the screen (color is homage to the XNA years)
        ScreenUtils.clear(0.39f, 0.58f, 0.93f, 1.0f);

        // This shows off how powerful our new SpriteBatch is
        batch.begin(camera);

        // Draw the meshes (images)
        for(ObstacleSprite obj : sprites) {
            obj.draw(batch);
        }

        if (debug) {
            // Draw the outlines
            for (ObstacleSprite obj : sprites) {
                obj.drawDebug( batch );
            }
        }

        // Draw a final message
        if (complete && !failed) {
            batch.drawText(goodMessage, width/2, height/2);
        } else if (failed) {
            batch.drawText(badMessage, width/2, height/2);
        }

        batch.end();
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never
     * happen before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        this.width  = width;
        this.height = height;
        if (camera == null) {
            camera = new OrthographicCamera();
        }
        camera.setToOrtho( false, width, height );
        scale.x = width/bounds.width;
        scale.y = height/bounds.height;
        reset();
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY
     * important that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            if (preUpdate(delta)) {
                update(delta); // This is the one that must be defined.
                postUpdate(delta);
            }
            draw(delta);
        }
    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }
}
