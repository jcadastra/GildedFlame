package edu.cornell.cis3152.physics.level_player.enviromentals;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.cis3152.physics.level_player.utils.RainFlag;
import edu.cornell.gdiac.math.Poly2;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import edu.cornell.gdiac.util.PooledList;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

public class MarthasWeatherMachine {

    private RayCastCallback rayCastCallback;
    private ArrayList<Fixture> collidedObstacles;
    private float rainAngle = 0;
    private ArrayList<ObstacleSprite> rainDrops;
    private float physicsUnits;
    private int rainTimer;
    private int rainFreq;
    private Random random;
    private World world;

    private Stack<RainFlag> rainflags;


    public MarthasWeatherMachine(float physicsUnits, int rainFreq) {
        this.physicsUnits = physicsUnits;
        this.rainFreq = this.rainTimer = rainFreq;
        this.rainflags = new Stack<>();
        this.random = new Random();
        this.collidedObstacles = new ArrayList<Fixture>(){};
        this.rayCastCallback = (fixture, point, normal, fraction) -> {
            collidedObstacles.add(fixture);
            return 1;
        };
        this.rainDrops = new ArrayList<ObstacleSprite>(){};
    }

    public void defineRainAngle (float angle) {
        this.rainAngle = angle;
    }

    public Stack<RainFlag> getRainflags() {return rainflags;}

    public void clean() {
//        rainRegion.clear();
//        kinematicObstacles.clear();
//        triangulator.clear();
//        path2.clear();
//        pathSmoother.clear();
    }

    public void update(World world) {
        if (this.world == null) {
            this.world = world;
        }
        generateRain();
    }

    public void generateRain() {
        rainTimer -= random.nextInt(3)+1;
        if (rainTimer <= 0) {
            rainTimer = rainFreq;

            ObstacleSprite newDrop = getFreeRain();
            if (newDrop.getObstacle() == null) {
                return;
            }

            newDrop.getObstacle().setPosition(random.nextInt(32) + random.nextFloat(), 18);
            newDrop.getObstacle().setLinearVelocity(new Vector2(0,-5));
        }
    }

    private ObstacleSprite getFreeRain() {
        System.out.println(rainDrops.size());
        for (ObstacleSprite os : rainDrops) {
            Vector2  ref = new Vector2(-2,-2);
            if (Objects.equals(os.getObstacle().getPosition(), ref)) {
                return os;
            }
        }
        WheelObstacle rainTemp = new WheelObstacle(-2,-2,.5f);
        rainTemp.setBodyType(BodyType.DynamicBody);
        rainTemp.setSensor(true);
        rainTemp.setGravityScale(0);
        rainTemp.setName("rain");
        ObstacleSprite rain = new ObstacleSprite(rainTemp);
        rain.getObstacle().setUserData(rain);
        rain.getObstacle().setPhysicsUnits(physicsUnits);

        rainDrops.add(rain);
        rainflags.add(new RainFlag("addRain" , rain));
        return rain;
    }

    public void resetRain(ObstacleSprite rain) {
        rain.getObstacle().setLinearVelocity(Vector2.Zero);
        rain.getObstacle().setPosition(-2,-2);
    }

    public boolean inRain(ObstacleSprite sprite) {
        Vector2 pos = sprite.getObstacle().getPosition();
        collidedObstacles.clear();
        world.rayCast(rayCastCallback, pos, new Vector2(pos.x, 18));

        for (Fixture fixture : collidedObstacles) {
            Object userData = fixture.getBody().getUserData();
            if (userData instanceof ObstacleSprite) {
                ObstacleSprite target = (ObstacleSprite) userData;
                if ((target.getObstacle().getBodyType() == BodyType.KinematicBody || target.getObstacle().getBodyType() == BodyType.StaticBody)
                    && !target.getObstacle().isSensor()) {
                    return false;
                }
            }
        }
        return true;
    }

}
