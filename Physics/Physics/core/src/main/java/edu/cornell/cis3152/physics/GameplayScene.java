/*
 * PhysicsScene.java
 *
 * This lab is composed of three minigames. Instead of repeating physics code
 * over-and-over, we pull it out into a common subclass. However, in most cases
 * you would just combine this class with your game scene (e.g. RocketScene,
 * RagdollScene, or PlatformScene). Unless your game has a lot of minigames
 * (unlikely) you should not need to architect your code this way.
 *
 * For the most part, this class is like CollisionController from previous labs.
 * It handles the physics, while gameplay is handled in the specific minigame
 * subclass. With that said, there is not much to do for collisions; Box2d takes
 * care of all of that for us. This controller invokes Box2d and then performs
 * any after the fact modifications to the data. These modifications are then
 * interpretted by the subclasses to create gameplay.
 *
 * If you study this class and the GDIAC API package, you should be able to
 * understand how the Physics engine works.
 *
 * Based on the original PhysicsDemo Lab by Don Holden, 2007
 *
 * Author:  Walker M. White
 * Version: 2/8/2025
 */
package edu.cornell.cis3152.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.cis3152.physics.level_player.CollisionController;
import edu.cornell.cis3152.physics.level_player.FireController;
import edu.cornell.cis3152.physics.level_player.enemies.Enemy;
import edu.cornell.cis3152.physics.level_player.enemies.Moth;
import edu.cornell.cis3152.physics.level_player.enemies.Totem;
import edu.cornell.cis3152.physics.level_player.player.Torch;
import edu.cornell.cis3152.physics.level_player.player.Traci;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.audio.SoundEffect;
import edu.cornell.gdiac.audio.SoundEffectManager;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.graphics.*;
import edu.cornell.gdiac.physics2.*;
import edu.cornell.cis3152.physics.level_player.enviromentals.*;
import java.util.Stack;


/**
 * Base class for a world-specific controller.
 *
 * A world has its own objects, assets, and input controller.  Thus this is
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a GameCanvas; that is shared with the main application.
 *
 * You should NOT copy this class for your game. This only exists because we
 * have separate minigames. It factors out all of the common code from each
 * of the minigames.
 */
public class GameplayScene implements Screen {
    // SOME EXIT CODES FOR GDXROOT
    /** Exit code for quitting the game */
    public static final int EXIT_QUIT = 0;
    /** Exit code for advancing to next level */
    public static final int EXIT_NEXT = 1;
    /** Exit code for jumping back to previous level */
    public static final int EXIT_PREV = 2;
    /** How many frames after winning/losing do we continue? */
    public static final int EXIT_COUNT = 180;
    private boolean queueFailure;

    /** The asset directory for retrieving textures, atlases */
    protected AssetDirectory directory;
    /** The drawing camera for this scene */
    protected OrthographicCamera camera;
    /** Reference to the sprite batch */
    protected SpriteBatch batch;

    protected float width;
    protected float height;

    /** The physics constants */
    protected JsonValue constants;

    /** The font for giving messages to the player */
    protected BitmapFont displayFont;
    /** A layout for drawing a victory message */
    private TextLayout goodMessage;
    /** A layout for drawing a failure message */
    private TextLayout badMessage;

    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    /** All the objects in the world. */
    protected PooledList<ObstacleSprite> sprites  = new PooledList<ObstacleSprite>();
    /** Queue for adding objects */
    protected PooledList<ObstacleSprite> addQueue = new PooledList<ObstacleSprite>();
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;

    /** Whether this is an active controller */
    protected boolean active;
    /** Whether we have completed this level */
    protected boolean complete;
    /** Whether we have failed at this world (and need a reset) */
    protected boolean failed;
    /** Whether debug mode is active */
    protected boolean debug;
    /** Countdown active for winning or losing */
    protected int countdown;

    private List<Enemy> enemies;
    protected Traci avatar;
    protected Torch torch;

    /** Reference to the goalDoor (for collision detection) */
    private Door goalDoor;

    //protected Totem totem;
    //protected Moth moth;

    private String levelName = "moth_intro";

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
     * Active joint for avatar holding torch
     */
    private Joint activeTorchJoint;

    /**
     * Active joint for torch holding light
     */
    private Joint activeLightJoint;
    private Joint activeFireJoint;
    protected CollisionController contactListener;
    protected FireController fireController;
    protected SoundEngine soundEngine;

    protected ParticleEngine particleEngine;

    protected Fire torchFire;

    /**
     * Flag to add torch to avatar in update
     */
    private boolean queueAddTorch;

    /**
     * If torch is on the right of the avatar
     */
    private boolean torchOnRight;

    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * Returns true if debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @return true if debug mode is active.
     */
    public boolean isDebug( ) {
        return debug;
    }

    /**
     * Sets whether debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @param value whether debug mode is active.
     */
    public void setDebug(boolean value) {
        debug = value;
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

    /**
     * Returns true if this is the active screen
     *
     * @return true if this is the active screen
     */
    public boolean isActive( ) {
        return active;
    }

    /**
     * Returns the sprite batch associated with this scene
     *
     * The canvas is shared across all scenes.
     *
     * @return the sprite batch associated with this scene
     */
    public SpriteBatch getSpriteBatch() {
        return batch;
    }

    /**
     * Sets the sprite batch associated with this scene
     *
     * The sprite batch is shared across all scenes.
     *
     * @param batch the sprite batch associated with this scene
     */
    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    /**
     * Creates a new game world from the given asset directory
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates. The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * All assets for this current scene are assumed to be prefixed by the
     * given string.
     *
     * @param directory The asset directory defining this scene
     * @param prefix    The prefix for the asset keys
     */
    protected GameplayScene(AssetDirectory directory, SoundEngine soundEngine, String  prefix) {
        this.directory = directory;
        constants = directory.getEntry(prefix+"-constants",JsonValue.class);
        JsonValue defaults = constants.get("world");
        fireController = new FireController();
        contactListener = new CollisionController(directory, fireController);
        this.soundEngine = soundEngine;

        // pull out sounds
        volume = constants.getFloat("volume", 1.0f);

        sensorFixtures = new ObjectSet<Fixture>();

        scale = new Vector2();
        bounds = new Rectangle(0,0,defaults.get("bounds").getFloat( 0 ), defaults.get("bounds").getFloat( 1 ));
        resize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());

        displayFont = directory.getEntry( "shared-retro" ,BitmapFont.class);
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

        complete = false;
        failed = false;
        debug  = false;
        active = false;
        countdown = -1;
    }

    /**
     * Disposes of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        if (world != null) {
            for(ObstacleSprite sprite : sprites) {
                Obstacle obj = sprite.getObstacle();
                obj.deactivatePhysics(world);
            }
        }

        soundEngine.dispose();

        sprites.clear();
        addQueue.clear();
        world.dispose();
        addQueue = null;
        sprites = null;
        bounds = null;
        scale = null;
        world = null;
        batch = null;
    }

    /**
     *
     * Adds a physics sprite in to the insertion queue.
     *
     * Objects on the queue are added just before collision processing. We do
     * this to control object creation.
     *
     * param sprite The sprite to add
     */
    public void addQueuedObject(ObstacleSprite sprite) {
        assert inBounds(sprite) : "Object is not in bounds";
        addQueue.add(sprite);
    }

    /**
     * Immediately adds a physics sprite to the physics world
     *
     * param sprite The sprite to add
     */
    protected void addSprite(ObstacleSprite sprite) {
        assert inBounds(sprite) : "Sprite is not in bounds";
        sprites.add(sprite);
        sprite.getObstacle().activatePhysics(world);
    }

    /**
     * Immediately adds a sprite group to the physics world
     *
     * param group  The sprite group to add
     */
    protected void addSpriteGroup(ObstacleGroup group) {
        for(ObstacleSprite sprite : group.getSprites()) {
            assert inBounds( sprite ) : "Sprite is not in bounds";
            sprites.add( sprite );
        }
        group.activatePhysics(world);
    }

    /**
     * Returns true if the sprite is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param sprite    The sprite to check.
     *
     * @return true if the sprite is in bounds.
     */
    public boolean inBounds(ObstacleSprite sprite) {
        Obstacle obj = sprite.getObstacle();
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    /**
     * Resets the status of the game so that we can play again.
     *
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
        //TODO: improve above

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
        world.setContactListener(contactListener);
        setComplete(false);
        setFailure(false);
        loadLevel(levelName);
    };

    public void clearLevel() {
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
        world.setContactListener(contactListener);
        setComplete(false);
        setFailure(false);
    }

    private void populateLevel() {}

    public void loadLevel(String levelName) {
        this.levelName = levelName;
        float units = height / bounds.height;

        JsonValue levelData = directory.getEntry(levelName,JsonValue.class);

        // Create ground pieces
        Texture texture = directory.getEntry( "shared-earth", Texture.class );
        enemies = new ArrayList<>();

        Surface wall;
        String wname = "wall";
        JsonValue walls = levelData.get("walls");
        JsonValue walljv = walls.get("positions");
        for (int ii = 0; ii < walljv.size; ii++) {
            wall = new Surface(walljv.get(ii).asFloatArray(), units, walls);
            wall.getObstacle().setName(wname + ii);
            wall.setTexture(texture);
            addSprite(wall);
        }

        // Create walls and platforms
        JsonValue platforms = levelData.get("platforms");
        for (JsonValue platformJson : platforms) {
            Surface platform = new Surface(platformJson.get("positions").asFloatArray(), units, platformJson);
            platform.getObstacle().setName(platformJson.getString("name"));
            platform.setTexture(texture);
            addSprite(platform);
        }

        // Add level goal
        texture = directory.getEntry( "shared-goal", Texture.class );

        JsonValue goal = levelData.get("goal");
        goalDoor = new Door(units, goal);
        goalDoor.setTexture( texture );
        goalDoor.getObstacle().setName("goal");
        addSprite(goalDoor);

        // Create rope bridges
        texture = directory.getEntry( "platform-rope", Texture.class );
        JsonValue bridges = levelData.get("bridges");
        for (JsonValue bridgeJson : bridges) {
            RopeBridge bridge = new RopeBridge(units, bridgeJson);
            bridge.setTexture(texture);
            addSpriteGroup(bridge);
        }

        // Create Ropes
        texture = directory.getEntry( "platform-rope-end", Texture.class );
        Texture middle_texture = directory.getEntry( "platform-rope-mid", Texture.class );
        JsonValue ropes = levelData.get("ropes");
        for (JsonValue ropeJson : ropes) {
            if (ropeJson.getInt("type") == 1) {
                Vector2 pin1 = new Vector2(ropeJson.get("pin1").getFloat(0), ropeJson.get("pin1").getFloat(1));
                Vector2 pin2 = new Vector2(ropeJson.get("pin2").getFloat(0), ropeJson.get("pin2").getFloat(1));
                float dep = ropeJson.getFloat("depth");
                Rope rope = new Rope(pin1, pin2, dep, units, ropeJson);
                rope.setTextures(texture, middle_texture);
                addSpriteGroup(rope);
            } else {
                Vector2 pin1 = new Vector2(ropeJson.get("pin1").getFloat(0), ropeJson.get("pin1").getFloat(1));
                boolean bottom = ropeJson.getBoolean("bottom");
                float len = ropeJson.getFloat("len");
                Rope rope = new Rope(pin1, bottom, len, units, ropeJson);
                rope.setTextures(texture, middle_texture);
                addSpriteGroup(rope);
            }
        }

        // Create spinners
        texture = directory.getEntry( "platform-barrier", Texture.class );
        JsonValue spinners = levelData.get("spinners");
        for (JsonValue spinnerJson : spinners) {
            Spinner spinner = new Spinner(units, spinnerJson);
            spinner.setTexture(texture);
            addSpriteGroup(spinner);
        }

        // Create Traci
        texture = directory.getEntry("platform-player", Texture.class);
        avatar = new Traci(units, levelData.get("traci"));
        avatar.setTexture(texture);
        addSprite(avatar);
        // Have to do after body is created
        avatar.createSensor();

        Light l = new Light(units, levelData.get("light"));
        l.setTexture(texture);
        addSprite(l);
        l.createSensor();
        torchFire = new Fire(units, new Vector2(10,10));
        addSprite(torchFire);

        particleEngine = new ParticleEngine(torchFire);
        particleEngine.newFires(fireController);
//
        // Create Torch
        texture = directory.getEntry("platform-torch", Texture.class);
        torch = new Torch(units, constants.get("torch"));
        torch.setTexture(texture);
        addSprite(torch);
        l.getObstacle().setPosition(torch.getObstacle().getPosition());
        torchFire.getObstacle().setPosition(torch.getObstacle().getPosition());
        activeLightJoint = world.createJoint(torch.attachObj(l));
        activeFireJoint = world.createJoint(torch.attachObj(torchFire));
        // TODO: Optimize the above ^^

        JsonValue enemiesJson = levelData.get("enemies");

        // Create Totem
        texture = directory.getEntry("platform-totem01", Texture.class);
        JsonValue totemsJson = enemiesJson.get("totems").get("instances");
        for (int i = 0; i < totemsJson.size; i++) {
            Vector2 position = new Vector2(totemsJson.get(i).get("pos").getFloat(0), totemsJson.get(i).get("pos").getFloat(1));
            Totem totem = new Totem(i, units, totemsJson.get(i), directory, position);
            totem.setTexture(texture);
            addSprite(totem);
            totem.createSensor();
            enemies.add(totem);
        }

        // Create Moth
        texture = directory.getEntry("platform-moth01", Texture.class);
        JsonValue mothsJson = enemiesJson.get("moths").get("instances");
        for (int i = 0; i < mothsJson.size; i++) {
            Vector2 position = new Vector2(mothsJson.get(i).get("pos").getFloat(0), mothsJson.get(i).get("pos").getFloat(1));
            Moth moth = new Moth(i, units, mothsJson.get(i), directory, position);
            moth.setTexture(texture);
            addSprite(moth);
            moth.createSensor();
            enemies.add(moth);
        }


    }

    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time to switch to a
     * new game mode. If not, the update proceeds normally.
     *
     * @param dt    Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        InputController input = InputController.getInstance();
        input.sync(bounds, scale);
        if (listener == null) {
            return true;
        }

        // Toggle debug
        if (input.didDebug()) {
            debug = !debug;
        }

        // Handle resets
        if (input.didReset()) {
            reset();
        }

        // Now it is time to maybe switch screens.
        if (input.didExit()) {
            pause();
            listener.exitScreen(this, EXIT_QUIT);
            return false;
        } else if (input.didAdvance()) {
            pause();
            listener.exitScreen(this, EXIT_NEXT);
            return false;
        } else if (input.didRetreat()) {
            pause();
            listener.exitScreen(this, EXIT_PREV);
            return false;
        } else if (countdown > 0) {
            countdown--;
        } else if (countdown == 0) {
            if (failed) {
                reset();
            } else if (complete) {
                pause();
                listener.exitScreen(this, EXIT_NEXT);
                return false;
            }
        }

        contactListener.processPendingMerges();

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
     *
     * This method contains the specific update code for this mini-game. It
     * does not handle collisions, as those are managed by the parent class
     * PhysicsScene. This method is called after input is synced to the current
     * frame, but before collisions are resolved. The very last thing that it
     * should do is apply forces to the appropriate objects.
     *
     * @param dt    Number of seconds since last animation frame
     */
    public void update(float dt) {
        soundEngine.tendToMusicLoop();
        supplementaryCollisionActions();
        supplementaryFireActions();
        if (enemies != null) {
            for (Enemy e : enemies) {
                e.update();
            }
        }
        torch.update();
        fireController.update();
        contactListener.sustainedContact();

        InputController input = InputController.getInstance();

        // Process actions in object model
        avatar.setMovement(input.getHorizontal() * avatar.getForce());
        soundEngine.avatarWalking(input.getHorizontal(), avatar.isGrounded());
        avatar.setJumping(input.didPrimary());
        avatar.setShooting(input.didSecondary());

        // Add a bullet if we fire
        /*if (avatar.isShooting()) {
            createBullet();
        }*/

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
            soundEngine.jump();
        }
        if ((queueAddTorch && activeTorchJoint == null) || (activeTorchJoint != null &&
            torchOnRight != avatar.isFacingRight())) {
            joinTorchtoAvatar();
        }
        updateCamera();
    }

    private void updateCamera() {
        Vector2 playerPos = avatar.getObstacle().getPosition();
        float playerPixelX = playerPos.x * scale.x;
        float playerPixelY = playerPos.y * scale.y;

        float lerp = 0.3f;
        camera.position.x += (playerPixelX - camera.position.x) * lerp;
        camera.position.y += (playerPixelY - camera.position.y) * lerp;

        float effectiveWidth = camera.viewportWidth * camera.zoom;
        float effectiveHeight = camera.viewportHeight * camera.zoom;
        float halfWidth = effectiveWidth / 2f;
        float halfHeight = effectiveHeight / 2f;

        float minXPixel = bounds.x * scale.x;
        float maxXPixel = (bounds.x + bounds.width) * scale.x;
        float minYPixel = bounds.y * scale.y;
        float maxYPixel = (bounds.y + bounds.height) * scale.y;

        camera.position.x = MathUtils.clamp(camera.position.x, minXPixel + halfWidth, maxXPixel - halfWidth);
        camera.position.y = MathUtils.clamp(camera.position.y, minYPixel + halfHeight, maxYPixel - halfHeight);

        camera.update();
    }

    private void supplementaryCollisionActions() {
        Stack<Object[]> todos = contactListener.getCollisionFlags();
        while ( !todos.isEmpty() ) {
            Object[] todo_action = todos.pop();
            switch ((String) todo_action[0]) {
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
                    if (((EnhancedObstacleSprite) todo_action[1]).getObstacle().getBody() == null) {
                        break;
                    }
                    joinFireToObject((ObstacleSprite) todo_action[1], fire);
                    break;
                case "expireObj":
                    for (Fire f : (ArrayList<Fire>) todo_action[2]) {
                        if (f.getFixtureJoint() != null) {
                            world.destroyJoint(f.getFixtureJoint());
                            f.setFixtureJoint(null);
                        }
                        f.dispose();
                    }
                    ((EnhancedObstacleSprite) todo_action[1]).getObstacle().markRemoved(true);
                    fireController.cleanObj((EnhancedObstacleSprite) todo_action[1]);
                    break;
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

    private void joinFireToObject (ObstacleSprite o, Fire f) {
        WeldJointDef jointDef = new WeldJointDef();
        jointDef.initialize(o.getObstacle().getBody(), f.getObstacle().getBody(), Vector2.Zero);
        jointDef.collideConnected = false;
        Joint joint = world.createJoint(jointDef);
        f.setFixtureJoint(joint);
    }

    /**
     * Processes the physics for this frame
     *
     * Once the update phase is over, but before we draw, we are ready to
     * process physics. The primary method is the step() method in world. This
     * implementation works for all applications and should not need to be
     * overwritten.
     *
     * @param dt    Number of seconds since last animation frame
     */
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
        ScreenUtils.clear(0.17f, 0.28f, 0.35f, 1.0f);

        // This shows off how powerful our new SpriteBatch is
        batch.begin(camera);


        // Draw the meshes (images)
        for(ObstacleSprite obj : sprites) {
            obj.draw(batch);
        }

        if (fireController.getLitFires().size()!=0){
            System.out.println("not FIRE!");
            for (Fire fire:fireController.getLitFires()){
                particleEngine.draw(batch,fire);
            }
        }
        particleEngine.draw(batch,torchFire);



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
            camera.zoom = 0.8f;
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
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application
     * is also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub
        SoundEffectManager sounds = SoundEffectManager.getInstance();
        sounds.stop("plop");
        sounds.stop("fire");
        sounds.stop("jump");
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

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

}
