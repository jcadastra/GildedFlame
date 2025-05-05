package edu.cornell.cis3152.physics.level_player;

import box2dLight.Light;
import box2dLight.PointLight;
import box2dLight.PositionalLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.Viewport;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.cis3152.physics.level_player.enviromentals.FloatingLight;
import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;
import edu.cornell.cis3152.physics.level_player.player.Avatar;
//import edu.cornell.cis3152.physics.level_player.player.Traci;
import edu.cornell.cis3152.physics.level_player.utils.FireFlag;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.util.PooledList;

import java.util.*;

public class LightController {

    private World world;
    private RayHandler rayHandler;
    private OrthographicCamera camera;
    private float cameraZoomLevel;

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

    private float BOX_TO_WORLD;

    private float WORLD_TO_BOX;

    private Fire fire;

    private Rectangle bounds;

    private int flickerMax = 300;
    private int flickerCount = 300;
    private int lightIndex = 0;
    private float lightRadius = 5f;
    private boolean debug;
    public static final short CATEGORY_AVATAR = 0x0002;  // 00000010
    public static final short CATEGORY_ENVIRONMENT = 0x0004;  // 00000100
    public static final short CATEGORY_LIGHT = 0x0008;  // 00001000

    private PositionalLight playerLight;

    /*Pool of lights for doing fire*/
    private PooledList<PointLight> lightPool;
    private int maxLights = 30;
    Map<Body, PointLight> lightAssignments = new HashMap<>();

    private Map<Integer,PointLight> fireAssignments = new HashMap<>();

    private int[] lightInUse = new int[maxLights];


    public void initLights(RayHandler rayHandler) {
        lightPool = new PooledList<>();

        for (int i = 0; i < maxLights; i++) {
            PointLight light = new PointLight(rayHandler, 20, Color.LIGHT_GRAY, 0.5f, 0, 0);
            light.setActive(false);  // Hide initially
            lightPool.add(light);
        }
    }



    /* Gives the filter catergory bits for the fixture*/
    public short getFilter(String name) {
        if (name.equals("avatar")) {
            return CATEGORY_AVATAR;
        } else if (name.contains("light")) {
            return CATEGORY_LIGHT;

        } else {
            return CATEGORY_ENVIRONMENT;
        }
    }

    /*
     * Lighting_ON: the Lighting is on, has a radius, attached to a flame
     * Lighting_WAVER: the Lighting is being stamped on, it wavers
     * Lighting_OFF: the Lighting is off, radius = 0
     * */
    public enum LightingState {
        Lighting_ON,
        Lighting_WAVER,
        Lighting_OFF,
    }

    public void setRadius(int radius) {

    }
    public LightController(Vector2 points, World world, OrthographicCamera camera,
                           Rectangle bounds, float physicsUnits, float cameraZoomLevel) {
        this.BOX_TO_WORLD = physicsUnits;
        this.WORLD_TO_BOX = 1/physicsUnits;
        this.world = world;
        this.bounds = bounds;
        this.cameraZoomLevel = cameraZoomLevel;
        // Create a separate camera for box2dlights
        this.camera = new OrthographicCamera(bounds.width, bounds.height);//Uses physic units
        this.camera.position.set(bounds.width / 2.0f, bounds.height / 2.0f, 0);
        //cameraViewport
        //this.camera.setToOrtho(false, bounds.width, bounds.height);
        camera.zoom=cameraZoomLevel;
        this.camera.update();
        rayHandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        rayHandler.setCombinedMatrix(this.camera);

        torchLight = new Lighting(2.2f, points);

        //Initializes torch light
        //PositionalLight testlight = new PointLight(rayHandler,10,Color.WHITE,100f,10,10);
        Color lightCol = new Color(1f, 0.92f, 0.6f, 1);
        torchLighting = new PointLight(rayHandler, 100, lightCol,
            5f, points.x, points.y);
        torchLighting.setSoft(false);
        torchLighting.setSoftnessLength(10f);
        torchLightState = torchLight.getState();

        Color playerLightCol = new Color(Color.LIGHT_GRAY.r, Color.LIGHT_GRAY.g, Color.LIGHT_GRAY.b, 0.1f);
        playerLight = new PointLight(rayHandler, 60, Color.LIGHT_GRAY, 2.5f, points.x, points.y);
        playerLight.setContactFilter(CATEGORY_LIGHT, (short) 0,
            (short) CATEGORY_ENVIRONMENT);
        playerLight.setSoft(true);

        torchLighting.setContactFilter(CATEGORY_LIGHT, (short) 0, (short) CATEGORY_ENVIRONMENT);
        //rayHandler.useCustomViewport(viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
        rayHandler.useDiffuseLight(true);
//         Uncomment if you want no overlay dark hue  ⬇️
//        rayHandler.useDiffuseLight(false);
        // Background light color, original hue ⬇️
//        rayHandler.setAmbientLight(0.15f, 0.15f, 0.35f, 1f); // same hue, just darker
        // Background black color ⬇️
//        rayHandler.setAmbientLight(Color.BLACK);
        //rayHandler.setBlur(true);
        debug = false;
        initLights(rayHandler);
        Arrays.fill(lightInUse, 0);
    }


    public LightController(JsonValue data, World world, OrthographicCamera camera) {
        this.world = world;
        this.camera = camera;
        this.rayHandler = new RayHandler(world);
        rayHandler.setCombinedMatrix(camera);

        //Positions the torch light
        JsonValue torchInfo = data.get("torch-light");
        torchLight = new Lighting(1f, torchInfo);

        //Initializes torch light
        torchLighting = new PointLight(rayHandler, 100, new Color(1.0f, 1.0f, 0.0f, 1.0f),
            torchLight.getRadius(), torchLight.getX(), torchLight.getY());
        //torchLighting.setSoft(false);
        //rayHandler.setAmbientLight(0.5f);
        //rayHandler.setShadows(true);

    }

    public void attachTorchLight(Fire fire) {
        this.fire = fire;
        torchLighting.attachToBody(fire.getObstacle().getBody());
        torchLightState = torchLight.getState();
    }

    public void attachPlayerLight(Avatar avatar) {
        playerLight.attachToBody(avatar.getObstacle().getBody());
    }


    public void updateAttach(Fire fire) {
        Vector2 bodyPosition = fire.getObstacle().getPosition();

        // Apply the offset (in meters) if needed, for example, position the light above the body
        float lightPosX = bodyPosition.x + torchLighting.getX() * WORLD_TO_BOX;
        float lightPosY = bodyPosition.y + torchLighting.getY() * WORLD_TO_BOX;

        // Update the light's position (in world coordinates)
        torchLighting.setPosition(lightPosX, lightPosY);
    }

    public void attachAmbientLight(ObstacleSprite sprite) {
        PointLight light = lightPool.get(lightIndex);
        if (sprite.getClass() == Fire.class) {
            //System.out.println("is fire");
            if (lightInUse[lightIndex]==2){
                for (Map.Entry<Body,PointLight> entry : lightAssignments.entrySet()){
                    entry.getValue().equals(light);
                    entry.getValue().setActive(false);//turn off the ambient light
                    lightInUse[lightIndex]=0;//not in use
                }
                lightAssignments.clear();
            }
            Fire fire = (Fire) sprite;
//            System.out.println(fireAssignments.get(fire.fireID)==null);
            if (fireAssignments.get(fire.fireID)!=light){//only adds fires when it's not already there
//                System.out.println("add fire!");
                light.setColor(Color.YELLOW);
                light.setDistance(3f);
                light.attachToBody(sprite.getObstacle().getBody());
                light.setActive(true);
                light.setContactFilter(CATEGORY_LIGHT, (short) 0,
                    (short) CATEGORY_ENVIRONMENT);
                light.setSoft(true);
                lightInUse[lightIndex]=1;
                fireAssignments.put(fire.fireID,light);
            }
        }else if (sprite.getClass() == FloatingLight.class) {
//            System.out.println("is floating light");
                if (lightAssignments.get(sprite.getObstacle().getBody())!=light){
               //check if it's already attached
                   // System.out.println("attaching new light");
                    if(lightInUse[lightIndex]!=1){//only when fire is not using it
                        lightAssignments.put(sprite.getObstacle().getBody(), light);
                        //light.setColor(Color.LIGHT_GRAY);
                        light.setColor(Color.LIGHT_GRAY.r, Color.LIGHT_GRAY.g, Color.LIGHT_GRAY.b, 1f);
                        light.setDistance(1.5f);
                        light.attachToBody(sprite.getObstacle().getBody());
                        light.setActive(true);
                        light.setSoft(true);
                        light.setContactFilter(CATEGORY_LIGHT, (short) 0,
                            (short) CATEGORY_ENVIRONMENT);
                        lightInUse[lightIndex] = 2;
                    }
                }
            }
//        System.out.println("index"+lightIndex);
//        System.out.println(lightInUse[lightIndex]);
            lightIndex = (lightIndex + 1) % maxLights;
        }


    public Array<Lighting> fireLights(FireController fireController) {
        Array<Lighting> fireLights = new Array<>();
        for (Fire fire: fireController.getLitFires()){
                if (fire.fireID!=0){//not torch flamed
//                    System.out.println(fire.fireID);
                    Lighting lighting = new Lighting(2.2f,fire.getObstacle().getPosition());
                    fireLights.add(lighting);
//                    System.out.print("fire id"+fire.fireID+" ");
                    attachAmbientLight(fire);

            }
        }
        return fireLights;
    }


    public void turnOffAmbientLight(ObstacleSprite sprite) {
        PointLight light = lightAssignments.get(sprite.getObstacle().getBody());
        if (light != null) {
            light.setActive(false);
            lightAssignments.remove(sprite.getObstacle().getBody());
        }
        //Arrays.fill(lightInUse,0);
    }

    public void translate() {

    }


    public void render() {

        rayHandler.setCombinedMatrix(camera);
        if (fire != null){
            updateAttach(fire);
        }
        if (torchLightState != null) {
            switch (torchLightState) {
                case LIGHT_OFF:
                    torchLighting.setActive(false);
                    break;
                case LIGHT_WAVER:
                    float dis = torchLighting.getDistance() - 1;
                    torchLighting.setDistance(1 + dis * flickerCount / flickerMax);
                    if (flickerCount < 0) {
                        torchLightState = Lighting.LightState.LIGHT_OFF;
                    }
                    break;
                case LIGHT_ON:
                    if (flickerCount < 0) {
                        flickerCount = flickerMax;
                    }//light out, renew
                    break;
            }
        }

        rayHandler.update();
        rayHandler.render();
        if (debug){
            ShapeRenderer shapeRenderer = new ShapeRenderer();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.circle(torchLighting.getX() * BOX_TO_WORLD, torchLighting.getY() * BOX_TO_WORLD,
                torchLighting.getDistance() * BOX_TO_WORLD);
            shapeRenderer.end();

        }

    }

    public void resetCamera(Camera mainCamera) {
        float dx = mainCamera.position.x;
        float dy = mainCamera.position.y;
        this.camera.position.set(dx / BOX_TO_WORLD, dy / BOX_TO_WORLD, 0);
        camera.zoom=cameraZoomLevel;
        inBounds();
        this.camera.viewportWidth = mainCamera.viewportWidth/BOX_TO_WORLD;
        this.camera.viewportHeight = mainCamera.viewportHeight/BOX_TO_WORLD;
        this.camera.update();
        rayHandler.setCombinedMatrix(camera);
        rayHandler.update();
    }

    private void inBounds(){
//        System.out.println(camera.viewportWidth+ ",  ***  , " + camera.viewportHeight);
        float visibleW =  camera.viewportWidth/2*camera.zoom; //half of world visible, zoomed
        float visibleH = camera.viewportHeight/2*camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x,
            bounds.x * WORLD_TO_BOX + visibleW * WORLD_TO_BOX,
            (bounds.x + bounds.width) * BOX_TO_WORLD - visibleW);
        camera.position.y = MathUtils.clamp(camera.position.y,
            bounds.y * BOX_TO_WORLD + visibleH,
            (bounds.y + bounds.height) * BOX_TO_WORLD - visibleH);
    }

    public void updateCamera(float dx, float dy) {

        camera.position.x += dx / BOX_TO_WORLD;
        camera.position.y += dy / BOX_TO_WORLD;
        // --- UPDATE CAMERA MATRIX ---
        camera.update();
        // --- UPDATE RAYHANDLER ---
        // Convert the camera's physics position to pixel units for the RayHandler
        float cameraPixelX = camera.position.x;
        float cameraPixelY = camera.position.y;

        inBounds();
        // Update RayHandler with the camera's position in pixel units
        rayHandler.setCombinedMatrix(camera);
        rayHandler.update();
    }
    public void updateCamera(OrthographicCamera mainCamera){
        this.camera.viewportWidth = mainCamera.viewportWidth/BOX_TO_WORLD;
        this.camera.viewportHeight = mainCamera.viewportHeight/BOX_TO_WORLD;
        //this.camera.zoom = mainCamera.zoom;
        this.camera.update();
        rayHandler.setCombinedMatrix(this.camera);
        rayHandler.update();
    }



    public void dispose() {
        rayHandler.dispose();
        if (camera != null) {
            camera = null;
        }
        lightPool.clear();
        lightAssignments.clear();
        fireAssignments.clear();
    }

//    public void update(FireController fireController) {
//        Set<Fire> deadFires = fireController.getAllFires();
//        deadFires.removeAll(fireController.getLitFires());
//        for (Fire fire : deadFires){
//            if (fireAssignments.get(fire.fireID) != null) {//a previous lit fire
//                PointLight light = lightAssignments.get(fire.fireID);
//                light.setActive(false);
//                int index = lightPool.indexOf(light);
//                lightInUse[index] = 0;
//                fireAssignments.remove(fire.fireID);
//            }
//        }
//    }

    public void update(FireController fireController) {
       Set<Fire> allFires = fireController.getAllFires();
       Set<Fire> litFires = fireController.getLitFires();
       for (Fire fire : allFires) {//turn off all fires
           if (fireAssignments.containsKey(fire.fireID)) {
               PointLight light = fireAssignments.get(fire.fireID);
               light.setActive(false);
           }
           fireAssignments.clear();
           Arrays.fill(lightInUse,0);
       }
       for (Fire fire : litFires) {
           if (fire.fireID!=0){//not torch flamed
//                    System.out.println(fire.fireID);
               Lighting lighting = new Lighting(2.2f,fire.getObstacle().getPosition());
//               System.out.print("fire id"+fire.fireID+" ");
               attachAmbientLight(fire);

           }
       }
    }




    public void update(Boolean beginSmother) {
        if (beginSmother) {
            //torchLight.waver(flickerCount);
//            System.out.println("flickerCount"+flickerCount);
            flickerCount--;
            if (flickerCount % 60 == 0 && flickerCount > 0) {
                torchLightState = Lighting.LightState.LIGHT_WAVER;
            } else {
                torchLightState = Lighting.LightState.LIGHT_ON;
            }
            if (flickerCount <= 0) {
                torchLightState = Lighting.LightState.LIGHT_OFF;
            }
        } else {
            if (torchLightState == Lighting.LightState.LIGHT_ON) {
//                System.out.println("reset light radius");
                torchLighting.setDistance(lightRadius);
            }
        }
    }
}



