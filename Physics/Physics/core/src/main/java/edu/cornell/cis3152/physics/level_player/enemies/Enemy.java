package edu.cornell.cis3152.physics.level_player.enemies;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.cis3152.physics.level_player.enviromentals.Fire;
import edu.cornell.cis3152.physics.level_player.enviromentals.Lighting;
import edu.cornell.cis3152.physics.level_player.player.Avatar;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.SpriteSheet;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.physics2.BoxObstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.PolygonObstacle;

public class Enemy extends ObstacleSprite {


    protected static AssetDirectory directory;
    protected final JsonValue data;
    private final int id;
    private final float width;
    private final float height;
    private final float size;
    private final float friction;
    public SpriteBatch batch;
    public RaycastResult rr;
    /**
     * Which direction is the character facing
     */
    protected boolean faceRight;
    protected boolean justCollided;
    private Path2 sensorOutline;
    // Instance attributes
    private Color sensorColor;
    private String sensorName;
    private int frameCount;
    private EnemyState state;
    private boolean isGrounded = false;
    private float x;
    private float y;
    private float speed;
    private final float units;
    private final float hitboxScale;

    public Enemy(int id, float units, JsonValue data, AssetDirectory directory, Vector2 position) {
        Enemy.directory = directory;
        this.units = units;
        this.id = id;
        this.data = data;
        this.state = EnemyState.OUT_OF_LIGHT;
        this.faceRight = true;
        this.speed = data.getFloat("speed");
        this.size = data.getFloat("size") * units;
        this.friction = data.getFloat("friction");
        this.hitboxScale = data.getFloat("size");

        this.width = data.get("dimension").getFloat(0);
        this.height = data.get("dimension").getFloat(1);

        this.x = position.x;
        this.y = position.y;
        this.justCollided = false;

        // helpers
        float half = size/units * 0.5f;
        float cut  = size/units * 0.05f;
        float[] verts = {
            half, half,
            -half, half, //top vertices
            -half, (-half + cut),
            0, (-half), //bottom left slice
            half, (-half + cut) //bottom right

        };

        obstacle = new PolygonObstacle(verts, x, y);
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

    public float getUnits() {
        return units;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
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

    public void setDirection(boolean isRight) {
        faceRight = isRight;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }

    public boolean getJustCollided() {
        return justCollided;
    }

    public void setJustCollided(boolean collided) {
        this.justCollided = collided;
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


    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float value) {
        speed = value;
    }


    public void update() {
//        if (this instanceof Totem){
//            System.out.println(getState() + " | idle: " + ((Totem)this).getIdleFrameCount() + " | cd: "  +
//                ((Totem)this).getCdFrameCount() + " | reverse: " + ((Totem)this).getReverseFrameCount());
//        }
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
            case JUMP:
                jump();
                break;
            case TRANCE:
                trance();
                break;
            case DAZED:
                dazed();
                break;
            case SMOTHER:
                smother();
                break;
            case FRUSTRATED:
                frustrated();
                break;
            default:
                break;
        }
    }

    public void updateRayCast() {
        if (getState() == EnemyState.JUMP || getState() == EnemyState.IN_LIGHT || getState() == EnemyState.CD) {
            rr = raycastInLight();
        } else {
            rr = raycast();
        }
    }

    public void out_of_light() {

    }

    public void smother() {

    }

    public void frustrated(){

    }
    public boolean isAboutToFall() {
//        if (!isGrounded()) return false;

        Body body = obstacle.getBody();
        if (body == null) return false;

        Vector2 position = body.getPosition();
        float rayLength = 0.8f;
        float xOffset = isFacingRight() ? (width * hitboxScale / 2) : (-width * hitboxScale / 2);
        Vector2 rayStart = new Vector2(position.x + xOffset, position.y - (height * hitboxScale) / 2);

        Vector2 rayEnd = rayStart.cpy().add(0, -rayLength);
        final boolean[] groundDetected = {false};

        RayCastCallback callback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                Object userData = fixture.getBody().getUserData();
                if (userData instanceof ObstacleSprite) {
                    ObstacleSprite target = (ObstacleSprite) userData;
                    if (target.getName().contains("platform") || target.getName().contains("enemy") ||
                        target.getName().contains("ground") || target.getName().contains("floor")  ||
                        target.getName().contains("burnable")) {
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
        Vector2 start;
        Vector2 direction;
        if (isFacingRight()) {
            start = new Vector2(pos.x + width/2.0f, pos.y - height/4.0f);
            direction = new Vector2(1, 0);
        } else {
            start = new Vector2(pos.x - width/2.0f, pos.y - height/4.0f);
            direction = new Vector2(-1, 0);
        }

        float maxDistance = 5f;
        Vector2 end = start.cpy().add(direction.scl(maxDistance));
        final Object[] closestObject = {null};
        final Vector2[] closestPoint = {null};
        final float[] closestFraction = {Float.MAX_VALUE};
        RayCastCallback callback = (fixture, point, normal, fraction) -> {
            if (fixture.isSensor()){
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

    public RaycastResult raycastInLight() {

        Vector2 pos = obstacle.getBody().getPosition();
        Vector2 start;
        Vector2 direction;
        if (isFacingRight()) {
            start = new Vector2(pos.x + width/2.0f, pos.y - height/4.0f);
            direction = new Vector2(1, 0);
        } else {
            start = new Vector2(pos.x - width/2.0f, pos.y- height/4.0f);
            direction = new Vector2(-1, 0);
        }
        float maxDistance = 5f;
        Vector2 end = start.cpy().add(direction.scl(maxDistance));
        final Object[] closestObject = {null};
        final Vector2[] closestPoint = {null};
        final float[] closestFraction = {Float.MAX_VALUE};
        RayCastCallback callback = (fixture, point, normal, fraction) -> {
            if (fixture.isSensor()){
                return -1;
            } else {
                Object detectedObject = fixture.getBody().getUserData();
                if (detectedObject instanceof Lighting || detectedObject instanceof Fire) {
                    return -1;
                }
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
        Body body = obstacle.getBody();
        float direction;
        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
        if (!(this instanceof Moth && this.getState() == EnemyState.ATTACK)){
            if (isAboutToFall() && !isGrounded()) {
                changeDirection();
            }
        }
        if (isFacingRight()) {
            direction = speed;
        } else {
            direction = -speed;
        }
        body.setLinearVelocity(new Vector2(direction, body.getLinearVelocity().y));

    }

    public void move_to(Vector2 pos) {
        Body body = obstacle.getBody();
        float direction;
        if (pos.x > body.getPosition().x) {
            direction = speed;
        } else {
            direction = -speed;
        }

        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);
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

    public void jump() {
    }

    public void trance() {

    }
    public void stop() {
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

        ATTACK, JUMP, DAZED, SMOTHER, FRUSTRATED, TRANCE
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
