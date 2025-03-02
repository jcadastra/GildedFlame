package edu.cornell.cis3152.physics.platform;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.utils.JsonValue;

public class Enemy {

    private static float MOVE_SPEED;

    // Instance attributes
    private int id;
    private Vector2 position;
    private int state;
    // default is 0
    // frozen is 1

//    public static void setConstants(JsonValue constants){

//    }

    public Enemy(int id, float x, float y){
        this.id = id;
        position = new Vector2(x,y);
        state = 0;
    }

    public int getId() { return id; }
    public float getX() { return position.x; }
    public void getX(float value) { position.x = value; }
    public float getY() { return position.y; }
    public void getY(float value) { position.y = value; }
    public Vector2 getPosition() { return position; }

    public int getState() { return state; }
    public void setState(int value) { state = value; }

    public void update(){
        if (state == 0) {
            // move
        } else if (state == 1) {

        }
    }


}
