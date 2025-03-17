package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.cis3152.physics.level_player.enviromentals.Light;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;

public class Enemy extends ObstacleSprite {


    private final JsonValue data;
    private final int id;
    private final float width;
    private final float height;
    public SpriteBatch batch;
    public RaycastResult rr;
    protected AssetDirectory directory;
    private Path2 sensorOutline;
    // Instance attributes
    private Color sensorColor;
    private String sensorName;
    /**
     * Which direction is the character facing
     */
    private boolean faceRight;
    private int freezeTimer;
    private int attackTimer;
    private int attackAnimationTimer;
    private EnemyState state;
    private boolean isGrounded = false;
    private float x;
    private float y;
    private float speed;
    private float size;
    private Fixture fixture;
    private boolean justCollided = false;

    public Enemy(int id, float units, JsonValue data, AssetDirectory directory, Vector2 position) {
        this.directory = directory;
        this.id = id;
        this.data = data;
        this.state = EnemyState.OUT_OF_LIGHT;
        this.faceRight = true;
        this.speed = data.getFloat("speed");
        this.size = data.getFloat("size") * units;

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

    /**
     * Returns true if this character is facing right
     *
     * @return true if this character is facing right
     */
    public boolean isFacingRight() {
        return faceRight;
    }

    public void changeDirection() {
        if (!justCollided) {
            faceRight = !isFacingRight();
        }
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public boolean hasJustCollided() {
        return justCollided;
    }

    public void setJustCollided(boolean collided) {
        this.justCollided = collided;
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

    public float getSize() {
        return size;
    }

    public void setSize(float val) {
        size = val;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float value) {
        speed = value;
    }

    public void update() {
        updateRayCast();
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
            case CD:
                cd();
                break;
            case CREEP:
                creep();
                break;
            case DAZED:
                dazed();
            default:
                break;
        }
    }

    public void updateRayCast() {
        if (getState() == EnemyState.CREEP || getState() == EnemyState.IN_LIGHT || getState() == EnemyState.CD) {
            rr = raycastInLight();
        } else {
            rr = raycast();
        }
    }

    public void out_of_light() {

    }

    public boolean isAboutToFall() {
//        if (!isGrounded()) return false;

        Body body = obstacle.getBody();
        if (body == null) return false;

        Vector2 position = body.getPosition();
        float rayLength = 1.0f;
        float xOffset = isFacingRight() ? (width / 2 - 0.4f) : (-width / 2 + 0.4f);
        Vector2 rayStart = new Vector2(position.x + xOffset, position.y - height / 2);

        Vector2 rayEnd = rayStart.cpy().add(0, -rayLength);

        final boolean[] groundDetected = {false};

        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                Object userData = fixture.getBody().getUserData();
                if (userData instanceof ObstacleSprite) {
                    ObstacleSprite target = (ObstacleSprite) userData;
                    if (target.getName().equals("platform") || target.getName().equals("enemy") || target.getName().equals("ground")) {
                        groundDetected[0] = true;
                    }
                }
                return fraction;
            }
        };

        body.getWorld().rayCast(callback, rayStart, rayEnd);
        return !groundDetected[0];
    }

    public RaycastResult raycast() {

        Body body = obstacle.getBody();
        if (body == null) {
            return null;
        }
        Vector2 pos = obstacle.getBody().getPosition();
        Vector2 start = new Vector2(pos.x, pos.y - height / 4);
        Vector2 direction;
        if (isFacingRight()) {
            direction = new Vector2(1, 0);
        } else {
            direction = new Vector2(-1, 0);
        }

        float maxDistance = 5f;
        Vector2 end = start.cpy().add(direction.scl(maxDistance));
        final Object[] closestObject = {null};
        final Vector2[] closestPoint = {null};
        final float[] closestFraction = {Float.MAX_VALUE};
        RayCastCallback callback = (fixture, point, normal, fraction) -> {
            if (fraction < closestFraction[0]) {
                closestObject[0] = fixture.getBody().getUserData();
                closestPoint[0] = new Vector2(point);
                closestFraction[0] = fraction;
            }
            return fraction;
        };

        World world = obstacle.getBody().getWorld();
        world.rayCast(callback, start, end);

        if (closestObject[0] != null && closestPoint[0] != null) {
            float distance = start.dst(closestPoint[0]);
            return new RaycastResult(closestObject[0], closestPoint[0], distance);
        }

        return null;
    }

    public RaycastResult raycastInLight() {
//        System.out.println("Using new raycast");

        Vector2 pos = obstacle.getBody().getPosition();
        Vector2 start = new Vector2(pos.x, pos.y - height / 4);
        Vector2 direction;
        if (isFacingRight()) {
            direction = new Vector2(1, 0);
        } else {
            direction = new Vector2(-1, 0);
        }
        float maxDistance = 5f;
        Vector2 end = start.cpy().add(direction.scl(maxDistance));
        final Object[] closestObject = {null};
        final Vector2[] closestPoint = {null};
        final float[] closestFraction = {Float.MAX_VALUE};
        RayCastCallback callback = (fixture, point, normal, fraction) -> {
            // If the fixture belongs to a Light, ignore it
            Object detectedObject = fixture.getBody().getUserData();
            if (detectedObject instanceof Light || detectedObject instanceof Fire) {
                return -1;
            }
            if (fraction < closestFraction[0]) {
                closestObject[0] = fixture.getBody().getUserData();
                closestPoint[0] = new Vector2(point);
                closestFraction[0] = fraction;
            }
            return fraction;
        };


        World world = obstacle.getBody().getWorld();
        world.rayCast(callback, start, end);

        if (closestObject[0] != null && closestPoint[0] != null) {
            float distance = start.dst(closestPoint[0]);
            return new RaycastResult(closestObject[0], closestPoint[0], distance);
        }

        return null;
    }

    public void move() {
//        System.out.print("MOVE: " );
        Body body = obstacle.getBody();
        float direction;
        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
        if (isAboutToFall() && !isGrounded()) {
            changeDirection();
        }
        if (isFacingRight()) {
//            System.out.println("Facing right");
            direction = speed;
        } else {
//            System.out.println("Facing left");
            direction = -speed;
        }

//        System.out.println("Direction: " + getSpeed());
        body.setLinearVelocity(new Vector2(direction, body.getLinearVelocity().y));

    }

    public void in_light() {

    }

    public void dazed() {
    }

    /*
     * angry when in range of the light
     */
    public void angry() {
    }

    public void attack() {
    }

    public void cd() {
    }

    public void creep() {
    }

    public void stop() {
//        System.out.println("stopping");
        float currY = obstacle.getLinearVelocity().y;
        obstacle.getBody().setLinearVelocity(0, currY);
        obstacle.setBodyType(BodyDef.BodyType.StaticBody);
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
        sensorFixture.setUserData(sensorName);

        // Finally, we need a debug outline
        float u = obstacle.getPhysicsUnits();
        PathFactory factory = new PathFactory();
        sensorOutline = new Path2();
        factory.makeRect((sensorCenter.x - w / 2) * u, (sensorCenter.y - h / 2) * u, w * u, h * u, sensorOutline);
    }

    public enum EnemyState {

        OUT_OF_LIGHT, IN_LIGHT,

        ANGRY, CD,

        ATTACK, CREEP, DAZED
    }

    public class RaycastResult {
        public Object targetObject;
        public Vector2 targetPosition;
        public float targetDistance;

        public RaycastResult(Object object, Vector2 point, float distance) {
            this.targetObject = object;
            this.targetPosition = point;
            this.targetDistance = distance;
        }
    }

}
