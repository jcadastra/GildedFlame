package edu.cornell.cis3152.physics.platform;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.graphics.SpriteBatch;
import edu.cornell.gdiac.graphics.Texture2D;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.physics2.*;

public class Enemy extends ObstacleSprite {

    private static int MOVE_SPEED = 3;
    private JsonValue data;

    private Path2 sensorOutline;
    private Color sensorColor;
    private String sensorName;
    // Instance attributes
    /** Which direction is the character facing */
    private boolean faceRight;
    private int id;
    private Vector2 position;

    private int freezeTimer;

    private int attackTimer;

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
        faceRight = !faceRight;
    }
    private float width;
    private float height;
    private float x;
    private float y;
    public SpriteBatch batch;

    private Fixture fixture;

    public enum EnemyState {

        OUT_OF_LIGHT,
        IN_LIGHT,

        ANGRY,

        ATTACK
    }

    public Enemy(int id, float units, JsonValue data) {
        this.id = id;
        this.data = data;
        this.state = EnemyState.OUT_OF_LIGHT;
        this.faceRight = true;

        float s = data.getFloat( "size" );
        float size = s*units;

        this.width = data.get("dimension").getFloat(0);
        this.height = data.get("dimension").getFloat(1);

        this.x = data.get("pos").getFloat(0);
        this.y = data.get("pos").getFloat(1);
        obstacle = new BoxObstacle(x, y, width, height);
        obstacle.setBodyType(BodyDef.BodyType.DynamicBody);

        obstacle.setDensity( data.getFloat( "density", 0 ) );
        obstacle.setFriction( data.getFloat( "friction", 0 ) );
        obstacle.setRestitution( data.getFloat( "restitution", 0 ) );

        obstacle.setPhysicsUnits( units );
        obstacle.setFixedRotation(true);
        obstacle.setUserData( this );
        obstacle.setName("enemy");


        mesh.set(-size/2.0f,-size/2.0f,size,size);
    }


    public int getId() { return id; }
    public float getX() { return x; }
    public void setX(float value) { x = value; }
    public float getY() { return y; }
    public void setY(float value) { y = value; }

    public int getMoveSpeed() { return MOVE_SPEED; }
    public void setMoveSpeed(int value) { MOVE_SPEED = value; }

    public void resetMoveSpeed() { MOVE_SPEED = 3; }
    public Fixture getFixture() {
        return obstacle.getBody().getFixtureList().first();
    }

    public EnemyState getState() { return state; }
    public void setState(EnemyState value) { state = value; }


    public int getFreezeTimer() { return freezeTimer; }

    public void decrementFreezeTimer() { freezeTimer--; }

    public void resetFreeze() { freezeTimer = data.getInt("freezeTimer");}


    public int getAttackTimer() { return attackTimer; }

    public void decrementAttackTimer() { attackTimer--; }

    public void resetAttackTimer() { attackTimer = data.getInt("attackTimer");}

    public void update(){
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

    public void out_of_light(){
        x += MOVE_SPEED;
    }

    public void move_to(Vector2 target) {
        Body body = obstacle.getBody();
        if (body == null) {
            System.out.println("R");
            return;
        }
        int direction = MOVE_SPEED;
        if (target.x < x) {
            direction *= -1;
        } else if (target.x == x) {
            direction *= 0;
        }
        obstacle.getBody().applyForceToCenter(new Vector2(direction, 0), true);
    }

    public void move() {
        int direction;
        Body body = obstacle.getBody();
        if (body == null) {
            return;
        }
        if (!isFacingRight()) {
            direction = MOVE_SPEED;
        } else {
            direction = -MOVE_SPEED;
        }
        obstacle.getBody().setLinearVelocity(new Vector2(direction, obstacle.getBody().getLinearVelocity().y));
    }

    public void in_light(){

    }

    public void angry(){
    }

    public void attack(){}
    public void stop() { obstacle.getBody().setLinearVelocity(0, 0); }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }

    public void createSensor() {
        Vector2 sensorCenter = new Vector2(0, -height / 2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = data.getFloat("density",0);
        sensorDef.isSensor = true;

        JsonValue sensorjv = data.get("sensor");
        float w = sensorjv.getFloat("shrink",0)*width/2.0f;
        float h = sensorjv.getFloat("height",0);
        PolygonShape sensorShape = new PolygonShape();
        sensorShape.setAsBox(w, h, sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        // Ground sensor to represent our feet
        Body body = obstacle.getBody();
        Fixture sensorFixture = body.createFixture( sensorDef );
//        sensorName = "traci_sensor";
        sensorFixture.setUserData(sensorName);

        // Finally, we need a debug outline
        float u = obstacle.getPhysicsUnits();
        PathFactory factory = new PathFactory();
        sensorOutline = new Path2();
        factory.makeRect( (sensorCenter.x-w/2)*u,(sensorCenter.y-h/2)*u, w*u, h*u,  sensorOutline);
    }

}
