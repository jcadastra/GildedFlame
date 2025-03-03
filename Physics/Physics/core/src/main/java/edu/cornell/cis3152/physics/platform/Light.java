package edu.cornell.cis3152.physics.platform;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.ParserUtils;
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

    private Body body;
    private LightState state;
    private Vector2 center;

    /*The number of stomps this light source can withstand*/
    private int damp;

    private Obstacle obstacle;

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

    @Override
    public Obstacle getObstacle() {
        return obstacle;
    }

    /*
    * The light flickers.
    * */
    public void waver(){
        //the light flickers
        // reads from attached body for collisions
        // maybe do something with the radius
    }

    /*
    The light is stable
    * */
    public void stable(){

    }

    /*
    Returns the center of the light for other to attach the flame
    * */
    public Vector2 getCenter(){return center;}

    public Light (float units, JsonValue data){
        this.data = data;
        //this.id = (int)data.getFloat("id");
        this.radius = data.getFloat("radius")*units;
        float x = data.get("pos").getFloat(0);
        float y = data.get("pos").getFloat(1);
        position = new Vector2 (x,y);
        //int id, float radius, Vector2 pos
//        this.id = id;
//        this.radius = radius;
//        this.body = body;
        this.state = LightState.LIGHT_ON;
        obstacle = new WheelObstacle(x,y,radius);
        obstacle.setDensity(0);
        obstacle.setPhysicsUnits(units);
        obstacle.setFixedRotation(true);
        obstacle.setRestitution( 0 );
        obstacle.setPhysicsUnits( units );
        obstacle.setUserData( this );
        obstacle.setName("light");
        mesh.set( -radius, -radius, 2 * radius, 2 * radius );
    }

    public void update(float dt){
        switch (state){
            case LIGHT_WAVER:
                waver();
            case LIGHT_OFF:
                radius =0;
            case LIGHT_ON:
                break;
            default:
                break;
        }
    }



//    @Override
//    public void draw(SpriteBatch batch){
//
//    }

}
