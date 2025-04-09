package edu.cornell.cis3152.physics.level_player;

import box2dLight.PointLight;
import box2dLight.PositionalLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;

public class LightController {

    private World world;
    private RayHandler rayHandler;
    private OrthographicCamera camera;

    /** All of the active environmental Lightings that we loaded from the JSON file */
    private Array<Lighting> lights = new Array<>();

    private Array<PointLight> lightings = new Array<>();

    /* All active environmental lights.*/
    private Array<Integer> activeLights = new Array<>();

    /* The state of each environmental light*/
    private Array<Integer> lightStates = new Array<>();

    /*The index of environmental light being calculated*/
    private int currentLight;

    /*The torch-light*/
    private Lighting torchLight;
    private PositionalLight torchLighting;

    /*State fo the torch-light*/
    private Lighting.LightState torchLightState;

    private int numRays;

    private float BOX_TO_WORLD = 32.0f;

    private float WORLD_TO_BOX = 1/32.0f;

    private Fire fire;

    private Rectangle bounds;

    private boolean debug;
    public static final short CATEGORY_AVATAR = 0x0002;  // 00000010
    public static final short CATEGORY_ENVIRONMENT = 0x0004;  // 00000100
    public static final short CATEGORY_LIGHT = 0x0008;  // 00001000


    /* Gives the filter catergory bits for the fixture*/
    public short getFilter(String name){
        if (name.equals("avatar")){
            return CATEGORY_AVATAR;
        } else if (name.contains("light")) {
            return CATEGORY_LIGHT;

        }else{
            return CATEGORY_ENVIRONMENT;
        }
    }
    /*
     * Lighting_ON: the Lighting is on, has a radius, attached to a flame
     * Lighting_WAVER: the Lighting is being stamped on, it wavers
     * Lighting_OFF: the Lighting is off, radius = 0
     * */
    public enum LightingState{
        Lighting_ON,
        Lighting_WAVER,
        Lighting_OFF,
    }

    public void setRadius(int radius){

    }
    public LightController(Vector2 points, World world, OrthographicCamera camera, Rectangle bounds){
        this.world = world;
        this.bounds = bounds;
        //this.camera = camera;
        // Create a separate camera for box2dlights
        this.camera = new OrthographicCamera(bounds.width, bounds.height);//Uses physic units
        this.camera.position.set(bounds.width/2.0f,bounds.height/2.0f,0);
        //this.camera.setToOrtho(false, bounds.width, bounds.height);
        this.camera.update();
        //rayHandler = new RayHandler(world,(int)this.camera.viewportWidth,(int)this.camera.viewportHeight);
        rayHandler = new RayHandler(world,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        //System.out.println("viewport"+this.camera.viewportWidth+","+camera.viewportHeight);
        //System.out.println("graphics"+Gdx.graphics.getWidth()+","+Gdx.graphics.getHeight());
        rayHandler.setCombinedMatrix(this.camera);

        torchLight = new Lighting(2.2f, points);

        //Initializes torch light
        //PositionalLight testlight = new PointLight(rayHandler,10,Color.WHITE,100f,10,10);
        torchLighting = new PointLight(rayHandler, 100, Color.YELLOW,
            5f, points.x, points.y);
        torchLighting.setSoft(false);

        //TODO:right now the light is interacting with nothing, discuss if this is the bahviour we want?
        torchLighting.setContactFilter(CATEGORY_LIGHT, (short)0, (short) CATEGORY_ENVIRONMENT);
        //rayHandler.useCustomViewport(viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
        rayHandler.useDiffuseLight(true);
        // Background light color⬇️, modify if needed
        rayHandler.setAmbientLight(0.3f, 0.3f, 0.7f, 0.1f);
        rayHandler.setShadows(true);
        rayHandler.setBlur(true);
        //System.out.println(torchLighting==null);
        //System.out.println(points.x+","+points.y);
        debug = false;
        //torchLighting.setPosition(camera.position.x, camera.position.y);
        //System.out.println("light pos "+ torchLighting.getX()+","+torchLighting.getY());
    }
    public LightController(JsonValue data, World world, OrthographicCamera camera){
        this.world = world;
        this.camera = camera;
        this.rayHandler = new RayHandler(world);
        rayHandler.setCombinedMatrix(camera);

        //Positions the torch light
        JsonValue torchInfo = data.get("torch-light");
        torchLight = new Lighting(1f,torchInfo);

        //Initializes torch light
        torchLighting = new PointLight(rayHandler, 100, new Color(1.0f, 1.0f, 0.0f, 1.0f),
            torchLight.getRadius(), torchLight.getX(), torchLight.getY());
        //torchLighting.setSoft(false);
        //rayHandler.setAmbientLight(0.5f);
        //rayHandler.setShadows(true);
        //System.out.println(torchLighting==null);

    }

    public void attachTorchLight (Fire fire){
        this.fire = fire;
        //System.out.println("fire pos"+ fire.getObstacle().getX()+","+fire.getObstacle().getY());
        torchLighting.attachToBody(fire.getObstacle().getBody());
        torchLightState = torchLight.getState();
    }

    public void fireLights(FireController fireController){
        for (Fire fire: fireController.getLitFires()){
            attachFireLight(fire);
        }
    }
    private void attachFireLight(Fire fire){
        PointLight fireLight = new PointLight(rayHandler,100,Color.YELLOW,
            4f,fire.getObstacle().getX(),fire.getObstacle().getY());
        fireLight.attachToBody(fire.getObstacle().getBody());
    }

    public void updateAttach(Fire fire)
    {
        Vector2 bodyPosition = fire.getObstacle().getPosition();

        // Apply the offset (in meters) if needed, for example, position the light above the body
        float lightPosX = bodyPosition.x + torchLighting.getX()*WORLD_TO_BOX;
        float lightPosY = bodyPosition.y + torchLighting.getY()*WORLD_TO_BOX;

        // Update the light's position (in world coordinates)
        torchLighting.setPosition(lightPosX, lightPosY);
    }

    public void translate(){

    }


    public void render() {
        //System.out.println("Drawing light");
//        System.out.println("light pos "+ torchLighting.getX()+","+torchLighting.getY());
//        System.out.println("fire pos "+ fire.getObstacle().getX()+","+fire.getObstacle().getY());
      // camera.zoom = 0.8f;
        //camera.update();
        rayHandler.setCombinedMatrix(camera);
        //updateCamera();
        //rayHandler.setCombinedMatrix(camera.combined.scale(WORLD_TO_BOX,WORLD_TO_BOX,1));
        //System.out.println("camera position"+camera.position.x+ ","+camera.position.y);
        if (fire != null){
            updateAttach(fire);
        }
        //System.out.println("camera matrix"+camera.combined.toString());
//        rayHandler.setCombinedMatrix(camera.combined, camera.position.x, camera.position.y, camera.viewportWidth, camera.viewportHeight);
        //System.out.println("Camera Position: " + camera.position.x*WORLD_TO_BOX+","+camera.position.y*WORLD_TO_BOX);
        //rayHandler.update();
        //rayHandler.setCombinedMatrix(camera.combined.scale(WORLD_TO_BOX,WORLD_TO_BOX,WORLD_TO_BOX));
//            camera.position.x * WORLD_TO_BOX,
//            camera.position.y*WORLD_TO_BOX,
//            camera.viewportWidth * camera.zoom,
//            camera.viewportHeight * camera.zoom);
        rayHandler.update();
        rayHandler.render();
        if (debug){
            ShapeRenderer shapeRenderer = new ShapeRenderer();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.circle(torchLighting.getX()*BOX_TO_WORLD, torchLighting.getY()*BOX_TO_WORLD,
                torchLighting.getDistance()*BOX_TO_WORLD);
            shapeRenderer.end();

        }

    }

    public void resetCamera(float dx, float dy){
        this.camera.position.set(dx/BOX_TO_WORLD,dy/BOX_TO_WORLD,0);
        //rayHandler = new RayHandler(world,(int)this.camera.viewportWidth,(int)this.camera.viewportHeight);
//        rayHandler = new RayHandler(world,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
//        //System.out.println("viewport"+this.camera.viewportWidth+","+camera.viewportHeight);
//        //System.out.println("graphics"+Gdx.graphics.getWidth()+","+Gdx.graphics.getHeight());
//        rayHandler.setCombinedMatrix(this.camera);
        //attachTorchLight(fire);
        inBounds();
        this.camera.update();
        rayHandler.setCombinedMatrix(camera);
        rayHandler.update();
    }

    private void inBounds(){
        float visibleW =  camera.viewportWidth/2*0.8f; //half of world visible, zoomed
        float visibleH = camera.viewportHeight/2*0.8f;

        camera.position.x = MathUtils.clamp(camera.position.x,
            bounds.x*WORLD_TO_BOX+visibleW*WORLD_TO_BOX,
            (bounds.x+bounds.width)*BOX_TO_WORLD - visibleW);
        System.out.println("x reached bounds:"+(camera.position.x));
        camera.position.y = MathUtils.clamp(camera.position.y,
            bounds.y*BOX_TO_WORLD+visibleH,
            (bounds.y+bounds.height)*BOX_TO_WORLD - visibleH);
    }

    public void updateCamera(float dx, float dy){
//            // Get the player position in physics units (world units)
//            Vector2 firePos = fire.getObstacle().getPosition();
//
//            // Convert the player position from physics units to pixel units
//            float playerPixelX = firePos.x * BOX_TO_WORLD;  // Convert to pixel units
//            float playerPixelY = firePos.y * BOX_TO_WORLD;
//
//            // --- SMOOTH CAMERA FOLLOW ---
//            float lerp = 0.3f;  // Adjust for smoother or quicker movement
//            camera.position.x += (firePos.x - camera.position.x) * lerp; // Follow player in physics units
//            camera.position.y += (firePos.y - camera.position.y) * lerp;
//
//            // --- SMOOTH ZOOM USING VIEWPORT SIZE ---
//            float targetWidth = 960;  // Base width in world units (change based on need)
//            float targetHeight = 540;  // Base height in world units
//
//            float zoomFactor = 0.85f;  // Zoom factor: < 1 = zoom in, > 1 = zoom out
//            float zoomLerp = 0.05f;    // Smooth zoom transition speed
//
//            // Smoothly interpolate to the target width and height (zoom effect)
//            float newWidth = targetWidth * zoomFactor;
//            float newHeight = targetHeight * zoomFactor;
//
//            // Apply the updated projection matrix for zooming effect
//            camera.setToOrtho(false, newWidth, newHeight);
//
//            // --- CLAMP CAMERA TO STAY WITHIN WORLD BOUNDS ---
//            float halfWidth = newWidth / 2f;
//            float halfHeight = newHeight / 2f;
//
//            float minXPixel = bounds.x * BOX_TO_WORLD;
//            float maxXPixel = (bounds.x + bounds.width) * BOX_TO_WORLD;
//            float minYPixel = bounds.y * BOX_TO_WORLD;
//            float maxYPixel = (bounds.y + bounds.height) * BOX_TO_WORLD;
//
//            // Ensure the camera stays within the world bounds
//            camera.position.x = MathUtils.clamp(camera.position.x, minXPixel + halfWidth, maxXPixel - halfWidth);
//            camera.position.y = MathUtils.clamp(camera.position.y, minYPixel + halfHeight, maxYPixel - halfHeight);

            camera.position.x += dx/BOX_TO_WORLD;
            camera.position.y += dy/BOX_TO_WORLD;
            // --- UPDATE CAMERA MATRIX ---
            camera.update();
            // --- UPDATE RAYHANDLER ---
            // Convert the camera's physics position to pixel units for the RayHandler
            float cameraPixelX = camera.position.x;
            float cameraPixelY = camera.position.y;
            //System.out.println(cameraPixelX);
            //System.out.println(cameraPixelY);
        inBounds();

            // Update RayHandler with the camera's position in pixel units
            rayHandler.setCombinedMatrix(camera);
            rayHandler.update();
        }



    public void dispose() {
        rayHandler.dispose();
        if (camera!=null){
            camera = null;
        }
    }
}
//TODO: finish rayhandling, switch light states, and dispose when finished
