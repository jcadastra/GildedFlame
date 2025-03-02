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

    private static int MOVE_SPEED;

    // Instance attributes
    private int id;
    private Vector2 position;

    private EnemyState state;
    private Color spriteColor = Color.WHITE;
    private TextureRegion sprite;
    private Body body;
    private float width;
    private float height;
    private SpriteBatch batch;

    public enum EnemyState {

        OUT_OF_LIGHT,
        IN_LIGHT,
        ATTRACTED,
    }

//    public static void setConstants(JsonValue constants){

//    }

    public Enemy(int id, Vector2 position, Body body, float width, float height, SpriteBatch batch){
        this.id = id;
        this.position = position;
        this.state = EnemyState.OUT_OF_LIGHT;
        this.body = body;
        this.width = width;
        this.height = height;
        this.batch = batch;
        obstacle = new BoxObstacle(position.x, position.y, width, height);
    }

    public int getId() { return id; }
    public float getX() { return position.x; }
    public void getX(float value) { position.x = value; }
    public float getY() { return position.y; }
    public void getY(float value) { position.y = value; }
    public Vector2 getPosition() { return position; }

    public EnemyState getState() { return state; }
    public void setState(EnemyState value) { state = value; }

    public void update(){
        switch (state) {
            case OUT_OF_LIGHT:
                out_of_light();
                break;
            case IN_LIGHT:
                in_light();
                break;
            case ATTRACTED:
                attracted();
                break;
            default:
                break;
        }
    }

    public void out_of_light(){
        position.x += MOVE_SPEED;
    }

    public void move_to(Vector2 target) {
        int direction = MOVE_SPEED;
        if (target.x < position.x) {
            direction *= -1;
        } else if (target.x == position.x) {
            direction *= 0;
        }
        body.applyForceToCenter(new Vector2(direction, 0), true);
    }

    public void in_light(){

    }

    public void attracted(){
    }
    public void stop() { body.setLinearVelocity(0, 0); }

    @Override
    public void draw(SpriteBatch batch) {
        batch.setColor(spriteColor);
        super.draw(batch);
        batch.setColor(Color.WHITE);
    }
}
