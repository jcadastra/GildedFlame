package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.Texture2D;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.physics2.*;

import java.util.List;

public class Enemy extends ObstacleSprite {


    protected AssetDirectory directory;
    private JsonValue data;

    private Path2 sensorOutline;
    private Color sensorColor;
    private String sensorName;
    // Instance attributes
    /**
     * Which direction is the character facing
     */
    private boolean faceRight;
    private int id;
    private int freezeTimer;

    private int attackTimer;

    private int attackAnimationTimer;

    private EnemyState state;

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    public void changeDirection() {
        faceRight = !isFacingRight();
    }


    public void setFaceRight() {
        faceRight = true;
    }

    public void setFaceLeft() {
        faceRight = false;
    }

    private float width;
    private float height;
    private float x;
    private float y;
    private int speed;
    public SpriteBatch batch;

    private Fixture fixture;

    public enum EnemyState {

        OUT_OF_LIGHT,
        IN_LIGHT,

        ANGRY,

        ATTACK,
        CREEP,

    }

    public Enemy(int id, float units, JsonValue data, AssetDirectory directory, Vector2 position) {
        this.directory = directory;
        this.id = id;
        this.data = data;
        this.state = EnemyState.OUT_OF_LIGHT;
        this.faceRight = true;
        this.speed = data.getInt("speed");

        float s = data.getFloat("size");
        float size = s * units;

        this.width = data.get("dimension").getFloat(0);
        this.height = data.get("dimension").getFloat(1);

        this.x = position.x;
        this.y = position.y;
        obstacle = new BoxObstacle(x, y, width, height);
        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);

        obstacle.setDensity(data.getFloat("density", 0));
        obstacle.setFriction(data.getFloat("friction", 0));
        obstacle.setRestitution(data.getFloat("restitution", 0));

        obstacle.setPhysicsUnits(units);
        obstacle.setFixedRotation(true);
        obstacle.setUserData(this);
        obstacle.setName("enemy");


        mesh.set(-size / 2.0f, -size / 2.0f, size, size);
    }


    public int getId() {
        return id;
    }

    public float getX() {
        return x;
    }

    public void setX(float value) {
        x = value;
    }

    public float getY() {
        return y;
    }

    public void setY(float value) {
        y = value;
    }

    public Fixture getFixture() {
        return obstacle.getBody().getFixtureList().first();
    }

    public EnemyState getState() {
        return state;
    }

    public void setState(EnemyState value) {
        state = value;
    }


    public int getFreezeTimer() {
        return freezeTimer;
    }

    public void decrementFreezeTimer() {
        freezeTimer--;
    }

    public void resetFreeze() {
        freezeTimer = data.getInt("freezeTimer");
    }

    public int getAttackTimer() {
        return attackTimer;
    }

    public void decrementAttackTimer() {
        attackTimer--;
    }

    public void resetAttackTimer() {
        attackTimer = data.getInt("attackTimer");
    }

    public int getAttackAnimationTimer() {
        return attackAnimationTimer;
    }

    public void decrementAttackAnimationTimer() {
        attackAnimationTimer--;
    }

    public void resetAttackAnimationTimer() {
        attackAnimationTimer = data.getInt("attackAnimationTimer");
    }

    public void update() {
        switch (state) {
            case OUT_OF_LIGHT:
                out_of_light();
                break;
            case IN_LIGHT:
                in_light();
                break;
            case ANGRY:
                angry();
                break;
            case ATTACK:
                attack();
                break;
            default:
                break;
        }
    }

    public void out_of_light() {

    }

//    public void move_to(Vector2 target) {
//        Body body = obstacle.getBody();
//        if (body == null) {
//            System.out.println("R");
//            return;
//        }
//        int direction = MOVE_SPEED;
//        if (target.x < x) {
//            direction *= -1;
//        } else if (target.x == x) {
//            direction *= 0;
//        }
//        obstacle.getBody().applyForceToCenter(new Vector2(direction, 0), true);
//    }


    public boolean isAboutToFall() {
        Body body = obstacle.getBody();
        if (body == null) return false;

        Vector2 position = body.getPosition();
        float halfWidth = width / 2.0f;
        Vector2 rayStart;
        if (isFacingRight()) {
            rayStart = new Vector2(position.x - halfWidth, position.y - height / 2);
        } else {
            rayStart = new Vector2(position.x + halfWidth, position.y - height / 2);
        }

        float rayLength = 1.0f;
        Vector2 rayEnd = rayStart.cpy().add(0, -rayLength);

        final boolean[] groundDetected = {false};

        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                ObstacleSprite target = (ObstacleSprite) fixture.getBody().getUserData();
                if (target.getName().equals("platform") || target.getName().equals("enemy")) {
                    groundDetected[0] = true;
                }
                return fraction;
            }
        };

        body.getWorld().rayCast(callback, rayStart, rayEnd);
        return !groundDetected[0];
    }


    public void raycast() {
        Vector2 start = obstacle.getBody().getPosition();
        Vector2 direction;
        if (isFacingRight()) {
            direction = new Vector2(-1, 0);
        } else {
            direction = new Vector2(1, 0);
        }
        float maxDistance = 5f;
        Vector2 end = start.cpy().add(direction.scl(maxDistance));
        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                return fraction;
            }
        };
        World world = obstacle.getBody().getWorld();
        world.rayCast(callback, start, end);
    }

    public void move() {
        raycast();

        Body body = obstacle.getBody();
        if (body == null) {
            return;
        }

        if (isAboutToFall()) {
            changeDirection();
        }

        int direction;
        if (!isFacingRight()) {
            direction = speed;
        } else {
            direction = -speed;
        }
        obstacle.getBody().setLinearVelocity(new Vector2(direction, obstacle.getBody().getLinearVelocity().y));

    }

    public void in_light() {

    }

    public void angry() {
    }

    public void attack() {
    }

    public void stop() {
        float currY = obstacle.getLinearVelocity().y;
        obstacle.getBody().setLinearVelocity(0, currY);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }

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
//        sensorName = "traci_sensor";
        sensorFixture.setUserData(sensorName);

        // Finally, we need a debug outline
        float u = obstacle.getPhysicsUnits();
        PathFactory factory = new PathFactory();
        sensorOutline = new Path2();
        factory.makeRect((sensorCenter.x - w / 2) * u, (sensorCenter.y - h / 2) * u, w * u, h * u, sensorOutline);
    }

}
