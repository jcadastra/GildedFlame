package edu.cornell.cis3152.physics.platform;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;

public class Enemy {

    private static int MOVE_SPEED;

    // Instance attributes
    private int id;
    private Vector2 position;

    private EnemyState state;
    private Body body;

    public enum EnemyState {

        OUT_OF_LIGHT,
        IN_LIGHT,
        ATTRACTED,
    }

//    public static void setConstants(JsonValue constants){

//    }

    public Enemy(int id, Vector2 position, Body body){
        this.id = id;
        this.position = position;
        this.state = EnemyState.OUT_OF_LIGHT;
        this.body = body;
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
                move();
                break;
            case IN_LIGHT:
                react();
                break;
            case ATTRACTED:
                attracted();
                break;
            default:
                break;
        }
    }

    public void move(){
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

    public void react(){

    }

    public void attracted(){
    }


}
