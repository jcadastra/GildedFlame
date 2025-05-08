package edu.cornell.cis3152.physics.level_player;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import edu.cornell.cis3152.physics.level_player.utils.RainFlag;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MarthasWeatherMachine {

    private RayCastCallback rayCastCallback;
    private ArrayList<Fixture> collidedObstacles;
    public ArrayList<ObstacleSprite> rainDrops;
    private float physicsUnits;
    private int rainTimer;
    private int rainFreq;
    private Vector2 windVel = new Vector2(Vector2.Zero);
    private Random random;
    private World world;

    private Stack<RainFlag> rainflags;
    private Vector2 rainRestPos = new Vector2(-2,-2);


    public MarthasWeatherMachine() {
//        TODO: ensure rain and wind work concurrently
        this.rainflags = new Stack<>();
        this.random = new Random();
        this.collidedObstacles = new ArrayList<Fixture>(){};
        this.rayCastCallback = (fixture, point, normal, fraction) -> {
            collidedObstacles.add(fixture);
            return 1;
        };
        this.rainDrops = new ArrayList<ObstacleSprite>(){};
    }

    public Stack<RainFlag> getRainflags() {return rainflags;}

    public void clean() {
//        rainRegion.clear();
//        kinematicObstacles.clear();
//        triangulator.clear();
//        path2.clear();
//        pathSmoother.clear();
    }

    public void activateRain(float physicsUnits, int rainFreq) {
        this.physicsUnits = physicsUnits;
        this.rainFreq = this.rainTimer = rainFreq;
    }

    public void activateWind(Vector2 velocity) {
        this.windVel = velocity;
    }
    public Vector2 getWind() {return windVel;}

    public boolean isRainActive() {
        return rainFreq != 0;
    }
    public boolean isWindActive() {
        return windVel.len()!=0;
    }

    public void updateWorld(World world) {this.world = world;}

    public void update(World world) {
        if (isRainActive()) {
            for (int i = 0; i < rainFreq; i++)
                generateRain();
        }

        if (isWindActive()) {
            blowWind(world);
        }
    }

    private void generateRain() {
        ObstacleSprite newDrop = getFreeRain();
        if (newDrop.getObstacle() == null) {
            return;
        }

        newDrop.getObstacle().setPosition(random.nextInt(40) + random.nextFloat(), 18);
        newDrop.getObstacle().setLinearVelocity(new Vector2(0,-5));
    }
    public void blowWind(World world) {
        Array<Body> entities = new Array<Body>();
        world.getBodies(entities);
        for (Body body : entities) {
            if (body.isActive()) {
                ObstacleSprite obs = ((ObstacleSprite) body.getUserData());
                if (obs.getObstacle().getBodyType() == BodyType.DynamicBody && !obs.getObstacle().isSensor()) {
//                    body.applyForceToCenter(Vector2.X, true);
//                    System.out.println((obs.getObstacle().getName() + ", " + body.getMass() + ", " + body.getLinearVelocity()));
                    body.setLinearVelocity(body.getLinearVelocity().add(windVel));
                }
            }
        }
    }

    private ObstacleSprite getFreeRain() {
        for (ObstacleSprite rainDrop : rainDrops) {
            if (rainDrop.getObstacle().getPosition().epsilonEquals(rainRestPos, 1e-3f) || rainDrop.getObstacle().getPosition().y < rainRestPos.y) {
                return rainDrop;
            }
        }
        WheelObstacle rainTemp = new WheelObstacle(rainRestPos.x, rainRestPos.y,.25f);
        rainTemp.setBodyType(BodyType.DynamicBody);
        rainTemp.setPhysicsUnits(physicsUnits);
        rainTemp.setSensor(true);
        rainTemp.setGravityScale(0);
        rainTemp.setName("rain");
        ObstacleSprite rain = new ObstacleSprite(rainTemp);
        rain.getObstacle().setUserData(rain);
        rainDrops.add(rain);
        rainflags.add(new RainFlag("addRain" , rain, random.nextInt(1,5)));
        return rain;
    }

    public void resetRain(ObstacleSprite rain) {
        rain.getObstacle().setLinearVelocity(Vector2.Zero);
        rain.getObstacle().setPosition(rainRestPos);
    }

    public boolean inRain(ObstacleSprite sprite) {
        if (!isRainActive()) {return false;}
        Vector2 pos = sprite.getObstacle().getPosition();
        collidedObstacles.clear();
        if (world == null) {return false;}
        world.rayCast(rayCastCallback, pos, new Vector2(pos.x, 18));
        System.out.println("raycast in rain");

        for (Fixture fixture : collidedObstacles) {
            Object userData = fixture.getBody().getUserData();
            if (userData instanceof ObstacleSprite) {
                ObstacleSprite target = (ObstacleSprite) userData;
                if ((target.getObstacle().getBodyType() == BodyType.KinematicBody || target.getObstacle().getBodyType() == BodyType.StaticBody)
                    && !target.getObstacle().isSensor() && !target.getObstacle().getName().contains("grate")) {
                    return false;
                }
            }
        }
        return true;
    }

}
