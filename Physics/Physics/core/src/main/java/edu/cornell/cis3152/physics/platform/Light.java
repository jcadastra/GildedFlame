package edu.cornell.cis3152.physics.platform;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.ParserUtils;
import edu.cornell.gdiac.math.Path2;
import edu.cornell.gdiac.math.PathFactory;
import edu.cornell.gdiac.physics2.Obstacle;
import edu.cornell.gdiac.physics2.ObstacleSprite;
import edu.cornell.gdiac.physics2.WheelObstacle;
import edu.cornell.gdiac.graphics.SpriteBatch;

public class Light extends ObstacleSprite {
    private static float TORCH_RADIUS;

    /* Radius of the light*/
    private float radius;

    /*ID of the light */
    private int id;

    private Vector2 position;

//    private Body body;
    private LightState state;
    private Vector2 center;

    /*The number of stomps this light source can withstand*/
    private int damp;

    JsonValue data;

    /*
    * LIGHT_ON: the light is on, has a radius, capable of lighting a flame
    * LIGHT_WAVER: the light is being stamped on, may go out after a certain count
    * LIGHT_OFF: the light is off, radius=0, can't light a flame
    * */
    public enum LightState{
        LIGHT_ON,
        LIGHT_WAVER,
        LIGHT_OFF,
    }
    public void setRadius(float radius){this.radius = radius;}

    public float getRadius(){ return radius;}

    public int getId() { return id; }
    public float getX() { return position.x; }
    public void setX(float value) { position.x = value; }
    public float getY() { return position.y; }
    public void setY(float value) { position.y = value; }
    public Vector2 getPosition() { return position; }

    public LightState getState() { return state; }
    public void setState(LightState value) { state = value; }

    public void setDamp(int damp) {this.damp = damp;}

    /*
    * The light flickers.
    * */
    public void waver(){
        //the light flickers
        // reads from attached body for collisions
        // maybe do something with the radius
    }

    /*
    The light is stable, does something
    * */
    public void stable(){

    }

    /*
    Returns the center of the light for other to attach the flame
    * */
    public Vector2 getCenter(){return center;}

    public Light (float units, JsonValue data){
        super();
        this.data = data;
        //this.id = (int)data.getFloat("id");
        this.radius = data.getFloat("radius");
        float x = data.get("pos").getFloat(0);
        float y = data.get("pos").getFloat(1);
        position = new Vector2 (x,y);
        //int id, float radius, Vector2 pos
//        this.id = id;
//        this.radius = radius;
//        this.body = body;
        this.state = LightState.LIGHT_ON;
        obstacle = new WheelObstacle(x,y,radius);
        obstacle.setDensity(0.0001f);
        obstacle.setMass(0.0001f);
        obstacle.setInertia(0.0001f);
        obstacle.setBodyType( BodyType.DynamicBody );
//        obstacle.setPhysicsUnits(units);
        obstacle.setRestitution( 0 );
        obstacle.setPhysicsUnits( units );
        obstacle.setUserData( this );
        obstacle.setSensor(true);
        obstacle.setName("light");
//        obstacle.setDensity(data.getFloat("density",0));
        obstacle.setFriction(data.getFloat("friction",0));
        obstacle.setRestitution(data.getFloat("restitution",0));
        mesh.set( -radius, -radius, 2 * radius, 2 * radius );
        //Color color =  ParserUtils.parseColor( data.get("debug"), Color.WHITE);
        debug = ParserUtils.parseColor( data.get("debug"),  Color.WHITE);
    }

    public void update(float dt){
        switch (state){
            case LIGHT_WAVER:
                waver();
            case LIGHT_OFF:
                radius =0;
            case LIGHT_ON:
                stable();
            default:
                break;
        }
    }

    @Override
    public void drawDebug(SpriteBatch batch){
        if (this.obstacle != null) {
            this.obstacle.draw(batch, this.debug);
        }
    }

    @Override
    public void draw(SpriteBatch batch){
        super.draw(batch);
    }

    public void createSensor(){
//        FixtureDef fixtureDef = new FixtureDef();
//        fixtureDef.shape = new CircleShape();
//        fixtureDef.shape.setRadius(radius);
//        fixtureDef.isSensor = true;
//
//        Vector2 sensorCenter = new Vector2(0, -radius / 2);
//        FixtureDef sensorDef = new FixtureDef();
//        sensorDef.density = data.getFloat("density",0);
//        sensorDef.isSensor = true;
//
//        CircleShape sensorShape = new CircleShape();
//        sensorShape.setRadius(radius);
//        sensorDef.shape = sensorShape;
//        // Ground sensor to represent our feet
//        Body body = obstacle.getBody();
//        Fixture sensorFixture = body.createFixture( sensorDef );
//        String sensorName = "traci_sensor";
//        sensorFixture.setUserData(sensorName);
//
//        // Finally, we need a debug outline
//        float u = obstacle.getPhysicsUnits();
//        PathFactory factory = new PathFactory();
//        Path2 sensorOutline = new Path2();
//        factory.makeRect( (sensorCenter.x-radius/2)*u,
//            (sensorCenter.y-radius/2)*u, radius*u, radius*u,  sensorOutline);
    }

}
