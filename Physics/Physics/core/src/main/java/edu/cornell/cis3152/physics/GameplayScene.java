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

import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.viewport.FitViewport;
import edu.cornell.cis3152.physics.level_player.CollisionController;
import edu.cornell.cis3152.physics.level_player.EventHandler;
import edu.cornell.cis3152.physics.level_player.FireController;
import edu.cornell.cis3152.physics.level_player.LightController;
import edu.cornell.cis3152.physics.level_player.enemies.Enemy;
import edu.cornell.cis3152.physics.level_player.enemies.Moth;
import edu.cornell.cis3152.physics.level_player.enemies.Totem;
import edu.cornell.cis3152.physics.level_player.player.Torch;
import edu.cornell.cis3152.physics.level_player.player.Avatar;
import edu.cornell.cis3152.physics.level_player.player.Avatar.GroundState;
import edu.cornell.cis3152.physics.level_player.utils.CollisionFlag;
import edu.cornell.cis3152.physics.level_player.utils.EventAction;
import edu.cornell.cis3152.physics.level_player.utils.Event;
import edu.cornell.cis3152.physics.level_player.utils.FireFlag;
import edu.cornell.cis3152.physics.level_player.utils.ObstacleGroup;

import edu.cornell.cis3152.physics.level_player.utils.TweenElement;

import java.util.*;

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
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
    protected Avatar avatar;
    protected Torch torch;

    /** Reference to the goalDoor (for collision detection) */
    private GameObject goalDoor;

    //protected Totem totem;
    //protected Moth moth;

    private String levelName = "example_level";

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
    protected EventHandler eventHandler;
    protected SoundEngine soundEngine;
    protected HashSet<Rune> runeSet;
    protected PooledList<TweenElement<Float>> tweenedMovmentObjectsFloat;
    protected PooledList<TweenElement<Vector2>> tweenedMovmentObjectsVec2;
    protected FitViewport fitViewport;

    protected LightController lightController;
    protected ShapeRenderer shapeRenderer;

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
     * floating lights in the level, place-holder for now
     * three default lights:
     * 1. light for the level door (doesn't move)
     * 2. light for center of the room (doesn't move)
     * 3. light for the far end of the room (moves between the far end and this end)
     */
    protected FloatingLight[] floatingLights = new FloatingLight[3];

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
     * Sets the sprite batch associated with this scene
     *
     * The sprite batch is shared across all scenes.
     *
     * @param batch the sprite batch associated with this scene
     */
    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
    }
    private float phyiscsUnits;

    /**
     * the constant list of items used to repersent the torch throw arc parabola
     * offset is an internal timer used for animating it
     */
    private ArrayList<ObstacleSprite> torchArc;
    private float dtTorchArcOffset = 0;

    private int dotTorchArcCount = 120;
    private int deltaTorchArc = 4;
    private float animationOffsetTorchArc = .18f;


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
        fireController = new FireController(directory);
        contactListener = new CollisionController(directory, fireController);
        this.eventHandler = new EventHandler();
        this.soundEngine = soundEngine;
        torchArc = new ArrayList<>();
        tweenedMovmentObjectsVec2 = new PooledList<>();
        tweenedMovmentObjectsFloat = new PooledList<>();
        this.shapeRenderer = new ShapeRenderer();
        runeSet = new HashSet<>();

        this.fitViewport = new FitViewport(16, 9);

        // pull out sounds
        volume = constants.getFloat("volume", 1.0f);

        sensorFixtures = new ObjectSet<Fixture>();

        scale = new Vector2();
        //TODO: Value needs to be imported from level vvvv
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
        lightController.dispose();
        eventHandler.dispose();
        sprites.clear();
        addQueue.clear();
        torchArc.clear();
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

        if (eventHandler != null) {
            eventHandler.dispose();
        }

        if (lightController != null){
            lightController.dispose();}

        for (ObstacleSprite sprite : sprites) {
            Obstacle obj = sprite.getObstacle();
            sprite.getObstacle().deactivatePhysics(world);
        }
        sprites.clear();
        addQueue.clear();
        runeSet.clear();
        if (world != null) {
            world.dispose();
        }

        world = new World(gravity, false);
        world.setContactListener(contactListener);
        setComplete(false);
        setFailure(false);
        loadLevel(levelName, "rope_test");
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

        for (ObstacleSprite s : torchArc) {
            s.getObstacle().markRemoved(true);
        }
        torchArc.clear();
        runeSet.clear();

        if (this.shapeRenderer == null) {
            this.shapeRenderer = new ShapeRenderer();
        }
    }

    private void populateLevel() {}
    private Rope temp;

    private List<float[]> extractSurfaces(int[] data, int cols, int rows) {
        List<float[]> surfaces = new ArrayList<>();
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                int yy = rows - y;
                int index = y * cols + x;
                int tileId = data[index];
                if (tileId != 0) { // Skip empty tiles
                    float[] polygon = new float[]{
                        x, yy,                         // top-left
                        x, yy - 1,                // bottom-left
                        x + 1, yy - 1,        // bottom-right
                        x + 1, yy
                    };
                    surfaces.add(polygon);
                }
            }
        }
        return surfaces;
    }

    public void loadLevel(String levelName, String levelInfoName) {
        this.levelName = levelName;
        float units = height / bounds.height;
        phyiscsUnits = units;

        JsonValue levelData = directory.getEntry(levelName,JsonValue.class);
        JsonValue levelInfo = directory.getEntry(levelInfoName, JsonValue.class);

        // Create ground pieces
        Texture texture;
        enemies = new ArrayList<>();

        JsonValue layers = levelData.get("layers");
        for (JsonValue layer : layers) {
            String layerType = layer.getString("type");
            String layerName = layer.getString("name");

            if (layerType.equals("tilelayer")) {
                int width = layer.getInt("width");
                int height = layer.getInt("height");
                System.out.println(width + "x" + height);
                JsonValue data = layer.get("data");

                JsonValue settings = levelInfo.get("walls");
                int[] tileData = new int[width * height];
                for (int i = 0; i < data.size; i++) {
                    tileData[i] = data.getInt(i);
                }

                List<float[]> polygons = extractSurfaces(tileData, width, height);

                for (float[] points : polygons) {
//                    System.out.println(Arrays.toString(points));
                    // Determine bounds of the polygon in tile coordinates
                    int minX = (int) points[0];
                    int maxY = (int) points[1];
                    int maxX = (int) points[4];
                    int minY = (int) points[5];
                    int x = minX;
                    int y = height - maxY;
                    int index = y * width + x;
                    int tileId = tileData[index];
                    String name;
                    if (tileId == 2 || tileId == 3 || tileId == 4) {
                        name = "platform";
                    } else if (tileId == 8 || tileId == 9 || tileId == 10) {
                        name = "platform";
                    } else {
                        name = "wall";
                    }

                    float tileunits = units/300f;
                    Surface tile = new Surface(points, units, name, settings);
                    Texture textur = directory.getEntry("stoneTile"+(tileId), Texture.class);
                    textur.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

                    tile.setTexture(textur);


                    addSprite(tile);
                }
            } else if (layerType.equals("objectgroup")) {
                Map<String, JsonValue> ropeAnchors = new HashMap<>();
                Map<Integer, ObstacleSprite> indexedPlatforms = new HashMap<>();
                for (JsonValue object : layer.get("objects")) {
                    String objName = object.getString("name", "unnamed");
                    float x = object.getFloat("x") / levelData.getInt("tilewidth");
                    float y = (18 * 300 - object.getFloat("y")) / levelData.getInt("tileheight");
                    float[] pos = new float[]{x, y};

                    switch (objName) {
                        case "player":
                            Texture playerTexture = directory.getEntry("platform-player", Texture.class);
                            avatar = new Avatar(directory, units, levelInfo.get("traci"));
                            avatar.setTexture(playerTexture);
                            avatar.getObstacle().setPosition(pos[0], pos[1]);
                            //System.out.println("position" + pos[0] + " " + pos[1]);
                            addSprite(avatar);
                            avatar.createSensor();
                            if(lightController!= null){
                                lightController.attachPlayerLight(avatar);
                            }
                            break;

                        case "torch":
                            Lighting l = new Lighting(units, levelInfo.get("light"));
                            addSprite(l);
                            l.createSensor();
                            torchFire = new Fire(units, new Vector2(10,10));
                            addSprite(torchFire);
                            lightController = new LightController(torchFire.getObstacle().getPosition(),world,camera,bounds, units);
                            lightController.attachTorchLight(torchFire);
                            lightController.resetCamera(camera.position.x,camera.position.y);
                            lightController.attachPlayerLight(avatar);

                            particleEngine = new ParticleEngine(torchFire, units);
                            particleEngine.newFires(fireController);

                            texture = directory.getEntry("platform-torch", Texture.class);
                            torch = new Torch(units, constants.get("torch"));
                            torch.getObstacle().setPosition(pos[0], pos[1]);
                            torch.setTexture(texture);
                            torch.setMaterial(new ObstacleMaterial("torch", null));
                            addSprite(torch);
                            l.getObstacle().setPosition(torch.getObstacle().getPosition());
                            torchFire.getObstacle().setPosition(new Vector2(torch.getObstacle().getPosition().cpy().add(0,torch.getHeight() / 5)));
                            fireController.forceAddTorchFire(torchFire, torch, new Vector2(torch.getObstacle().getPosition().cpy().add(0,torch.getHeight() / 4)));
                            activeLightJoint = world.createJoint(torch.attachObj(l));
                            activeFireJoint = world.createJoint(torch.attachObj(torchFire));

                            //floating lights
                            floatingLights = new FloatingLight[3];
                            Vector2 goalPos = goalDoor.getObstacle().getPosition();
                            FloatingLight goalLight = new FloatingLight(units,goalPos,1,goalPos);
                            floatingLights[0] = goalLight;
                            Vector2 centerPos = new Vector2(bounds.width/2,bounds.height/2);
                            FloatingLight centerLight = new FloatingLight(units,centerPos,1,centerPos);
                            floatingLights[1] = centerLight;
                            Vector2 edgePos1 = new Vector2(bounds.width-5,5);
                            Vector2 edgePos2 = new Vector2(5,5);
                            FloatingLight edgeLight = new FloatingLight(units,edgePos1,1,edgePos2);
                            floatingLights[2] = edgeLight;
                            for (int i = 0; i <floatingLights.length; i++){
                                addSprite(floatingLights[i]);}
                            for (FloatingLight floatingLight: floatingLights) {
                                lightController.attachAmbientLight(floatingLight);
                            }
                            break;

                        case "moth":
                            texture = directory.getEntry("platform-moth01", Texture.class);
                            Vector2 position = new Vector2(pos[0], pos[1]);
                            Moth moth = new Moth(1, units, levelInfo.get("enemies").get("moths").get("instances").get(0), directory, position);
                            moth.setTexture(texture);
                            addSprite(moth);
                            moth.createSensor();
                            enemies.add(moth);
                            break;

                        case "totem":
                            texture = directory.getEntry("platform-totem01", Texture.class);
                            position = new Vector2(pos[0], pos[1]);
                            Totem totem = new Totem(1, units, levelInfo.get("enemies").get("totems").get("instances").get(0), directory, position);
                            totem.setTexture(texture);
                            addSprite(totem);
                            totem.createSensor();
                            enemies.add(totem);
                            break;

                        case "goaldoor":
                            texture = directory.getEntry("shared-goal", Texture.class);
                            System.out.println("goal door texture: " + texture);
                            float size = 1f;

                            GameObject goalDoor = new GameObject(x,y,size*1.47f,size,units, true);
                            goalDoor.getObstacle().setSensor(true);
                            goalDoor.getObstacle().setBodyType(BodyType.StaticBody);
                            goalDoor.getObstacle().setName("goalDoor");
                            goalDoor.setTexture(texture);
                            goalDoor.getObstacle().setPhysicsUnits(units);
                            this.goalDoor = goalDoor;
                            addSprite(goalDoor);
                            break;

                        /*case "window":
                            System.out.println("window");
                            texture = directory.getEntry("window", Texture.class);
                            float width = object.getFloat("width") / levelData.getInt("tilewidth");
                            float height = object.getFloat("height") / levelData.getInt("tileheight");

                            // Define rectangle points (counter-clockwise)
                            float[] points = new float[]{
                                -width / 2f, height / 2f,
                                -width / 2f, -height / 2f,
                                width / 2f, -height / 2f,
                                width / 2f, height / 2f
                            };

                            GameObject decoration = new GameObject(points, pos[0], pos[1], units);
                            decoration.getObstacle().setSensor(true);  // set as sensor
                            decoration.setTexture(texture);
                            decoration.getObstacle().setName(objName);

                            addSprite(decoration);
                            break;*/

                        case "rune":
                            float platWidth = 1f;
                            float platHeight = 1f;
                            List<Float> platformPos = new ArrayList<>();
                            float[] thresholds = null;

                            JsonValue runeProperties = object.get("properties");
                            for (JsonValue prop : runeProperties) {
                                String propName = prop.getString("name");
                                String value = prop.getString("value");
                                switch (propName) {
                                    case "platformWidth":
                                        platWidth = Float.parseFloat(value);
                                        break;
                                    case "platformHeight":
                                        platHeight = Float.parseFloat(value);
                                        break;
                                    case "platformPosition":
                                        for (String num : value.split(",")) {
                                            platformPos.add(Float.parseFloat(num));
                                        }
                                        break;

                                    case "thresholds":
                                        String[] tokens = value.split(",");
                                        thresholds = new float[tokens.length];
                                        for (int i = 0; i < tokens.length; i++) {
                                            thresholds[i] = Float.parseFloat(tokens[i].trim());
                                        }
                                        break;
                                }
                            }

                            Rune runeTemp = new Rune(pos[0],pos[1], units, thresholds);
                            BoxObstacle tmp = new BoxObstacle(platformPos.get(0), platformPos.get(1), platWidth, platHeight);
                            tmp.setPhysicsUnits(units);
                            tmp.setName("floor");
                            tmp.setBodyType(BodyType.KinematicBody);
                            tmp.setFriction(0.5f);

                            ObstacleSprite plat = new ObstacleSprite(tmp);
                            plat.getObstacle().setBodyType(BodyType.KinematicBody);
                            plat.getObstacle().setFriction(.5f);
                            addSprite(plat);
//                            EventAction<Float> awef = new EventAction<Float>(plat, "rotate", plat.getObstacle().getAngle(), (float) Math.PI);
                            EventAction<Vector2> mo = new EventAction<Vector2>(plat, "move", new Vector2(8,10), new Vector2(16,10));
//                            runeTemp.registerEventAction(awef);
                            runeTemp.registerEventAction(mo);
                            runeSet.add(runeTemp);
                            addSprite(runeTemp);
                            break;

                        case "button":
                            float rotationRad = 0f;
                            boolean latch = false;
                            boolean doubleSided = false;
                            boolean hasMoveEvent = false;
                            boolean hasRotateEvent = false;
                            float startX = 0f, startY = 0f;
                            float endX = 0f, endY = 0f;
                            float startDegree = 0f;
                            float endDegree = 0f;
                            float duration = 1f;
                            int platformIndex = -1;
                            float platformWidth = 1f;
                            float platformHeight = 1f;
                            String interpolation = "";

                            JsonValue properties = object.get("properties");
                            for (JsonValue prop : properties) {
                                String propName = prop.getString("name");
                                String value = prop.getString("value");
                                switch (propName) {
                                    case "latch":
                                        latch = Boolean.parseBoolean(value);
                                        break;
                                    case "doublesided":
                                        doubleSided = Boolean.parseBoolean(value);
                                        break;
                                    case "rotationRadians":
                                        rotationRad = Float.parseFloat(value);
                                        break;
                                    case "rotateEvent":
                                        hasRotateEvent = Boolean.parseBoolean(value);
                                        break;
                                    case "moveEvent":
                                        hasMoveEvent = Boolean.parseBoolean(value);
                                        break;
                                    case "startX":
                                        startX = Float.parseFloat(value);
                                        break;
                                    case "startY":
                                        startY = Float.parseFloat(value);
                                        break;
                                    case "endX":
                                        endX = Float.parseFloat(value);
                                        break;
                                    case "endY":
                                        endY = Float.parseFloat(value);
                                        break;
                                    case "startDegree":
                                        startDegree = Float.parseFloat(value);
                                        break;
                                    case "endDegree":
                                        endDegree = Float.parseFloat(value);
                                        break;
                                    case "time":
                                        duration = Float.parseFloat(value);
                                        break;
                                    case "platformIndex":
                                        platformIndex = Integer.parseInt(value);
                                        break;
                                    case "platformWidth":
                                        platformWidth = Float.parseFloat(value);
                                        break;
                                    case "platformHeight":
                                        platformHeight = Float.parseFloat(value);
                                        break;
                                    case "interpolation":
                                        interpolation = value.toLowerCase();
                                        break;
                                }
                            }

                            Vector2 buttonPosition = new Vector2(pos[0], pos[1]);
                            Button button = new Button(buttonPosition, (float)Math.toRadians(rotationRad), doubleSided, latch, units);
                            addSpriteGroup(button);

                            ObstacleSprite platform = indexedPlatforms.get(platformIndex);
                            if (platform == null && platformIndex != -1) {
                                BoxObstacle temp = new BoxObstacle(startX, startY, platformWidth, platformHeight);
                                temp.setPhysicsUnits(units);
                                temp.setName("floor");
                                temp.setBodyType(BodyType.KinematicBody);
                                temp.setFriction(0.5f);

                                platform = new ObstacleSprite(temp);
                                platform.getObstacle().setBodyType(BodyType.KinematicBody);
                                platform.getObstacle().setFriction(.5f);
                                addSprite(platform);

                                indexedPlatforms.put(platformIndex, platform);
                            }

                            Function<Float, Float> movementFunc = Interpolation.smoother::apply;
                            switch (interpolation) {
                                case "linear":
                                    movementFunc = Interpolation.linear::apply;
                                    break;
                                case "smoother":
                                    movementFunc = Interpolation.smoother::apply;
                                    break;
                            }

                            if (hasMoveEvent) {
                                EventAction<Vector2> moveAction = new EventAction<>(platform, "move",
                                    new Vector2(startX, startY), new Vector2(endX, endY), duration, movementFunc);

                                Event<Integer, Vector2> moveEvent = new Event<>(button, button::getState,
                                    state -> state == 1, moveAction);
                                eventHandler.registerEvent(moveEvent);
                            }

                            if (hasRotateEvent) {
                                float fromRad = (float) Math.toRadians(startDegree);
                                float toRad = (float) Math.toRadians(endDegree);

                                EventAction<Float> rotateAction = new EventAction<>(platform, "rotate",
                                    fromRad, toRad, duration, movementFunc);

                                Event<Integer, Float> rotateEvent = new Event<>(button, button::getState,
                                    state -> state == 1, rotateAction);
                                eventHandler.registerEvent(rotateEvent);
                            }

                        default:
                            if (objName.matches("\\d+")) {
                                ropeAnchors.put(objName, object);
                            } else {
                                Texture temp = directory.getEntry(objName, Texture.class);
                                if (temp != null) {

                                    float width = object.getFloat("width") / levelData.getInt("tilewidth");
                                    float height = object.getFloat("height") / levelData.getInt("tileheight");

                                    GameObject decoration = new GameObject(x,y,width,height, units, true);
                                    decoration.getObstacle().setSensor(true);  // set as sensor
                                    decoration.getObstacle().setBodyType(BodyType.StaticBody);
                                    decoration.setTexture(temp);
                                    decoration.getObstacle().setName(objName);

                                    addSprite(decoration);
                                } else {
                                    System.out.println("Unknown object: " + objName);
                                }
                            }
                            break;
                    }
                }

                int rcnt = ropeAnchors.size()/2;
                for (int i = 0; i < rcnt; i++) {
                    String startName = String.valueOf(2 * i + 1);
                    String endName = String.valueOf(2 * i + 2);

                    JsonValue start = ropeAnchors.get(startName);
                    JsonValue end = ropeAnchors.get(endName);

                    if (start != null && end != null) {
                        float x1 = start.getFloat("x") / levelData.getInt("tilewidth");
                        float y1 = (18 * 300 - start.getFloat("y")) / levelData.getInt("tileheight");
                        float x2 = end.getFloat("x") / levelData.getInt("tilewidth");
                        float y2 = (18 * 300 - end.getFloat("y")) / levelData.getInt("tileheight");

                        int depth = 10, piecelen = 10, thickness = 5;
                        JsonValue props = end.get("properties");
                        if (props != null) {
                            for (JsonValue prop : props) {
                                String pname = prop.getString("name");
                                String val = prop.getString("value");
                                switch (pname) {
                                    case "depth":
                                        depth = Integer.parseInt(val);
                                        break;
                                    case "piecelen":
                                        piecelen = Integer.parseInt(val);
                                        break;
                                    case "thickness":
                                        thickness = Integer.parseInt(val);
                                        break;
                                }
                            }
                        }
                        texture = directory.getEntry( "platform-rope-end", Texture.class );
                        Texture middle_texture = directory.getEntry( "platform-rope-mid", Texture.class );
                        Rope rope = new Rope(new Vector2(x1, y1), new Vector2(x2, y2), depth, thickness, piecelen, units, levelInfo.get("ropes").get(0));
                        rope.setTextures(texture, middle_texture);
                        //temp = rope;
                        addSpriteGroup(rope);
                    }

                }
            }
        }


        /*Surface wall;
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
            Vector2 pin1 = new Vector2(ropeJson.get("pin1").getFloat(0), ropeJson.get("pin1").getFloat(1));
            Vector2 pin2 = new Vector2(ropeJson.get("pin2").getFloat(0), ropeJson.get("pin2").getFloat(1));
            float dep = ropeJson.getFloat("depth");
            Rope rope = new Rope(pin1, pin2, dep, units, ropeJson);
            rope.setTextures(texture, middle_texture);
            addSpriteGroup(rope);
            temp = rope;
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
        avatar = new Avatar(directory, units, levelData.get("traci"));
        avatar.setTexture(texture);
        addSprite(avatar);
        // Have to do after body is created
        avatar.createSensor();

        Lighting l = new Lighting(units, levelData.get("light"));
        l.setTexture(texture);
        addSprite(l);
        l.createSensor();
        torchFire = new Fire(units, new Vector2(10,10));
        torchFire.setID(0);
        addSprite(torchFire);
        lightController = new LightController(torchFire.getObstacle().getPosition(),world,camera,bounds);
        lightController.attachTorchLight(torchFire);
        lightController.resetCamera(camera.position.x,camera.position.y);
//        lightController.fireLights(fireController);

        particleEngine = new ParticleEngine(torchFire);
        particleEngine.newFires(fireController);

//
        // Create Torch
        texture = directory.getEntry("platform-torch", Texture.class);
        torch = new Torch(units, constants.get("torch"));
        torch.setTexture(texture);
        torch.setMaterial(new ObstacleMaterial("torch", null));
        addSprite(torch);
        l.getObstacle().setPosition(torch.getObstacle().getPosition());
        torchFire.getObstacle().setPosition(torch.getObstacle().getPosition().cpy().add(0,torch.getHeight() / 4));
        activeLightJoint = world.createJoint(torch.attachObj(l));
        activeFireJoint = world.createJoint(torch.attachObj(torchFire));
        fireController.forceAddTorchFire(torchFire, torch, new Vector2(torch.getObstacle().getPosition().cpy().add(0,torch.getHeight() / 4)) );
//        torchFire.setFixtureJoint(activeFireJoint);
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
        }*/

        torchArc = new ArrayList<>();
        for (int i = 0; i < dotTorchArcCount / deltaTorchArc; i++) {
            WheelObstacle temp = new WheelObstacle(-1,-1, 0.4f/32f * units);
            temp.setBodyType(BodyType.StaticBody);
            ObstacleSprite tracker = new ObstacleSprite(temp);
            tracker.getObstacle().setPhysicsUnits(phyiscsUnits);
            tracker.getObstacle().setName("trajectoryPoint");
            tracker.getObstacle().setSensor(true);
            tracker.getObstacle().setPhysicsUnits(units);
            addSprite(tracker);
            torchArc.add(tracker);
        }

//        RainBlock rainBlocktemp = new RainBlock(3,3,10,10,units, torchFire.getRadius());
//        addSprite(rainBlocktemp);

        /*if (levelName.equals("rope_test")) {
            // SAMPLE BUTTON CODE BELOW::
            Button button = new Button(new Vector2(24,2.75f), 0, true, true, units);
            Button button2 = new Button(new Vector2(30.75f,7f), (float) Math.PI/2, false, false, units);
            BoxObstacle temp = new BoxObstacle(5,5,5,.5f);
            temp.setPhysicsUnits(units);
            temp.setName("floor");
            ObstacleSprite thing = new ObstacleSprite(temp);
            thing.getObstacle().setBodyType(BodyType.KinematicBody);
            thing.getObstacle().setFriction(.5f);
            addSprite(thing);

    //        Array<Object> actionArray = new Array<>(new Object[]{thing, "move", new Vector2(8, 10), new Vector2(10, 10)});
            Function<Float, Float> movementFunc = Interpolation.swing::apply;
            EventAction<Vector2> eventAction = new EventAction<Vector2>(thing, "move", new Vector2(8, 10), new Vector2(16, 5), 4f, movementFunc);
    //        Object[] actionArray = new Object[]{thing, "rotate", 0f, (float) (Math.PI), 300, movementFunc};
    //        Object[] actionArray = new Object[]{thing, "move", new Vector2(8, 10), new Vector2(16, 5), 100, movementFunc};

            Event<Integer,Vector2> event = new Event<Integer,Vector2>(button, button::getState,
                state -> state == 1,  eventAction);
            eventHandler.registerEvent(event);*/



//        }

//        if (levelName.equals("moth_intro")) {
//            temp.deactivateAnchor(1);
//        }
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
        updateRunes(dt);
        supplementaryCollisionActions();
        supplementaryFireActions();
        supplementaryEventActions(dt);
        updateTweenedMovementObjectsVec2(dt);
        updateTweenedMovementObjectsFloat(dt);

        if (temp != null) {
//            for (EnhancedObstacleSprite eos : temp.getTopEntities()) {
//                System.out.println(eos.getObstacle().getAngle());
//            }
        }
//        System.out.println("-----------");

        if (enemies != null) {
            for (Enemy e : enemies) {
                e.update();
            }
        }

        torch.update();
        fireController.update();
        eventHandler.update();
        contactListener.sustainedContact();

        InputController input = InputController.getInstance();

        // Process actions in object model
        avatar.setMovement(new Vector2(input.getHorizontal() * avatar.getForce(), input.getVertical() * avatar.getForce()));
        soundEngine.avatarWalking(input.getHorizontal(), avatar.getGroundedState().equals(GroundState.GROUNDED));
        avatar.setJumping(input.didPrimary());
        avatar.setShooting(input.didSecondary());

        if (!(avatar.getBodyTouchedClimbables().isEmpty()) && !avatar.getHasTorch() && !avatar.getGroundedState().equals(GroundState.CLIMBING)
             && input.didVertical()) {
            avatar.setGroundedState(GroundState.CLIMBING);
            avatar.getObstacle().getBody().setLinearVelocity(Vector2.Zero);
            avatar.applyClimbingPhysics();
        }

        if (avatar.getGroundedState().equals(GroundState.CLIMBING) && avatar.getBodyTouchedClimbables().isEmpty()) {
            avatar.setGroundedState(GroundState.AIRBORNE);
            avatar.removeClimbingPhysics();
        }

        if (input.getThrowing() && avatar.getHasTorch() && activeTorchJoint != null) {
            avatar.setHasTorch(false);
            world.destroyJoint(activeTorchJoint);
            activeTorchJoint = null;
            torch.applyThrowForce(avatar.isFacingRight() ? 1 : -1);
            torch.resetPickUp();
            torch.getObstacle().setSensor(false);
            soundEngine.throwTorch();
        }
        if (input.assistParabola()) {
            generateTorchArc();
        } else {
            hideArc();
        }

        avatar.applyForce();

        if (avatar.isJumping()) {
            avatar.setGroundedState(GroundState.AIRBORNE);
            SoundEffectManager sounds = SoundEffectManager.getInstance();
            soundEngine.jump();
        }

        if ((queueAddTorch && activeTorchJoint == null) || (activeTorchJoint != null &&
            torchOnRight != avatar.isFacingRight())) {
            joinTorchtoAvatar();
        }

//        for (ObstacleSprite sprite : sprites) {
//            if (!sprite.getObstacle().isRemoved() && sprite instanceof Smoke) {
//                sprite.getObstacle().getBody().applyForceToCenter(new Vector2(1f,0f), true);
//            }
//        }

        updateCamera();
    }


    private void generateTorchArc() {
        dtTorchArcOffset %= deltaTorchArc;

        if (!avatar.getHasTorch()) {
            hideArc();
            return;
        }

        float dt = 1/60f;
        Vector2 start = new Vector2(torch.getObstacle().getPosition());
        // magic numbers but idk why they work
        Vector2 vel = new Vector2(torch.getIntialThrowVelocity()).scl(1.05f * (avatar.isFacingRight() ? 1 : -1),2.1f);
        float gravity = world.getGravity().y;
        final Fixture[] hit = { null };
        final Vector2[] hitpoint = {new Vector2()};
        RayCastCallback raycastCallback = (fixture, point, normal, fraction) -> {
            hit[0] = fixture;
            hitpoint[0] = point;
            return 0;
        };
        Vector2 lastTP = new Vector2(start);

        ArrayList<Vector2> arcPoints = new ArrayList<Vector2>();
        float r=dt * dtTorchArcOffset;
        int generatedCount = 0;
        while (generatedCount < dotTorchArcCount) {
            float t = (dt) * (generatedCount + dtTorchArcOffset);
            float x = start.x + vel.x * t;
            float y = start.y + t * vel.y + 0.5f * (t * t + t) * gravity;
            Vector2 trajectoryPosition = new Vector2(x,y);
            if (generatedCount > 0) {
                world.rayCast(raycastCallback, lastTP, trajectoryPosition);
                if (hit[0] != null && !hit[0].isSensor()) {
                    arcPoints.add(hitpoint[0]);
                    break;
                }
            }

            lastTP = trajectoryPosition;
            if (generatedCount % deltaTorchArc == 0) {
                arcPoints.add(new Vector2(x, y));
            }
            generatedCount++;
        }

        for (int i = 0; i < dotTorchArcCount / deltaTorchArc; i++) {
            if (i < generatedCount/ deltaTorchArc) {
                torchArc.get(i).getObstacle().setPosition(arcPoints.get(i));
            } else {
                torchArc.get(i).getObstacle().setPosition(-1,-1);
            }
        }
        // this part right here is just to display final hit section
        torchArc.get(torchArc.size() - 1).getObstacle().setPosition(arcPoints.get(arcPoints.size()-1));
        dtTorchArcOffset += animationOffsetTorchArc;
    }

    private void hideArc() {
        if (torchArc.get(0).getObstacle().getPosition().x == -1 && torchArc.get(0).getObstacle().getPosition().y == -1) {
            return;
        }
        for (ObstacleSprite i : torchArc) {
            i.getObstacle().setPosition(-1,-1);
        }
    }

    // Camera player not light camera (light camera updated internally) but movements
    // here are for the camera that follows player (?)
    private void updateCamera() {
//        System.out.println();

        float prevX = camera.position.x;
        float prevY = camera.position.y;

        Vector2 playerPos = avatar.getObstacle().getPosition();
        float playerX = playerPos.x*scale.x;
        float playerY = playerPos.y*scale.y;

        float lerp = 0.3f;
        camera.position.x += (playerX - camera.position.x) * lerp;
        camera.position.y += (playerY - camera.position.y) * lerp;

//        float visibleW =  (bounds.x + bounds.width) * scale.x/2*0.8f; //half of world visible
//        float visibleH = (bounds.y + bounds.height) * scale.y/2*0.8f;

//        System.out.println(camera.viewportWidth + ", " + camera.viewportHeight);
        float visibleW =  camera.viewportWidth/2*camera.zoom; //half of world visible, zoomed
        float visibleH = camera.viewportHeight/2*camera.zoom;
//        System.out.println("actual height and width: " + height +", " + width);
//        System.out.println(visibleW + ": W, H ;" + visibleH + ";; " + camera.zoom);
//        System.out.println("gutters, top: " + fitViewport.getTopGutterHeight() + ", bottom: " + fitViewport.getBottomGutterHeight() + ", left: " + fitViewport.getLeftGutterWidth() + ", right: " + fitViewport.getRightGutterWidth());
//        System.out.println("screen width and height " + fitViewport.getScreenWidth() + ", " + fitViewport.getScreenHeight() + " ;; now world: " + fitViewport.getWorldWidth() + ", " + fitViewport.getWorldHeight());
//        System.out.println("cam viewports: " + camera.viewportWidth + ", " + camera.viewportHeight);


        camera.position.x = MathUtils.clamp(camera.position.x,
            bounds.x*scale.x+visibleW,
            (bounds.x+bounds.width)*scale.x - visibleW);
//        System.out.println(camera.position.x +", " + bounds.x + " , " + bounds.width + " , " + bounds.height+ " , " + scale);
//        System.out.println("min: "+ (bounds.x*scale.x+visibleW )+ "max: "+ ((bounds.x+bounds.width)*scale.x - visibleW));
        //System.out.println("x reached bounds:"+(camera.position.x==bounds.x*scale.x+visibleW));
        camera.position.y = MathUtils.clamp(camera.position.y,
            bounds.y*scale.y+visibleH,
            (bounds.y+bounds.height)*scale.y - visibleH);

        camera.update();

        //debug code
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(bounds.x * scale.x, bounds.y * scale.y,
            bounds.width * scale.x, bounds.height * scale.y);
        shapeRenderer.end();


        float dx = camera.position.x-prevX;
        float dy = camera.position.y-prevY;
        lightController.updateCamera(dx,dy);
    }

    @SuppressWarnings("unchecked")
    private void updateRunes(float dt) {
        for (Rune rune : runeSet) {
            System.out.println(rune.getPowerLevel());
            if (!rune.returnInLight()) {
                rune.dissapatePowerLevel();
            }
            float factor = rune.getPowerLevel();
            float prevFactor = rune.getPrevPowerLevel();
            factor = Math.max(factor,0);
            factor = Math.min(factor,1);
            if (factor == 0 || factor == 1) {
                break;
            }

            for (EventAction<?> undefEventAction : rune.getEventAction()) {

                if (undefEventAction.getInitialValue() instanceof Vector2) {
                    EventAction<Vector2> eventAction = (EventAction<Vector2>) undefEventAction;
                    Vector2 initial = eventAction.getInitialValue().cpy();
                    Vector2 fin = eventAction.getFinalValue().cpy();
                    switch (eventAction.getName()) {
                        case "move":
                            Vector2 endPointZeroed = rune.returnInLight() ? (fin.cpy()).sub(initial) : (initial.cpy()).sub(fin);
                            Vector2 delta = endPointZeroed.cpy().scl(factor).sub(endPointZeroed.cpy().scl(prevFactor));
                            delta.scl(1/dt) ;
                            eventAction.getTarget().getObstacle().setLinearVelocity(delta.scl(rune.returnInLight() ? 1 : -1));
                            if (2*factor - prevFactor >= 1 || 2*factor - prevFactor <= 0) {
                                eventAction.getTarget().getObstacle().setLinearVelocity(Vector2.Zero);
                            }
                            break;
                    }

                } else if (undefEventAction.getInitialValue() instanceof Float) {
                    EventAction<Float> eventAction = (EventAction<Float>) undefEventAction;
                    Float initial = eventAction.getInitialValue();
                    Float fin = eventAction.getFinalValue();

                    switch (eventAction.getName()) {
                        case "rotate":
                            Float endPointZeroed = fin - initial;
                            eventAction.getTarget().getObstacle().setAngle(endPointZeroed * factor + initial);
                            break;
                    }

                }
            }
        }
    }

    /**
     * function that pulls the collision flags from the collision controller and porcesses them as needed
     * mainly comprises items that cannot bbe done within the cotnroller or are expected in the
     * super, like destroying joints
     */
    private void supplementaryCollisionActions() {
        Stack<CollisionFlag> todos = contactListener.getCollisionFlags();
        while ( !todos.isEmpty() ) {
            CollisionFlag todo_action = todos.pop();
            switch (todo_action.getName()) {
                case "addTorch":
                    if (torch.canBePickedUp()) {
                        queueAddTorch = true;
                    }
                    break;
                case "traciGrounded":
                    if (avatar.getGroundedState().equals(GroundState.CLIMBING)) {
                        avatar.removeClimbingPhysics();
                    }
                    avatar.setGroundedState(GroundState.GROUNDED);
                    sensorFixtures.add(todo_action.getFixture());
                    break;
                case "traciAirborne":
                    sensorFixtures.remove(todo_action.getFixture());
                    if (sensorFixtures.size == 0) {
                        if (avatar.getGroundedState().equals(GroundState.CLIMBING)) {
                            avatar.removeClimbingPhysics();
                        }
                        avatar.setGroundedState(GroundState.AIRBORNE);
                    }
                    break;
                case "addClimbingJoint":
                    avatar.registerClimbable((EnhancedObstacleSprite) todo_action.getSubject());
                    break;
                case "removeClimbingJoint":
                    avatar.removeClimbable((EnhancedObstacleSprite) todo_action.getSubject());
                    break;
                case "queueWin":
                    setComplete(true);
                    break;
                case "queueFailure":
                    queueFailure = true;
                    break;
                case "debugKillObj":
                    // not safe operation, for now will kill game on reload
                    world.destroyBody(todo_action.getSubject().getObstacle().getBody());
                    break;
            }
        }
    }

    /**
     * function that pulls the fire flags from the fire controller and porcesses them as needed
     * mainly comprises items that cannot bbe done within the cotnroller or are expected in the
     * super, like destroying joints
     */
    private void supplementaryFireActions() {
        Stack<FireFlag> todos = fireController.getFireFlags();
        while (!todos.isEmpty()) {
            FireFlag fireFlag = todos.pop();
            switch (fireFlag.getName()) {
                case "attachFire":
                    Fire fire = fireFlag.getFire();
                    addSprite(fire);
                    if ((fireFlag.getSubject()).getObstacle().getBody() == null) {
                        break;
                    }
                    joinFireToObject(fireFlag.getSubject(), fire);
                    break;
                case "expireObj":
                    for (Fire f : fireFlag.getFires()) {
                        if (f.getFixtureJoint() != null) {
                            world.destroyJoint(f.getFixtureJoint());
                            f.setFixtureJoint(null);
                        }
                        f.dispose();
                    }
                    (fireFlag.getSubject()).getObstacle().markRemoved(true);
                    fireController.cleanObj(fireFlag.getSubject());
                    break;
                case "spawnSmoke":
                    ObstacleSprite smoke = fireFlag.getSmoke();
                    addSprite(smoke);
                    break;
                case "killFire":
                    Fire f = fireFlag.getFire();
                    if (f.getFixtureJoint() != null) {
                        if (!world.isLocked()) {
                            world.destroyJoint(f.getFixtureJoint());
                        }
                        f.setFixtureJoint(null);
                    }
                    if (torchFire != null && f.fireID == torchFire.fireID) {
                        if (activeTorchJoint != null && !world.isLocked()) {
                            world.destroyJoint(activeTorchJoint);
                            activeTorchJoint = null;
                        }
                    }
                    fireController.cleanFire(f);
                    f.dispose();
                    break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Function that pulls the stack of event action from the event handler and goes through each one
     * if there needs to be any upkeep in caller, like resetting a button, it occurs in the first part
     * then the event is actual action is pulled out of the event, any changes made, then processed ie
     * either running the event now or putting it into the tween handler
     *
     * @param dt delta time
     */
    private <T,U> void supplementaryEventActions(float dt) {
        Stack<Event<?,?>> todos = eventHandler.getEventFlags();
        Set<Button> toggleButtons = new HashSet<>();
        while (!todos.isEmpty()) {
            Event<T,U> event = (Event<T, U>) todos.pop();
            EventAction<U> action = event.action;

            // if the caller of the event needs any upkeep, in the case of a button the inverse of the
            // event will be added to be called after the button's state changes again, to act like a
            // true button
            if (event.source instanceof Button){
                EventAction<U> undo_action = action.cloneTweenEvent();
                U temp = undo_action.getInitialValue();
                undo_action.setInitialValue(undo_action.getFinalValue());
                undo_action.setFinalValue(temp);

                Button button = (Button) event.source;
                toggleButtons.add(button);

                int nextState;
                if (button.getDoubleSided()) {
                    nextState = 1;
                } else {
                    nextState = button.getState() == 0 ? 1 : 0;
                }

                if (button.getDoubleSided() || (!button.getDoubleSided() && !button.getLatch())) {
                    Event newEvent = event.clone();
                    event.conditional = (T state) -> state.equals(nextState);
                    event.action = undo_action;
                    eventHandler.registerEvent(event);
                }
            }

            // register the tween from the event to do an action
            float timeTill = action.getTime();
            switch (action.getName()) {
                case "move":
                    // the whooole point of this and its ocunterpart in "flaot" is to one, prevent two diff
                    // tweens of the same class (CAREFUL ABOUT CHAINING!!)  to affect the same object
                    // but also such that if a tween is interupted at point m on path A -> B, then
                    // it will return to A state in the same time it took to get to m regardless of
                    // where m is along the path (and more importantly because this gave me a headacche:
                    // how many times the tween was interupted along the path hence the calculation from origin each time)
                    // can be done better/optimzied but unsure if needed atm
                    Optional<TweenElement<Vector2>> oldEvent = tweenedMovmentObjectsVec2.stream().filter
                        (a -> a.target == action.getTarget() && a.name.equals(action.getName())).findFirst();
                    if (oldEvent.isPresent()) {
                        Vector2 start = ((Vector2) action.getInitialValue()).cpy();
                        Vector2 travellingPoint = start.cpy();
                        float stepCounter = 0;
                        Vector2 end = (Vector2) action.getFinalValue();
                        float terminalToDist = oldEvent.get().supplier.get().dst(end);

                        //loop should be safe as this is a rare action run only when a tween of this class is interupted
                        while (travellingPoint.dst(end) > terminalToDist) {
                            stepCounter += dt;
                            // no new vector creation cause that takes memory
                            travellingPoint.set((end.x - start.x) * action.interpolator.apply(stepCounter / action.getTime()) + start.x,
                                                (end.y - start.y) * action.interpolator.apply(stepCounter / action.getTime()) + start.y);
                        }
                        timeTill = action.getTime() - stepCounter;
                        tweenedMovmentObjectsVec2.remove(oldEvent.get());
                    }

                    // adjust new values for tween from aciton
                    Vector2 initialStateVec2 = action.getTarget().getObstacle().getPosition().cpy();
                    Supplier<Vector2> supplierVec2 = () -> action.getTarget().getObstacle().getPosition();
                    Consumer<Vector2> consumerVec2 = (value) -> action.getTarget().getObstacle().setLinearVelocity(value);

                    // attach tween to movement objects to be ran
                    TweenElement<Vector2> tweenElementVec2 = new TweenElement<Vector2>(action.getTarget(), action.getName(),
                        initialStateVec2, (Vector2) action.getFinalValue(), timeTill,
                        action.getInterpolator(), supplierVec2, consumerVec2);
                    tweenedMovmentObjectsVec2.add(tweenElementVec2);
                    break;

                case "rotate":
                    // see mirrored documentation in "move"
                    Optional<TweenElement<Float>> oldEventF = tweenedMovmentObjectsFloat.stream().filter
                        (a -> a.target == action.getTarget() && a.name.equals(action.getName())).findFirst();


                    if (oldEventF.isPresent()) {
                        Float start = (Float) action.getInitialValue();
                        Float travellingPoint = start;
                        float stepCounter = 0;
                        Float end = (Float) action.getFinalValue();
                        float terminalDist = end - oldEventF.get().supplier.get();

                        while (end - travellingPoint > terminalDist) {
                            stepCounter += dt;
                            travellingPoint = (end - start) * action.interpolator.apply(stepCounter / action.getTime()) + start;
                        }

                        timeTill = action.getTime() - stepCounter;
                        tweenedMovmentObjectsFloat.remove(oldEventF.get());
                    }

                    Float initialStateFloat = action.getTarget().getObstacle().getAngle();
                    Supplier<Float> supplierFloat = () -> action.getTarget().getObstacle().getAngle();
                    Consumer<Float> consumerFloat = (value) -> action.getTarget().getObstacle().setAngle(value);

                    TweenElement<Float> tweenElementFloat = new TweenElement<Float>(action.getTarget(), action.getName(),
                        initialStateFloat, (Float) action.getFinalValue(), timeTill,action.getInterpolator(), supplierFloat, consumerFloat);
                    tweenedMovmentObjectsFloat.add(tweenElementFloat);
                    break;

                case "demo":
                    temp.deactivateAnchor(1);
                    break;
            }
        }

        for (Button button : toggleButtons) {
            button.toggleButton(world);
        }
    }

    /**
     * Dedicated method to update Vec2 tweens, should be applicable for any vector change in attribute
     * not just movements
     *
     * The reason behind splitting Vec2 and Flaot instead of abstracting itno one, is because, most likely,
     * we aren't going to be tweening anything more than that and the generalization/casting was becoming
     * tedious and unsafe
     *
     * @param dt deltatime
     */
    private void updateTweenedMovementObjectsVec2(float dt) {
        for (Iterator<TweenElement<Vector2>> it = tweenedMovmentObjectsVec2.iterator(); it.hasNext(); ) {
            TweenElement<Vector2> tweenElement = it.next();

            // extract info from tween for readability and to prevent duplicate pulls later
            Vector2 timer = tweenElement.timerVector;
            Function<Float, Float> interpolator = tweenElement.interpolator;
            Consumer<Vector2> setter = tweenElement.updater;

            Vector2 endPointZeroed = (tweenElement.finalState.cpy()).sub(tweenElement.initalState);

            // get the interpolater value from time elapsed (x) / total time (y) as factor, then
            // apply the value to the getter for the funcction
            float factor = interpolator.apply(timer.x / timer.y);
            float oldFactor = interpolator.apply(Math.max(timer.x - dt, 0)/ timer.y);
            Vector2 delta = ( (endPointZeroed.cpy()).scl(factor) ).sub( ((endPointZeroed.cpy()).scl(oldFactor)) );
            delta.scl(1/dt);
            setter.accept(delta);

            timer.x += dt;

            // if elapsed>total allowed time
            if (timer.x > timer.y) {
                it.remove();

                // just a fail safe if the velocity does something weird
                if (tweenElement.name.equals("move")) {
                    setter.accept(Vector2.Zero);
                    tweenElement.target.getObstacle().setPosition(tweenElement.finalState);
                }
            }
        }
    }

    /**
     * see mirror documentation for updateTweenedMovementObjectsVec2
     * @param dt deltatime
     */
    private void updateTweenedMovementObjectsFloat(float dt) {
        for (Iterator<TweenElement<Float>> it = tweenedMovmentObjectsFloat.iterator(); it.hasNext(); ) {
            TweenElement<Float> tweenElement = it.next();

            Vector2 timer = tweenElement.timerVector;
            Function<Float, Float> interpolator = tweenElement.interpolator;
            Consumer<Float> setter = tweenElement.updater;

            Float endPointZeroed = (tweenElement.finalState - tweenElement.initalState);

            float factor = interpolator.apply(timer.x / timer.y);
            Float delta = (endPointZeroed * factor) + tweenElement.initalState;
            setter.accept(delta);
            timer.x += dt;
            if (timer.x >= timer.y) {
                it.remove();
                tweenElement.updater.accept(tweenElement.finalState);
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
        torch.getObstacle().setSensor(true);
        queueAddTorch = false;
        avatar.setHasTorch(true);
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
//        ScreenUtils.clear(0,0,0,1);
        // This shows off how powerful our new SpriteBatch is
        fitViewport.apply();
        batch.begin(camera);


        // Draw the meshes (images)
        for(ObstacleSprite obj : sprites) {
            batch.setProjectionMatrix(camera.combined);
            obj.draw(batch);
        }

        if (!fireController.getLitFires().isEmpty()){
//            System.out.println("not FIRE!");
            for (Fire fire:fireController.getLitFires()){
                particleEngine.draw(batch,fire);
            }
        }
        if (!torchFire.getObstacle().isRemoved()) {
            particleEngine.draw(batch,torchFire);
        }
        //lightController.fireLights(fireController);



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
        for (FloatingLight light : floatingLights){
            if (light.isOff()){
                lightController.turnOffAmbientLight(light);
            }
        }
        lightController.render();
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
//        scale.x = width/bounds.width;
        scale.y = height/bounds.height;
        scale.x = scale.y;
        // this works???? ^^^

        fitViewport.update(width, height, true);
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
